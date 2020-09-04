/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data.database

import androidx.paging.DataSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
internal interface ChartDao : BaseDao<ChartEntity> {

    @Transaction
    @Query("SELECT * FROM chart")
    fun getAll(): List<ChartWithEntriesEntity>

    @Transaction
    @Query("SELECT * FROM chart")
    fun getAllFlow(): Flow<List<ChartWithEntriesEntity>>

    @Transaction
    @Query("SELECT * FROM chart")
    fun getAllPaged(): DataSource.Factory<Int, ChartWithEntriesEntity>

    @Transaction
    @Query("SELECT * FROM chart ORDER BY CASE WHEN :isAscending = 1 THEN dateModified END ASC, CASE WHEN :isAscending = 0 THEN dateModified END DESC")
    fun getAllPagedSortedByDateModified(isAscending: Boolean): DataSource.Factory<Int, ChartWithEntriesEntity>

    @Transaction
    @Query("SELECT * FROM chart WHERE id IN (:chartId)")
    suspend fun getChartWithEntries(chartId: UUID): ChartWithEntriesEntity?

    @Transaction
    @Query("SELECT * FROM chart WHERE id IN (:chartId)")
    fun getChartWithEntriesFlow(chartId: UUID): Flow<ChartWithEntriesEntity?>

    @Transaction
    @Query("SELECT * FROM chart WHERE id IN (:chartId)")
    fun get(chartId: UUID): ChartWithEntriesEntity?

    @Query("SELECT * FROM chart WHERE id IN (:chartIds)")
    fun get(chartIds: List<String>): List<ChartEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(entity: ChartEntity)

    @Query("DELETE FROM chart WHERE id IN (:keys)")
    suspend fun deleteByIds(keys: Collection<UUID>)
}