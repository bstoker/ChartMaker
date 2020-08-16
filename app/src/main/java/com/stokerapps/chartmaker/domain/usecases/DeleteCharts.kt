/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.domain.usecases

import com.stokerapps.chartmaker.domain.Chart
import com.stokerapps.chartmaker.domain.ChartRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DeleteCharts(
    private val charts: List<Chart>,
    private val repository: ChartRepository,
    private val scope: CoroutineScope
) {

    private var job: Job? = null

    fun execute() {
        job = scope.launch {
            repository.delete(charts.map { chart -> chart.id })
        }
    }

    fun undo() {
        job?.let {
            it.cancel()
            scope.launch {
                charts.forEach { chart -> repository.store(chart) }
            }
        }
    }
}