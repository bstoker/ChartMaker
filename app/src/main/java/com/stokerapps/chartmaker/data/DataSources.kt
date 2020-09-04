/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data

import androidx.paging.DataSource
import com.stokerapps.chartmaker.domain.*
import kotlinx.coroutines.flow.Flow
import java.util.*

interface ChartDataSource {

    fun getAllChartsFlow(): Flow<List<PieChart>>
    fun getAllChartsPaged(sort: Sort): DataSource.Factory<Int, Chart>
    suspend fun getChart(chartId: UUID): PieChart?
    fun getChartFlow(chartId: UUID): Flow<PieChart?>

    suspend fun updateOrCreate(chart: PieChart)
    suspend fun updateOrCreate(entries: List<PieChartEntry>, chart: PieChart)

    suspend fun delete(chart: PieChart)
    suspend fun delete(charts: Collection<UUID>)
    suspend fun delete(entry: PieChartEntry, chart: PieChart)
}

interface EditorDataSource {

    suspend fun getEditor(): Editor?
    fun getEditorFlow(): Flow<Editor?>

    suspend fun updateOrCreate(editor: Editor)
}