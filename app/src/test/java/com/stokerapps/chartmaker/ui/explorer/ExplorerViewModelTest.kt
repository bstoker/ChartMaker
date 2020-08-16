/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.explorer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.stokerapps.chartmaker.data.ChartRepositoryImpl
import com.stokerapps.chartmaker.data.EditorRepositoryImpl
import com.stokerapps.chartmaker.data.TestData
import com.stokerapps.chartmaker.test.BlockingObserver
import com.stokerapps.chartmaker.test.TestLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class ExplorerViewModelTest {

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
        val database = data.database
        val repository = ChartRepositoryImpl(cache, database)
        val editorRepository = EditorRepositoryImpl(cache, database)

        val lifecycleOwner = TestLifecycleOwner()
        val observer = BlockingObserver<ViewState>()
        val viewModel = ExplorerViewModelImpl(this, repository, editorRepository)

        viewModel.viewState.observe(lifecycleOwner, observer)
        assertNull(viewModel.viewState.value)

        lifecycleOwner.onStart()


        // Put editor and charts and check upon view state

        // TODO: mock DataSource.Factory in ChartRepository


        assertTrue(uncaughtExceptions.isEmpty())

        lifecycleOwner.onDestroy()
    }

}