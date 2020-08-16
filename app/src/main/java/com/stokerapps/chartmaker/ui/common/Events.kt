/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common

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