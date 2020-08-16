/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data

import com.stokerapps.chartmaker.domain.Editor
import com.stokerapps.chartmaker.domain.PieChart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

// TODO: Consider removing in-memory cache, it's only ~20ms faster
class Cache {

    private var pieChart: PieChart? = null
    private val editorFlow = MutableStateFlow<Editor?>(null)

    fun close() {
    }

    fun getChart(id: UUID): PieChart? {
        return if (pieChart?.id == id) pieChart else null
    }

    fun putChart(chart: PieChart?) {
        pieChart = chart
    }

    fun delete(chart: PieChart) {
        if (pieChart?.id == chart.id) {
            pieChart = null
        }
    }

    fun deleteByIds(ids: Collection<UUID>) {
        if (ids.contains(pieChart?.id)) {
            pieChart = null
        }
    }

    fun getEditorFlow(): StateFlow<Editor?> = editorFlow

    fun update(editor: Editor) {
        editorFlow.value = editor
    }
}