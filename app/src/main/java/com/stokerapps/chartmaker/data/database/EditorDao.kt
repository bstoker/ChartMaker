/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
internal interface EditorDao : BaseDao<EditorEntity> {

    @Transaction
    @Query("SELECT * FROM editor LIMIT 1")
    fun getEditorWithColors(): EditorWithColorsEntity?

    @Transaction
    @Query("SELECT * FROM editor LIMIT 1")
    fun getEditorWithColorsFlow(): Flow<EditorWithColorsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(entity: EditorEntity)
}