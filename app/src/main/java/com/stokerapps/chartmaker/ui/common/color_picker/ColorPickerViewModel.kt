/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common.color_picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.stokerapps.chartmaker.domain.EditorRepository
import com.stokerapps.chartmaker.common.AppDispatchers as Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

class ColorPickerViewModel(private val repository: EditorRepository) : ViewModel() {

    val editor = repository.getEditorFlow()
        .mapNotNull { it.getOrNull() }
        .flowOn(Dispatchers.Default)
        .asLiveData()

    fun saveColor(color: Int) {
        editor.value?.let { editor ->
            editor.addColor(color)
            GlobalScope.launch(Dispatchers.IO) {
                repository.store(editor)
            }
        }
    }
}