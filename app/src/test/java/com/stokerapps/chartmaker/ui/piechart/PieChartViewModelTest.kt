/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.piechart

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.stokerapps.chartmaker.data.ChartRepositoryImpl
import com.stokerapps.chartmaker.data.EditorRepositoryImpl
import com.stokerapps.chartmaker.data.TestData
import com.stokerapps.chartmaker.domain.PieChart
import com.stokerapps.chartmaker.domain.PieChartEntry
import com.stokerapps.chartmaker.ui.common.*
import com.stokerapps.chartmaker.test.BlockingObserver
import com.stokerapps.chartmaker.test.TestLifecycleOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TestRule
import java.math.BigDecimal

@ExperimentalCoroutinesApi
@FlowPreview
@ObsoleteCoroutinesApi
class PieChartViewModelTest {

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

    @Test
    fun testViewState() = runBlockingTest {

        val data = TestData()
        val cache = data.cache
        val chart = PieChart()
        val database = data.database
        val repository = ChartRepositoryImpl(cache, database)
        val editorRepository = EditorRepositoryImpl(cache, database)
        val handle = SavedStateHandle()

        val eventObserver = BlockingObserver<Event>()
        val stateObserver = BlockingObserver<ViewState>()
        val viewModel = PieChartViewModel(
            handle,
            CoroutineScope(coroutineContext + Dispatchers.Default),
            repository,
            editorRepository
        )
        val lifecycleOwner = object : TestLifecycleOwner() {
            override fun onCreate() {
                super.onCreate()
                viewModel.events.observe(this, eventObserver)
                viewModel.viewState.observe(this, stateObserver)
            }
        }

        assertNull(viewModel.events.value)
        assertNull(viewModel.viewState.value)

        lifecycleOwner.startActivity()
        assertEquals(Loading, stateObserver.nextValue(500L))

        repository.store(chart)
        viewModel.show(chart.id)
        assertEquals(Empty(chart), stateObserver.nextValue())

        val newEntries = listOf(
            PieChartEntry(label = "1", value = BigDecimal(40)),
            PieChartEntry(label = "2", value = BigDecimal(60)),
            PieChartEntry(label = "3", value = BigDecimal(20))
        )
        viewModel.update(newEntries)
        when (val state = stateObserver.nextValue()) {
            is Loaded -> assertEquals(newEntries, state.chart.entries)
            else -> fail("Wrong state: Expected Loaded")
        }

        viewModel.addNewEntry()
        val entry = when (val state = stateObserver.nextValue()) {
            is Loaded -> {
                assertEquals(4, state.chart.entries.size)
                state.chart.entries.last()
            }
            else -> throw AssertionError("Wrong state: Expected Loaded")
        }
        assertEquals(ScrollToLastEntry, eventObserver.nextValue())

        val newEntry = entry.copy(label = "4", color = DARK_ORANGE)
        viewModel.update(newEntry)
        when (val state = stateObserver.nextValue()) {
            is Loaded -> assertEquals(newEntry, state.chart.entries.last())
            else -> fail("Wrong state: Expected Loaded")
        }

        viewModel.changeColor(newEntry)
        assertEquals(ShowColorPicker, eventObserver.nextValue())
        val stateBeforeRotate = stateObserver.values().last()

        lifecycleOwner.rotateScreen()
        assertEquals(stateBeforeRotate, stateObserver.nextValue())

        viewModel.updateEntryColor(MOON_YELLOW)
        when (val state = stateObserver.nextValue(Long.MAX_VALUE)) {
            is Loaded -> assertEquals(
                newEntry.copy(color = MOON_YELLOW),
                state.chart.entries.find { it.id == newEntry.id }
            )
            else -> fail("Wrong state: Expected Loaded")
        }

        assertEquals(2, eventObserver.size())
        assertEquals(7, stateObserver.size())
        assertTrue(uncaughtExceptions.isEmpty())

        lifecycleOwner.finishActivity()
    }
}