/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

@file:Suppress("ConstantConditionIf")

package com.stokerapps.chartmaker.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.stokerapps.chartmaker.BuildConfig
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.ui.common.LogLifecycleCallbacks
import org.koin.core.KoinComponent
import org.koin.core.inject


class NavigationActivity : AppCompatActivity(), KoinComponent {

    private val navController by lazy { findNavController(R.id.fragment_container_view) }
    private val appBarConfiguration by lazy { AppBarConfiguration(navController.graph) }

    private val fragmentFactory by inject<FragmentFactory>()

    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = fragmentFactory
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        if (BuildConfig.DEBUG_LIFECYCLE_CALLBACKS) {
            application.registerActivityLifecycleCallbacks(LogLifecycleCallbacks)
            supportFragmentManager.registerFragmentLifecycleCallbacks(LogLifecycleCallbacks, true)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (BuildConfig.DEBUG_LIFECYCLE_CALLBACKS) {
            application.unregisterActivityLifecycleCallbacks(LogLifecycleCallbacks)
            supportFragmentManager.unregisterFragmentLifecycleCallbacks(LogLifecycleCallbacks)
        }
    }

    override fun onSupportNavigateUp() =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
}