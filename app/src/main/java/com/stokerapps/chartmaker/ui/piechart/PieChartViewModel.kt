/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.piechart

import android.os.ParcelUuid
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.stokerapps.chartmaker.domain.*
import com.stokerapps.chartmaker.domain.usecases.DeleteEntry
import com.stokerapps.chartmaker.ui.common.*
import kotlinx.coroutines.CoroutineScope
import com.stokerapps.chartmaker.common.AppDispatchers as Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class PieChartViewModel(
    private val savedState: SavedStateHandle,
    private val applicationScope: CoroutineScope,
    private val repository: ChartRepository,
    private val editorRepository: EditorRepository
) : ViewModel() {

    companion object {
        private const val CHART_ID = "chartId"
        private const val ENTRY_ID = "entryId"
    }

    private val chart: PieChart?
        get() = when (val state = viewState.value) {
            is Loaded -> state.chart
            is Empty -> state.chart
            else -> null
        }

    private var deleteEntryAction: DeleteEntry? = null

    private var entryId: Int?
        get() = savedState.get(ENTRY_ID)
        set(value) {
            value?.let { savedState.set(ENTRY_ID, it) } ?: savedState.remove<Int>(ENTRY_ID)
        }

    private var entry: PieChartEntry?
        get() = chart?.entries?.find { it.id == entryId }
        set(value) {
            entryId = value?.id
        }

    private val chartIdStateFlow = MutableStateFlow(chartId)

    var chartId: UUID?
        get() = savedState.get<ParcelUuid>(CHART_ID)?.uuid
        private set(value) {
            value?.let { savedState.set(CHART_ID, ParcelUuid(it)) }
                ?: savedState.remove<ParcelUuid>(CHART_ID)
        }

    private val editorFlow = editorRepository.getEditorFlow()
        .mapNotNull { it.getOrNull() }

    val observableEditor = editorFlow
        .asLiveData(viewModelScope.coroutineContext)

    val events = LiveEvent<Event>()

    val viewState = chartIdStateFlow
        .flatMapLatest { chartId ->
            if (chartId == null) {
                flow { emit(Loading) }
            } else {
                repository.getPieChartFlow(chartId)
                    .conflate()
                    .combine(editorFlow) { result, editor ->
                        when (val chart = result.getOrNull()) {
                            null -> ErrorLoading(result.exceptionOrNull())
                            else -> if (chart.isEmpty()) Empty(chart) else Loaded(
                                chart,
                                editor.isEditSidebarExpanded
                            )
                        }
                    }
                    .onStart { emit(Loading) }
            }
        }
        .flowOn(Dispatchers.Default)
        .distinctUntilChanged()
        .asLiveData(viewModelScope.coroutineContext)

    fun editChart() {
        applicationScope.launch {
            chart?.let { chart ->
                if (chart.isEmpty()) {
                    editorRepository.store(
                        getEditor().copy(
                            selectedEditTab = Editor.EditTab.ENTRIES
                        )
                    )
                }
                events.postValue(NavigateToProperties)
            }
        }
    }

    fun isSidebarExpanded() = when (val state = viewState.value) {
        is Loaded -> state.isEditSidebarExpanded
        else -> false
    }

    fun setSidebarExpanded(isSidebarExpanded: Boolean) {
        applicationScope.launch {
            editorRepository.store(
                getEditor().copy(
                    isEditSidebarExpanded = isSidebarExpanded
                )
            )
        }
    }

    fun setSelectedTab(selectedTab: Editor.EditTab) {
        applicationScope.launch {
            editorRepository.store(
                getEditor().copy(
                    selectedEditTab = selectedTab
                )
            )
        }
    }

    fun show(id: UUID?) {
        chartIdStateFlow.value = id
        chartId = id
    }

    fun delete(entry: PieChartEntry) {
        chart?.let { chart ->
            deleteEntryAction = DeleteEntry(
                entry,
                chart,
                repository,
                applicationScope
            ).also { action ->
                action.execute()
            }
            events.postValue(ShowUndoDeleteMessage())
        }
    }

    fun undoDelete() {
        deleteEntryAction?.undo()
    }

    fun addNewEntry() {
        applicationScope.launch {
            chart?.let { chart ->
                val newEntry = PieChartEntry(
                    value = chart.getAverageValue() ?: PieChartEntry.defaultValue
                )
                repository.store(
                    chart.copy(
                        dateModified = Date(),
                        entries = (chart.entries + newEntry).toMutableList(),
                        version = chart.version + 1
                    )
                )
                delay(10)
                events.postValue(ScrollToLastEntry)
            }
        }
    }

    fun updateEntryColor(color: Int) {
        entry?.copy(color = color)?.let { entry ->
            chart?.let { chart ->
                update(chart.entries
                    .toMutableList()
                    .apply {
                        val index = indexOfFirst { it.id == entry.id }
                        set(index, entry)
                    })
            }
        }
    }

    fun update(entry: PieChartEntry) {
        applicationScope.launch {
            chart?.let { chart ->
                repository.store(
                    // TODO: make entries immutable
                    chart.copy(
                        dateModified = Date(),
                        entries = chart.entries.apply {
                            val index = indexOfFirst { it.id == entry.id }
                            set(index, entry)
                        },
                        version = chart.version + 1
                    )
                )
            }
        }
    }

    fun update(entries: List<PieChartEntry>) {
        applicationScope.launch {
            chart?.let { chart ->
                repository.store(
                    chart.update(entries = entries.toMutableList())
                )
            }
        }
    }

    fun changeColor(entry: PieChartEntry) {
        this@PieChartViewModel.entry = entry
        events.value =
            ShowColorPicker
    }

    private suspend fun getEditor() = observableEditor.value ?: editorRepository.getEditor()

}