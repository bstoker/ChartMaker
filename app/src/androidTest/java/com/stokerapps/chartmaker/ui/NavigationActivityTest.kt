/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui

import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.stokerapps.chartmaker.DispatcherIdlerRule
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.data.database.AppDatabase
import com.stokerapps.chartmaker.ui.explorer.ExplorerFragment
import com.stokerapps.chartmaker.ui.piechart.PieChartFragment
import com.stokerapps.chartmaker.waitUntil
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.KoinComponent
import org.koin.core.get


@RunWith(AndroidJUnit4::class)
class NavigationActivityTest : KoinComponent {

    @get:Rule
    var activityRule: ActivityScenarioRule<NavigationActivity> =
        ActivityScenarioRule(NavigationActivity::class.java)

    @get:Rule
    var dispatcherIdlingRule = DispatcherIdlerRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val database = get<AppDatabase>()

    private val explorerFragment: ExplorerFragment get() = getCurrentFragment()
    private val pieChartFragment: PieChartFragment get() = getCurrentFragment()

    @Before
    fun emptyDatabase() {
        database.clearAllTables()
    }

    @After
    fun deleteDatabase() {
        context.deleteDatabase(database.openHelper.databaseName)
    }

    @Test
    fun addNewChart() {

        val noChartsView = explorerFragment.binding.noCharts.noChartsView
        waitUntil { noChartsView.visibility == View.VISIBLE }

        onView(withId(R.id.no_charts)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.add)).perform(click())
        onView(withText(R.string.pie_chart)).perform(click())

        val emptyChartView = pieChartFragment.binding.emptyChart.root
        waitUntil { emptyChartView.visibility == View.VISIBLE }
    }

    @Suppress("unused")
    private fun <T : View> findViewById(@IdRes resId: Int): T {
        lateinit var view: T
        activityRule.scenario.onActivity {
            (it.findViewById<T>(resId))?.let { v ->
                view = v
            }
                ?: throw AssertionError("View ${context.resources.getResourceEntryName(resId)} not found!")
        }
        return view
    }

    private inline fun <reified T : Fragment> getCurrentFragment(): T {
        lateinit var fragment: T
        activityRule.scenario.onActivity {
            val current = it.supportFragmentManager.primaryNavigationFragment
                ?.childFragmentManager
                ?.primaryNavigationFragment

            fragment =
                if (current is T) current else throw AssertionError("Could not find fragment!")
        }
        return fragment
    }

}