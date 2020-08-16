/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.test

import androidx.lifecycle.*

@Suppress("unused")
open class TestLifecycleOwner : LifecycleOwner {

    private val lifecycle: LifecycleRegistry by lazy {
        LifecycleRegistry(this)
    }

    fun startActivity() {
        onCreate()
        onStart()
        onResume()
    }

    fun finishActivity() {
        onPause()
        onStop()
        onDestroy()
    }

    fun rotateScreen() {
        finishActivity()
        startActivity()
    }

    open fun onCreate() {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    open fun onStart() {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    open fun onResume() {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    open fun onPause() {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    open fun onStop() {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    open fun onDestroy() {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycle
    }
}