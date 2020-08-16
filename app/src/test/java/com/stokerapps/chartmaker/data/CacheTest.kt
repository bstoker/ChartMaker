/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data

import android.graphics.Color
import com.stokerapps.chartmaker.domain.Editor
import com.stokerapps.chartmaker.domain.PieChart
import com.stokerapps.chartmaker.test.BlockingFlowCollector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class CacheTest {

    private val cache = Cache()

    private val editor1 = Editor.default.copy(
        1,
        LinkedList(listOf(Color.RED))
    )
    private val editor2 = Editor.default.copy(
        1,
        LinkedList(listOf(Color.GREEN))
    )

    private val pieChart1 = PieChart.createPieChart()

    @Test
    fun getEditorFlow() = runBlockingTest {

        val results = BlockingFlowCollector<Editor?>()

        cache.update(editor1)
        cache.update(editor2)

        val job = launch {
            cache.getEditorFlow().collect(results)
        }

        assertEquals(1, results.size())
        assertEquals(editor2, results.nextValue())

        cache.update(editor1)
        assertEquals(2, results.size())
        assertEquals(editor1, results.nextValue())
        assertTrue(uncaughtExceptions.isEmpty())

        cache.close()
        job.cancelAndJoin()
    }

    @Test
    fun updateNonBlocking() = runBlockingTest {
        cache.putChart(pieChart1)
        cache.update(editor1)
    }
}