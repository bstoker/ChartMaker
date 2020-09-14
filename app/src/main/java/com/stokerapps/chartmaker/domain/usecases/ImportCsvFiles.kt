/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.domain.usecases

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.stokerapps.chartmaker.common.removeExtension
import com.stokerapps.chartmaker.data.files.CsvReader
import com.stokerapps.chartmaker.domain.ChartRepository
import com.stokerapps.chartmaker.domain.FileManager
import com.stokerapps.chartmaker.domain.PieChart
import com.stokerapps.chartmaker.domain.PieChartEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal

class ImportCsvFiles(
    private val scope: CoroutineScope,
    private val fileManager: FileManager,
    private val files: List<String>,
    private val repository: ChartRepository
) {
    interface State
    object Idle : State
    object Completed : State
    inner class Running(
        val completed: List<String> = this@ImportCsvFiles.completed,
        val failures: List<Pair<String, Throwable>> = this@ImportCsvFiles.failures,
        val total: Int = this@ImportCsvFiles.total
    ) : State

    private val _state = MutableLiveData<State>(Idle)
    private val reader = CsvReader()
    private val completed: MutableList<String> = mutableListOf()
    private val failures: MutableList<Pair<String, Throwable>> = mutableListOf()

    val state: LiveData<State> = _state
    val total = files.size

    fun execute() {
        scope.launch {

            setState(Running())

            files.forEach { file ->
                runCatching {
                    import(file)

                }.onFailure {
                    Timber.e(it)
                    failures.add(file to it)

                }.onSuccess { chart ->
                    repository.store(chart)
                    completed.add(file)
                }
                setState(Running())
            }
            setState(Completed)
        }
    }

    private fun import(file: String): PieChart {

        val dataList = fileManager.read(file).use {
            val mimeType = fileManager.getMimeType(file)
            reader.readAll(it, mimeType)
        }

        val description = fileManager.getMetaData(file).displayName?.removeExtension().toString()

        return when {
            dataList.isEmpty() -> PieChart(name = description)
            dataList.first() !is List<*> -> {
                val label = dataList.getString(0, "")
                val value = dataList.toBigDecimal(1, PieChartEntry.defaultValue)
                PieChart(
                    entries = listOf(PieChartEntry(label = label, value = value)),
                    name = description
                )
            }
            else -> {
                val entries = mutableListOf<PieChartEntry>()
                for (data in dataList) {
                    if (data is List<*>) {
                        val label = data.getString(0, "")
                        val value = data.toBigDecimal(1, PieChartEntry.defaultValue)
                        entries.add(PieChartEntry(label = label, value = value))
                    }
                }
                PieChart(entries = entries, name = description)
            }
        }
    }

    private fun List<*>.getString(index: Int, default: String): String {
        return runCatching {
            this[index] as String

        }.getOrElse {
            Timber.e(it)
            default
        }
    }

    private fun List<*>.toBigDecimal(index: Int, default: BigDecimal): BigDecimal {
        return runCatching {
            BigDecimal(this[index] as String)

        }.getOrElse {
            Timber.e(it)
            default
        }
    }

    private fun setState(state: State) {
        _state.postValue(state)
    }
}