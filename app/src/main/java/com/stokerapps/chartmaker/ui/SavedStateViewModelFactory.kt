/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

@file:Suppress("UNCHECKED_CAST")

package com.stokerapps.chartmaker.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.stokerapps.chartmaker.domain.ChartRepository
import com.stokerapps.chartmaker.domain.EditorRepository
import com.stokerapps.chartmaker.ui.piechart.PieChartViewModel
import com.stokerapps.chartmaker.ui.piechart.properties.PieChartPropertiesViewModel
import kotlinx.coroutines.CoroutineScope


class SavedStateViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val applicationScope: CoroutineScope,
    private val chartRepository: ChartRepository,
    private val editorRepository: EditorRepository
) : AbstractSavedStateViewModelFactory(owner, null) {

    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T =
        with(modelClass) {
            when {
                isAssignableFrom(PieChartViewModel::class.java) ->
                    PieChartViewModel(
                        handle,
                        applicationScope,
                        chartRepository,
                        editorRepository
                    )
                isAssignableFrom(PieChartPropertiesViewModel::class.java) ->
                    PieChartPropertiesViewModel(
                        handle,
                        applicationScope,
                        chartRepository,
                        editorRepository
                    )
                else -> throw IllegalArgumentException("Cannot create ViewModel for ${modelClass.name}")
            }
        } as T
}