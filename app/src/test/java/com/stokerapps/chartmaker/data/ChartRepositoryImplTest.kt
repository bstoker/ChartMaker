/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data

import com.stokerapps.chartmaker.common.Result
import com.stokerapps.chartmaker.domain.PieChart
import com.stokerapps.chartmaker.test.BlockingFlowCollector
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Test
import java.util.*

@InternalCoroutinesApi
@FlowPreview
@ExperimentalCoroutinesApi
class ChartRepositoryImplTest {

    private val data = TestData()

    @Test
    fun testPieChartFlow() = runBlockingTest {

        val cache = data.cache
        val chart1 = data.chart1
        val database = data.database

        val repository = ChartRepositoryImpl(cache, database)
        val results = BlockingFlowCollector<Result<PieChart>>()

        val job = launch {
            repository.getPieChartFlow(chart1.id).collect(results)
        }

        assertNull(results.nextValue().getOrNull())

        repository.store(chart1)
        assertEquals(chart1, results.nextValue().getOrNull())
        assertEquals(2, results.size())

        val newChart1 = chart1.copy(isRotationEnabled = true, dateModified = Date())
        repository.store(newChart1)
        assertEquals(newChart1, results.nextValue().getOrNull())
        assertEquals(3, results.size())

        repository.delete(chart1)
        assertNull(results.nextValue().getOrNull())
        assertEquals(4, results.size())
        assertTrue(uncaughtExceptions.isEmpty())

        job.cancelAndJoin()
    }
}