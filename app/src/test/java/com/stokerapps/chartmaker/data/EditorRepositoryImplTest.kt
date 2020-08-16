/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data

import com.stokerapps.chartmaker.common.Result
import com.stokerapps.chartmaker.domain.Editor
import com.stokerapps.chartmaker.test.BlockingFlowCollector
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Test

@ExperimentalCoroutinesApi
@FlowPreview
@InternalCoroutinesApi
class EditorRepositoryImplTest {

    private val data = TestData()

    @Test
    fun checkDefaultDataReturned() = runBlockingTest {

        val cache = data.cache
        val database = data.database

        val repository = EditorRepositoryImpl(cache, database)
        val results = BlockingFlowCollector<Result<Editor>>()

        val job = launch {
            repository.getEditorFlow()
                .collect(results)
        }

        val editor = results.nextValue().getOrNull()
        assertEquals(Editor.default, editor)
        assertEquals(editor, cache.getEditorFlow().first())
        assertNull(database.getEditorFlow().first())

        assertEquals(1, results.size())
        assertTrue(uncaughtExceptions.isEmpty())

        job.cancelAndJoin()
    }

    @Test
    fun checkCachedDataReturned() = runBlockingTest {

        val cache = data.cache
        val database = data.database
        val editor1 = data.editor1

        val repository = EditorRepositoryImpl(cache, database)
        val results = BlockingFlowCollector<Result<Editor>>()

        val job = launch {
            repository.store(editor1)
            repository.getEditorFlow()
                .collect(results)
        }

        assertEquals(cache.getEditorFlow().first(), results.nextValue().getOrNull())
        assertEquals(1, results.size())
        assertTrue(uncaughtExceptions.isEmpty())

        job.cancelAndJoin()
    }

    @Test
    fun checkDataSourceDataReturned() = runBlockingTest {

        val cache = data.cache
        val database = data.database
        val editor1 = data.editor1
        val editor2 = data.editor2

        val repository = EditorRepositoryImpl(cache, database)
        val results = BlockingFlowCollector<Result<Editor>>()

        val job = launch {
            repository.store(editor1)
            repository.getEditorFlow()
                .collect(results)
        }

        assertEquals(editor1, results.nextValue().getOrNull())
        assertEquals(1, results.size())

        repository.store(editor2)

        assertEquals(editor2, results.nextValue().getOrNull())
        assertEquals(2, results.size())
        assertTrue(uncaughtExceptions.isEmpty())

        job.cancelAndJoin()
    }

    @Test
    fun checkErrorReturned() = runBlockingTest {

        val cache = data.cache
        val database = data.databaseThrowingExceptions
        val editor1 = data.editor1

        val repository = EditorRepositoryImpl(cache, database)
        val results = BlockingFlowCollector<Result<Editor>>()

        val job = launch {
            repository.getEditorFlow()
                .collect(results)
        }

        assertNotNull(results.nextValue().exceptionOrNull())

        assertNotNull(runCatching { repository.store(editor1) }.exceptionOrNull())
        assertNotNull(results.nextValue().getOrNull())

        assertEquals(2, results.size())
        assertTrue(uncaughtExceptions.isEmpty())

        job.cancelAndJoin()
    }

}