/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.stokerapps.chartmaker.domain.ChartRepository
import com.stokerapps.chartmaker.domain.EditorRepository
import com.stokerapps.chartmaker.ui.common.color_picker.ColorPickerViewModel
import com.stokerapps.chartmaker.ui.explorer.ExplorerViewModel
import com.stokerapps.chartmaker.ui.explorer.ExplorerViewModelImpl
import kotlinx.coroutines.CoroutineScope

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val applicationScope: CoroutineScope,
    private val chartRepository: ChartRepository,
    private val editorRepository: EditorRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        with(modelClass) {
            when {
                isAssignableFrom(ColorPickerViewModel::class.java) ->
                    ColorPickerViewModel(editorRepository)
                isAssignableFrom(ExplorerViewModel::class.java) ->
                    ExplorerViewModelImpl(applicationScope, chartRepository, editorRepository)
                else ->
                    throw IllegalArgumentException("Cannot create ViewModel for ${modelClass.name}")
            }
        } as T
}