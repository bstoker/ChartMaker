/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.*

@Dao
internal interface EntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entries: List<EntryEntity>)

    @Query("DELETE FROM entry WHERE chartId = :chartId")
    fun delete(chartId: UUID)

    @Query("DELETE FROM entry WHERE chartId = :chartId AND id = :entryId")
    fun delete(entryId: Int, chartId: UUID)
}