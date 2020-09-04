/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.stokerapps.chartmaker.domain.ChartRepository
import com.stokerapps.chartmaker.domain.EditorRepository
import com.stokerapps.chartmaker.domain.FileManager
import com.stokerapps.chartmaker.domain.Resources
import com.stokerapps.chartmaker.ui.common.color_picker.ColorPickerViewModel
import com.stokerapps.chartmaker.ui.common.export_dialog.ExportViewModel
import com.stokerapps.chartmaker.ui.explorer.ExplorerViewModel
import com.stokerapps.chartmaker.ui.explorer.ExplorerViewModelImpl
import kotlinx.coroutines.CoroutineScope

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val applicationScope: CoroutineScope,
    private val chartRepository: ChartRepository,
    private val editorRepository: EditorRepository,
    private val fileManager: FileManager,
    private val resources: Resources
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        with(modelClass) {
            when {
                isAssignableFrom(ColorPickerViewModel::class.java) ->
                    ColorPickerViewModel(editorRepository)
                isAssignableFrom(ExplorerViewModel::class.java) ->
                    ExplorerViewModelImpl(
                        applicationScope,
                        fileManager,
                        chartRepository,
                        editorRepository
                    )
                isAssignableFrom(ExportViewModel::class.java) ->
                    ExportViewModel(applicationScope, chartRepository, fileManager, resources)
                else ->
                    throw IllegalArgumentException("Cannot create ViewModel for ${modelClass.name}")
            }
        } as T
}