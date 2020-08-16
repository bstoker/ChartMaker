/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data.database

import android.content.Context
import android.os.AsyncTask
import androidx.annotation.VisibleForTesting
import androidx.paging.DataSource
import androidx.room.*
import com.stokerapps.chartmaker.data.ChartDataSource
import com.stokerapps.chartmaker.data.EditorDataSource
import com.stokerapps.chartmaker.domain.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

@Database(
    entities = [
        ChartEntity::class,
        ColorEntity::class,
        EditorEntity::class,
        EntryEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase protected constructor() : RoomDatabase(),
    ChartDataSource,
    EditorDataSource {

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context) =
            instance ?: synchronized(this) {
                instance ?: createDatabase(context).also { instance = it }
            }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "database"
            )
                .setQueryExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                .setTransactionExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                .build()
    }

    internal abstract fun chartDao(): ChartDao
    internal abstract fun colorDao(): ColorDao
    internal abstract fun editorDao(): EditorDao
    internal abstract fun entryDao(): EntryDao

    override fun getAllChartsFlow(): Flow<List<PieChart>> {
        return chartDao().getAllFlow().map {
            it.map { chart -> chart.toDomainModel() }
        }
    }

    override fun getAllChartsPaged(sort: Sort): DataSource.Factory<Int, Chart> =
        when (sort.sortBy) {
            SortBy.DATE_MODIFIED ->
                chartDao()
                    .getAllPagedSortedByDateModified(sort.isAscending())
                    .map { it.toDomainModel() }
            SortBy.DESCRIPTION ->
                chartDao()
                    .getAllPaged()
                    .map { it.toDomainModel() }
                    .mapByPage {
                        if (sort.isAscending()) {
                            it.sortedBy { chart ->
                                chart.getDescription()
                            }
                        } else {
                            it.sortedByDescending { chart ->
                                chart.getDescription()
                            }
                        }
                    }
        }

    override fun getChartFlow(chartId: UUID): Flow<PieChart?> =
        chartDao().getChartWithEntriesFlow(chartId).map { it?.toDomainModel() }

    override suspend fun updateOrCreate(entries: List<PieChartEntry>, chart: PieChart) {
        entryDao().insert(entries.map { entry ->
            entry.toDatabaseEntity(chart.id, chart.entries.indexOf(entry))
        })
    }

    override suspend fun updateOrCreate(chart: PieChart) = withTransaction {
        chartDao().insert(chart.toDatabaseEntity())
        entryDao().delete(chart.id)
        entryDao().insert(chart.entries.mapIndexed { index, entry ->
            entry.toDatabaseEntity(chart.id, index)
        })
    }

    override suspend fun delete(chart: PieChart) = chartDao().delete(chart.toDatabaseEntity())

    override suspend fun delete(charts: Collection<UUID>) = chartDao().deleteByIds(charts)

    override suspend fun delete(entry: PieChartEntry, chart: PieChart) = withTransaction {
        val entries = chart.entries.toMutableList()
        val removedIndex = entries.indexOf(entry)
        if (removedIndex != -1) {
            entryDao().delete(entry.id, chart.id)
            entryDao().insert(entries
                .apply {
                    removeAt(removedIndex)
                }
                .filterIndexed { index, _ -> index >= removedIndex }
                .mapIndexed { index, entry ->
                    entry.toDatabaseEntity(
                        chart.id,
                        removedIndex + index
                    )
                }
            )
        }
    }


    override suspend fun getEditor() = editorDao().getEditorWithColors()?.toDomainModel()

    override fun getEditorFlow(): Flow<Editor?> =
        editorDao().getEditorWithColorsFlow().map { it?.toDomainModel() }

    override suspend fun updateOrCreate(editor: Editor) = withTransaction {
        editorDao().insert(editor.toDatabaseEntity())
        colorDao().deleteAll()
        colorDao().insert(editor.colors.mapIndexed { index, color ->
            ColorEntity(color, editor.id, index)
        })
    }

    @VisibleForTesting
    suspend fun delete(editor: Editor) = editorDao().delete(editor.toDatabaseEntity())
}