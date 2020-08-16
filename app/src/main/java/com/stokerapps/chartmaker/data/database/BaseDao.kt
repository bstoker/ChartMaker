/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data.database

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

internal interface BaseDao<T> {

    @Insert
    suspend fun insert(entity: T)

    @Insert
    suspend fun insert(vararg entities: T)

    @Update
    suspend fun update(entity: T)

    @Delete
    suspend fun delete(entity: T)

    @Delete
    suspend fun delete(vararg entity: T)
}