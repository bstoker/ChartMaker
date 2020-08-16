/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import timber.log.Timber

object LogLifecycleCallbacks : Application.ActivityLifecycleCallbacks, FragmentManager.FragmentLifecycleCallbacks() {

    private fun currentMethodName(): String? {
        return Throwable().stackTrace[1].methodName
    }

    override fun onActivityPaused(activity: Activity?) {
        log(
            currentMethodName(),
            activity
        )
    }

    override fun onActivityResumed(activity: Activity?) {
        log(
            currentMethodName(),
            activity
        )
    }

    override fun onActivityStarted(activity: Activity?) {
        log(
            currentMethodName(),
            activity
        )
    }

    override fun onActivityDestroyed(activity: Activity?) {
        log(
            currentMethodName(),
            activity
        )
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
        log(
            currentMethodName(),
            activity
        )
    }

    override fun onActivityStopped(activity: Activity?) {
        log(
            currentMethodName(),
            activity
        )
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        log(
            currentMethodName(),
            activity
        )
    }

    override fun onFragmentActivityCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        log(
            currentMethodName(),
            f
        )
    }

    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        log(
            currentMethodName(),
            f
        )
    }

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        log(
            currentMethodName(),
            f
        )
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        log(
            currentMethodName(),
            f
        )
    }

    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        log(
            currentMethodName(),
            f
        )
    }

    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        log(
            currentMethodName(),
            f
        )
    }

    override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) {
        log(
            currentMethodName(),
            f
        )
    }

    override fun onFragmentPreCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        log(
            currentMethodName(),
            f
        )
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        log(
            currentMethodName(),
            f
        )
    }

    override fun onFragmentSaveInstanceState(fm: FragmentManager, f: Fragment, outState: Bundle) {
        log(
            currentMethodName(),
            f
        )
    }

    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
        log(
            currentMethodName(),
            f
        )
    }

    override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
        log(
            currentMethodName(),
            f
        )
    }

    override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
        log(
            currentMethodName(),
            f
        )
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        log(
            currentMethodName(),
            f
        )
    }

    private fun log(lifecycle: String?, tag: Any?) {
        val maxTagLength = 50
        Timber.d(
            "$tag".let {
                if (it.length < maxTagLength) it.padEnd(maxTagLength)
                else it.substring(0, maxTagLength)
            },
            "$lifecycle called"
        )
    }

}