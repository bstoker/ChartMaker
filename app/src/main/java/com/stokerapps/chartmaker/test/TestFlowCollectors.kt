/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

@file:Suppress("unused")

package com.stokerapps.chartmaker.test

import androidx.lifecycle.Observer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

const val AWAIT_DELAY_IN_MS = 10L
const val TIMEOUT_IN_MS = 200L

/** Blocks on [getValue] and [nextValue] */
open class BlockingObserver<T> : Observer<T> {

    private val results = mutableListOf<T>()
    private val resultsLock = ReentrantLock()
    private val condition = resultsLock.newCondition()
    private var index: Int = 0

    fun size() = synchronized(results) { results.size }

    fun values() = synchronized(results) { results.toList() }

    fun nextValue(timeoutMillis: Long = TIMEOUT_IN_MS) = getValue(index++, timeoutMillis)

    override fun onChanged(value: T) {
        log("Collecting $value")
        resultsLock.withLock {
            results.add(value)
            condition.signal()
        }
    }

    fun getValue(index: Int, timeoutMillis: Long = TIMEOUT_IN_MS): T =
        resultsLock.withLock {
            while (index >= results.size) {
                log("Awaiting result")
                if (!condition.await(
                        timeoutMillis,
                        TimeUnit.MILLISECONDS
                    )
                ) {
                    log("Timeout occurred")
                    throw TimeoutException()
                }
            }

            log("Getting result ${results[index]}")
            results[index]
        }
}

/** @see BlockingObserver */
class BlockingFlowCollector<T> : BlockingObserver<T>(), FlowCollector<T> {
    override suspend fun emit(value: T) {
        onChanged(value)
    }
}

class SuspendingFlowCollector<T> : FlowCollector<T> {

    private val results = mutableListOf<T>()
    private val size = AtomicInteger()
    private var index: Int = 0

    fun size() = size.get()

    fun values() = synchronized(results) { results.toList() }

    suspend fun nextValue(timeoutMillis: Long = TIMEOUT_IN_MS) = getValue(index++, timeoutMillis)

    override suspend fun emit(value: T) {
        synchronized(results) {
            log("Collecting $value")
            results.add(value)
            size.set(results.size)
        }
    }

    suspend fun getValue(index: Int, timeoutMillis: Long = TIMEOUT_IN_MS): T? =
        withTimeout(timeoutMillis) {

            while (index >= size.get()) {
                log("Awaiting result")
                delay(AWAIT_DELAY_IN_MS)
            }

            synchronized(results) {
                log("Getting result ${results[index]}")
                results[index]
            }
        }
}