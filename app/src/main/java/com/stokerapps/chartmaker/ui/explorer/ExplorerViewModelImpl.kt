/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.explorer

import androidx.lifecycle.*
import androidx.paging.Config
import androidx.paging.toLiveData
import com.stokerapps.chartmaker.domain.*
import com.stokerapps.chartmaker.domain.usecases.DeleteCharts
import com.stokerapps.chartmaker.domain.usecases.ImportCsvFiles
import com.stokerapps.chartmaker.ui.common.LiveEvent
import com.stokerapps.chartmaker.ui.common.ShowUndoDeleteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import java.util.*
import com.stokerapps.chartmaker.common.AppDispatchers as Dispatchers


class ExplorerViewModelImpl(
    private val applicationScope: CoroutineScope,
    private val fileManager: FileManager,
    private val repository: ChartRepository,
    private val editorRepository: EditorRepository
) : ExplorerViewModel() {

    private val charts: List<Chart>?
        get() = when (val state = viewState.value) {
            is Loaded -> state.charts.toList()
            else -> null
        }

    private val config = Config(
        pageSize = 15,
        prefetchDistance = 8,
        initialLoadSizeHint = 15,
        enablePlaceholders = true
    )

    private var deleteChartsAction: DeleteCharts? = null

    private val editor = editorRepository.getEditorFlow()
        .mapNotNull { it.getOrNull() }
        .flowOn(Dispatchers.Default)
        .asLiveData(viewModelScope.coroutineContext)

    override val events = LiveEvent<Any>()

    override val viewState = liveData(viewModelScope.coroutineContext) {
        emit(Loading)
        emitSource(
            editor
                .map { it.sort }
                .distinctUntilChanged()
                .switchMap { sort ->
                    repository.getChartsPaged(sort)
                        .toLiveData(config)
                        .map { charts ->
                            if (charts.isEmpty()) {
                                Empty(charts, sort)
                            } else {
                                Loaded(charts, sort)
                            }
                        }
                }
        )
    }.distinctUntilChanged()

    override fun createPieChart(chart: PieChart) {
        store(chart)
    }

    override fun sort(newSort: Sort) {
        editor.value?.let { editor ->
            applicationScope.launch {
                editorRepository.store(
                    editor.copy(sort = newSort)
                )
            }
        }
    }

    override fun delete(chartIds: Collection<UUID>) {
        charts?.let { charts ->
            charts
                .filter { chart -> chart.id in chartIds }
                .takeIf { it.isNotEmpty() }
                ?.let { chartsToDelete ->
                    deleteChartsAction =
                        DeleteCharts(
                            chartsToDelete,
                            repository,
                            applicationScope
                        ).also { action ->
                            action.execute()
                        }
                    events.postValue(ShowUndoDeleteMessage(chartsToDelete.size))
                }
        }
    }

    override fun undoDelete() {
        deleteChartsAction?.undo()
    }

    override fun import(files: List<String>) {
        ImportCsvFiles(
            applicationScope,
            fileManager,
            files,
            repository
        ).also {
            events.postValue(it)
            it.execute()
        }
    }

    private fun store(chart: PieChart) = applicationScope.launch {
        repository.store(chart)
    }
}