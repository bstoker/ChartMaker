@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.stokerapps.chartmaker.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Custom [Dispatchers] class that allows settings IO and Default dispatchers too for Espresso tests.
 *
 * [source](https://github.com/Kotlin/kotlinx.coroutines/issues/242#issuecomment-561503344)
 */
object AppDispatchers {

    var Main: CoroutineDispatcher = Dispatchers.Main
    var IO: CoroutineDispatcher = Dispatchers.IO
    var Default: CoroutineDispatcher = Dispatchers.Default

    fun resetMain() {
        Main = Dispatchers.Main
    }

    fun resetIO() {
        IO = Dispatchers.IO
    }

    fun resetDefault() {
        Default = Dispatchers.Default
    }

    fun resetAll() {
        resetMain()
        resetIO()
        resetDefault()
    }
}