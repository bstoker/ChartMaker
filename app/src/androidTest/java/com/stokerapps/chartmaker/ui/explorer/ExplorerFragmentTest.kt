/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.explorer

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.stokerapps.chartmaker.ChartMakerApp
import com.stokerapps.chartmaker.DispatcherIdlerRule
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.domain.PieChart
import com.stokerapps.chartmaker.domain.Sort
import com.stokerapps.chartmaker.mockPagedList
import com.stokerapps.chartmaker.ui.FragmentFactory
import com.stokerapps.chartmaker.ui.common.LiveEvent
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

@RunWith(AndroidJUnit4::class)
class ExplorerFragmentTest : KoinComponent {

    private val viewModel = object : ExplorerViewModel() {

        private val _viewState = MutableLiveData<ViewState>()

        override val events: LiveEvent<Any> = LiveEvent()
        override val viewState: LiveData<ViewState> = _viewState

        override fun createPieChart(chart: PieChart) {
        }

        fun setState(state: ViewState) {
            _viewState.postValue(state)
        }

        override fun sort(newSort: Sort) {
        }

        override fun delete(chartIds: Collection<UUID>) {
        }

        override fun undoDelete() {
        }

        override fun import(files: List<String>) {
        }
    }
    private val fragmentFactory by inject<FragmentFactory>()

    @get:Rule
    var dispatcherIdlingRule = DispatcherIdlerRule()

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testViewStates() = runBlocking {

        ApplicationProvider.getApplicationContext<ChartMakerApp>()

        val scenario = launchFragmentInContainer<ExplorerFragment>(
            themeResId = R.style.AppTheme,
            factory = fragmentFactory
        )
        scenario.onFragment { fragment ->
            (fragment.activity as? AppCompatActivity)?.let { activity ->
                activity.setSupportActionBar(Toolbar(activity))
            }
        }

        // viewModel.setState(Loading)
        onView(withId(R.id.loadingIndicator)).check(matches(isCompletelyDisplayed()))

        viewModel.setState(Empty(mockPagedList(), Sort.default))

        // TODO: onCreateOptionsMenu is not called, so ExplorerFragment doesn't use the live data
        /*scenario.onFragment { fragment ->
            val noChartsView = fragment.binding.noCharts.noChartsView
            waitUntil { noChartsView.visibility == View.VISIBLE }
        }
        onView(withId(R.id.no_charts)).check(matches(isCompletelyDisplayed()))

        viewModel.setState(Loaded(mockPagedList(listOf(PieChart.createPieChart())), Sort.default))
        onView(withText(R.string.no_description)).check(matches(isCompletelyDisplayed()))*/

        Unit
    }
}