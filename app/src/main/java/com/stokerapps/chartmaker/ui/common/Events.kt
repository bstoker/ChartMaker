/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface Event

object NavigateToProperties : Event

object ScrollToLastEntry : Event

object ShowColorPicker : Event

object ShowCurrencyPicker : Event

data class ShowUndoDeleteMessage(val deleteCount: Int = 1) : Event

data class MaximumDonutRadiusReached(val maximum: Float) : Event

data class MinimumDonutRadiusReached(val minimum: Float) : Event

data class MaximumSliceSpaceReached(val maximum: Float) : Event

data class MinimumSliceSpaceReached(val minimum: Float) : Event

data class SaveChart(val uri: Uri, val mimeType: String) : Event {

    interface State
    class Idle : State
    class Running : State
    class Completed : State
    class Failed(val exception: Exception) : State

    private val _state = MutableLiveData<State>(Idle())
    val state: LiveData<State> = _state

    internal fun setState(state: State) {
        _state.postValue(state)
    }
}