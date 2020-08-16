/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data

import com.stokerapps.chartmaker.common.Result
import com.stokerapps.chartmaker.domain.Editor
import com.stokerapps.chartmaker.domain.EditorRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditorRepositoryImpl(
    private val cache: Cache,
    private val database: EditorDataSource
) : EditorRepository {

    override suspend fun getEditor(): Editor {
        return cache.getEditorFlow().value ?: database.getEditor() ?: Editor.default
    }

    override fun getEditorFlow(): Flow<Result<Editor>> =
        channelFlow {
            launch {
                cache.getEditorFlow()
                    .filterNotNull()
                    .collect {
                        send(Result.success(it))
                    }
            }
            launch {
                database.getEditorFlow()
                    .catch { send(Result.failure(it)) }
                    .collect { editor ->
                        cache.update(editor ?: Editor.default)
                    }
            }
        }.distinctUntilChanged()


    override suspend fun store(editor: Editor) {
        cache.update(editor)
        database.updateOrCreate(editor)
    }
}