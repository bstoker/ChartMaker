/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data

import android.graphics.Color
import androidx.paging.DataSource
import com.stokerapps.chartmaker.domain.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import java.io.Closeable
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

@ExperimentalCoroutinesApi
@FlowPreview
internal class TestData {

    interface ClosableChartDataSource : ChartDataSource, Closeable
    interface ClosableEditorDataSource : EditorDataSource, Closeable

    val cache = Cache()
    val database = TestDatabase(TestChartDataSource(), TestEditorDataSource())
    val databaseThrowingExceptions = TestDatabase(ErrorChartDataSource(), ErrorEditorDataSource())

    val chart1 = PieChart.createPieChart()

    val editor1 = Editor.default.copy(
        1,
        LinkedList(listOf(Color.RED))
    )
    val editor2 = Editor.default.copy(
        2,
        LinkedList(listOf(Color.WHITE))
    )

    internal class TestDatabase(
        private val chartDataSource: ClosableChartDataSource,
        private val editorDataSource: ClosableEditorDataSource
    ) : ClosableChartDataSource by chartDataSource, ClosableEditorDataSource by editorDataSource {

        override fun close() {
            chartDataSource.close()
            editorDataSource.close()
        }
    }

    internal class ErrorChartDataSource : ClosableChartDataSource {
        override fun getAllChartsFlow(): Flow<List<PieChart>> {
            throw IOException()
        }

        override fun getAllChartsPaged(sort: Sort): DataSource.Factory<Int, Chart> {
            throw IOException()
        }

        override fun getChartFlow(chartId: UUID): Flow<PieChart?> = flow {
            throw IOException()
        }

        override suspend fun updateOrCreate(chart: PieChart) {
            throw OutOfMemoryError()
        }

        override suspend fun updateOrCreate(entries: List<PieChartEntry>, chart: PieChart) {
            throw OutOfMemoryError()
        }

        override suspend fun delete(chart: PieChart) {
            throw IOException()
        }

        override suspend fun delete(charts: Collection<UUID>) {
            throw IOException()
        }

        override suspend fun delete(entry: PieChartEntry, chart: PieChart) {
            throw IOException()
        }

        override fun close() {}
    }

    internal class ErrorEditorDataSource : ClosableEditorDataSource {

        override suspend fun getEditor(): Editor? {
            throw IOException()
        }

        override fun getEditorFlow(): Flow<Editor?> = flow {
            throw IOException()
        }

        override suspend fun updateOrCreate(editor: Editor) {
            throw OutOfMemoryError()
        }

        override fun close() {}
    }

    internal class TestChartDataSource : ClosableChartDataSource {

        private val flows = HashMap<UUID, MutableStateFlow<PieChart?>>()

        private fun getChannel(id: UUID): MutableStateFlow<PieChart?> =
            flows[id] ?: MutableStateFlow<PieChart?>(null)
                .also { flows[id] = it }

        override fun getAllChartsFlow(): Flow<List<PieChart>> = flow {
            emit(flows.values.mapNotNull { it.value })
        }

        override fun getAllChartsPaged(sort: Sort): DataSource.Factory<Int, Chart> {
            TODO("Not yet implemented")
        }

        override fun getChartFlow(chartId: UUID): Flow<PieChart?> = getChannel(chartId)

        override suspend fun updateOrCreate(chart: PieChart) {
            getChannel(chart.id).value = chart
        }

        override suspend fun updateOrCreate(entries: List<PieChartEntry>, chart: PieChart) {
            getChannel(chart.id).value?.copy(
                entries = entries.toMutableList()
            )
        }

        override suspend fun delete(chart: PieChart) {
            getChannel(chart.id).value = null
        }

        override suspend fun delete(charts: Collection<UUID>) {
            flows.keys.filter { charts.contains(it) }.forEach { id ->
                getChannel(id).value = null
            }
        }

        override suspend fun delete(entry: PieChartEntry, chart: PieChart) {
            getChannel(chart.id).let { channel ->
                val entries = chart.entries
                    .toMutableList()
                    .apply { remove(entry) }

                channel.value = chart.copy(entries = entries)
            }
        }

        override fun close() {
        }
    }

    internal class TestEditorDataSource : ClosableEditorDataSource {

        private val flow = MutableStateFlow<Editor?>(null)

        override suspend fun getEditor() = flow.value

        override fun getEditorFlow(): Flow<Editor?> = flow

        override suspend fun updateOrCreate(editor: Editor) {
            flow.value = editor
        }

        override fun close() {
        }
    }

}