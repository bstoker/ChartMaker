/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data.database

import androidx.room.*

@Dao
internal interface ColorDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(colors: List<ColorEntity>)

    @Query("DELETE FROM color")
    suspend fun deleteAll()
}