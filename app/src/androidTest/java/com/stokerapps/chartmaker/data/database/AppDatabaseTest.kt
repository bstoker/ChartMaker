/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data.database

import android.graphics.Color
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.stokerapps.chartmaker.domain.Editor
import com.stokerapps.chartmaker.domain.PieChart
import com.stokerapps.chartmaker.test.BlockingFlowCollector
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

@FlowPreview
@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class AppDatabaseTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val data = TestData()
    private val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    private val scope = TestCoroutineScope(Job())

    @Before
    fun setUp() {
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        database.close()
        scope.cancel()
        scope.cleanupTestCoroutines()
    }

    @Test
    fun performChartCreateUpdateAndDeleteInChannel() = runBlocking {

        val chart = data.pieChart
        val chartUpdated = chart.update()
        val channel = database.getChartFlow(chart.id)
            .produceIn(this)

        assertNull(channel.receive())

        database.updateOrCreate(chart)
        assertEquals(chart, channel.receive())

        database.updateOrCreate(chartUpdated)
        assertEquals(chartUpdated, channel.receive())

        database.delete(chart)
        assertNull(channel.receive())
        assertTrue(channel.isEmpty)
        assertTrue(scope.uncaughtExceptions.isEmpty())

        channel.cancel()
    }

    @Test
    fun performChartCreateUpdateAndDelete() = runBlocking {

        val chart = data.pieChart
        val chartUpdated = chart.update()
        val results = BlockingFlowCollector<PieChart?>()

        scope.launch {
            database.getChartFlow(chart.id)
                .collect(results)
        }

        assertNull(results.nextValue())

        database.updateOrCreate(chart)
        assertEquals(chart, results.nextValue())

        database.updateOrCreate(chartUpdated)
        assertEquals(chartUpdated, results.nextValue())

        database.delete(chart)
        assertNull(results.nextValue())
        assertEquals(4, results.size())
        assertTrue(scope.uncaughtExceptions.isEmpty())
    }

    @Test
    fun performEditorCreateUpdateAndDelete() = runBlocking {

        val editor = data.editor
        val results = BlockingFlowCollector<Editor?>()

        scope.launch {
            database.getEditorFlow()
                .collect(results)
        }

        assertNull(results.nextValue())

        database.updateOrCreate(editor)
        assertEquals(editor, results.nextValue())

        editor.addColor(Color.MAGENTA)
        database.updateOrCreate(editor)
        assertEquals(editor, results.nextValue())
        assertTrue(editor.colors.contains(Color.MAGENTA))

        database.delete(data.editor)
        assertNull(results.nextValue())
        assertEquals(4, results.size())
        assertTrue(scope.uncaughtExceptions.isEmpty())
    }
}