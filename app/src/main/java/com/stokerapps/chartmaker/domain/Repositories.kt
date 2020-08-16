/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.domain

import androidx.paging.DataSource
import com.stokerapps.chartmaker.common.Result
import kotlinx.coroutines.flow.Flow
import java.util.*

interface ChartRepository {

    fun getChartsPaged(sort: Sort = Sort.default): DataSource.Factory<Int, Chart>

    fun getPieChartFlow(id: UUID): Flow<Result<PieChart>>

    suspend fun store(chart: Chart)

    suspend fun store(chart: PieChart)

    suspend fun store(entries: List<PieChartEntry>, chart: PieChart)

    suspend fun delete(entry: PieChartEntry, chart: PieChart)

    suspend fun delete(chart: PieChart)

    suspend fun delete(charts: Collection<UUID>)
}

interface EditorRepository {

    suspend fun getEditor(): Editor

    fun getEditorFlow(): Flow<Result<Editor>>

    suspend fun store(editor: Editor)
}