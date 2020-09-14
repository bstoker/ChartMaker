/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.*
import org.junit.Assert.assertNotEquals
import org.junit.rules.TestRule
import java.math.BigDecimal

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@Ignore("Playground")
class Playground {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    private val mainThreadSurrogate = newSingleThreadContext("main")

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @InternalCoroutinesApi
    @Test
    fun main() {

        val flow1 = ConflatedBroadcastChannel(1)
        val flow2 = flowOf(1).onStart { delay(1000) }.onCompletion { }
        val flow3 = flowOf(5).onStart { delay(3000) }

        runBlocking {
            flowOf(
                flow1.asFlow(),
                flow2,
                flow3
            )
                .map { it }
                .flattenConcat()
                .distinctUntilChanged()
                .collect { number ->
                    println(number)
                }
        }
    }

    @Test
    fun testBigDecimal() {
        val value = BigDecimal(0.1)
        assertNotEquals(BigDecimal("0.1"), value)
    }

    @Test
    fun testFlowCombine() {
        val flowA = flowOf(com.stokerapps.chartmaker.common.Result.success(1))
        val flowB = flowOf(1)
        val flowSum = flowA.combine(flowB) { resultA, b ->
            when (val a = resultA.getOrNull()) {
                null -> -1
                else -> a + b
            }
        }
        runBlocking {
            flowSum.collect {
                println(it)
            }
        }
    }

    @Test
    fun testFlowMerge() {

        val flowA = flowOf(listOf(1, 2), listOf(3, 4))
        val flowB = flowOf(listOf(5, 6), listOf(7))

        val flowMapA = flowA.map { mapOf("A" to it) }
        val flowMapB = flowB.map { mapOf("B" to it) }

        val merged = flowOf(flowMapA, flowMapB)
            .flattenConcat()

        runBlocking {
            merged.collect { println(it) }
        }
    }

    @Test
    fun testFlowSwitching() {

        val flow1 = flowOf(1, 2, 3)
        val flow2 = flowOf(4).onStart { delay(1000) }
        val chartId = ConflatedBroadcastChannel<Boolean>()

        val plantsUsingFlow: LiveData<Int> = chartId.asFlow()
            .flatMapLatest { chartId ->
                if (chartId) {
                    flow1
                } else {
                    flow2
                }
            }.asLiveData()

        chartId.offer(true)
    }

    @Test
    fun testFlowCombineDistinct() {

        data class A(val one: Int, val two: Int)
        data class B(val three: Boolean, val four: String)
        data class C(val five: Int, val six: Boolean)

        val aFlow = flow {
            emit(A(1, 2))
            delay(1000)
            emit(A(1, 3))
        }

        val bFlow = flow {
            delay(1000)
            emit(B(true, "text"))
            delay(1000)
            emit(B(true, "more"))
        }

        val liveData = aFlow
            .combine(bFlow) { a, b ->
                C(a.one, b.three)
            }
            .flowOn(Dispatchers.Default)
            .distinctUntilChanged()
            .asLiveData()

        val observer = Observer<C> {
            println(it)
        }

        runBlocking {


            liveData.observeForever(observer)

            delay(5000)

            liveData.removeObserver(observer)
        }
    }
}
