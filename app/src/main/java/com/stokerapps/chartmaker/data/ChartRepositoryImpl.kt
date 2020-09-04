/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data

import com.stokerapps.chartmaker.domain.*
import com.stokerapps.chartmaker.common.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import java.util.*
import kotlin.NoSuchElementException


class ChartRepositoryImpl(
    private val cache: Cache,
    private val database: ChartDataSource
) : ChartRepository {

    override fun getChartsPaged(sort: Sort) =
        database.getAllChartsPaged(sort)

    override suspend fun getPieChart(id: UUID) = database.getChart(id)

    override fun getPieChartFlow(id: UUID): Flow<Result<PieChart>> = flow {
        cache.getChart(id)?.let {
            emit(Result.success(it))
        }
        database.getChartFlow(id)
            .catch { emit(Result.failure<PieChart>(it)) }
            .collect { chart ->
                cache.putChart(chart)
                emit(
                    if (chart == null) Result.failure(NoSuchElementException()) else Result.success(
                        chart
                    )
                )
            }
    }

    override suspend fun store(chart: Chart) {
        when (chart) {
            is PieChart -> store(chart)
        }
    }

    override suspend fun store(chart: PieChart) {
        cache.putChart(chart)
        database.updateOrCreate(chart)
    }

    override suspend fun store(entries: List<PieChartEntry>, chart: PieChart) {
        database.updateOrCreate(entries, chart)
    }

    override suspend fun delete(entry: PieChartEntry, chart: PieChart) {
        database.delete(entry, chart)
    }

    override suspend fun delete(chart: PieChart) {
        cache.delete(chart)
        database.delete(chart)
    }

    override suspend fun delete(charts: Collection<UUID>) {
        cache.deleteByIds(charts)
        database.delete(charts)
    }
}