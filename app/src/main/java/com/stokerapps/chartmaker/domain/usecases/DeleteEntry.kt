/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.domain.usecases

import com.stokerapps.chartmaker.domain.ChartRepository
import com.stokerapps.chartmaker.domain.PieChart
import com.stokerapps.chartmaker.domain.PieChartEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DeleteEntry(
    private val entry: PieChartEntry,
    private val chart: PieChart,
    private val repository: ChartRepository,
    private val scope: CoroutineScope
) {
    var entries: List<PieChartEntry>? = null
    private var job: Job? = null

    fun execute() {
        if (chart.entries.contains(entry)) {
            job = scope.launch {
                repository.delete(entry, chart)
            }
        }
    }

    fun undo() {
        job?.let {
            it.cancel()
            scope.launch {
                with(chart.entries) {
                    val index = indexOf(entry)
                    repository.store(subList(index, size), chart)
                }
            }
        }
    }
}