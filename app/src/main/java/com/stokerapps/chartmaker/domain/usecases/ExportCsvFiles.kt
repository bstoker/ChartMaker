/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.domain.usecases

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.common.Timestamp
import com.stokerapps.chartmaker.data.files.CsvWriter
import com.stokerapps.chartmaker.domain.ChartRepository
import com.stokerapps.chartmaker.domain.FileManager
import com.stokerapps.chartmaker.domain.PieChart
import com.stokerapps.chartmaker.domain.Resources
import com.stokerapps.chartmaker.domain.Delimiter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class ExportCsvFiles(
    private val scope: CoroutineScope,
    private val fileManager: FileManager,
    private val files: Collection<UUID>,
    private val uriString: String,
    delimiter: Delimiter,
    private val repository: ChartRepository,
    private val resources: Resources
) {
    companion object {
        const val EXTENSION_CSV = ".csv"
        const val EXTENSION_TSV = ".tsv"

        const val MIME_TYPE_CSV = "text/csv"
        const val MIME_TYPE_TSV = "text/tsv"
    }

    interface State
    object Idle : State
    object Completed : State
    inner class Running(
        val completed: List<Pair<UUID, String>> = this@ExportCsvFiles.completed,
        val failures: List<Triple<UUID, String, Throwable>> = this@ExportCsvFiles.failures,
        val total: Int = this@ExportCsvFiles.total
    ) : State

    private val _state = MutableLiveData<State>(Idle)
    private val extension = when (delimiter) {
        Delimiter.TAB -> EXTENSION_TSV
        else -> EXTENSION_CSV
    }
    private val mimeType = when (delimiter) {
        Delimiter.TAB -> MIME_TYPE_TSV
        else -> MIME_TYPE_CSV
    }
    private val multipleFiles = files.size > 1
    private val writer = CsvWriter(delimiter = delimiter)
    private val completed: MutableList<Pair<UUID, String>> = mutableListOf()
    private val failures: MutableList<Triple<UUID, String, Throwable>> = mutableListOf()

    val state: LiveData<State> = _state
    val total = files.size

    fun execute() {
        scope.launch {

            setState(Running())

            files.forEach { chartId ->
                runCatching {
                    val chart = repository.getPieChart(chartId)
                    if (chart == null) {
                        throw IllegalArgumentException("Could not fetch chart $chartId!")

                    } else {
                        if (multipleFiles) {
                            fileManager.write(uriString, createFilename(chart), mimeType)

                        } else {
                            fileManager.write(uriString)

                        }.use {
                            writer.write(chart, it)
                        }
                    }
                }.onFailure { error ->
                    Timber.e(error)
                    failures.add(Triple(chartId, uriString, error))

                }.onSuccess {
                    completed.add(chartId to uriString)
                }
                setState(Running())
            }
            setState(Completed)
        }
    }

    private fun createFilename(chart: PieChart): String {
        val filename = when {
            chart.name.isNullOrEmpty() -> "${resources.getString(R.string.chart)}_$Timestamp"
            else -> chart.name
        }
        return "$filename$extension"
    }

    private fun setState(state: State) {
        _state.postValue(state)
    }
}