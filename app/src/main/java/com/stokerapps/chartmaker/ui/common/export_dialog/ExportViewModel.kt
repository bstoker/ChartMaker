/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common.export_dialog

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.stokerapps.chartmaker.domain.ChartRepository
import com.stokerapps.chartmaker.domain.FileManager
import com.stokerapps.chartmaker.domain.Resources
import com.stokerapps.chartmaker.domain.usecases.ExportCsvFiles
import com.stokerapps.chartmaker.ui.common.LiveEvent
import com.stokerapps.chartmaker.domain.Delimiter
import kotlinx.coroutines.CoroutineScope
import java.util.*

class ExportViewModel(
    private val applicationScope: CoroutineScope,
    private val chartRepository: ChartRepository,
    private val fileManager: FileManager,
    private val resources: Resources
) : ViewModel() {

    val events = LiveEvent<Any>()

    fun exportCsvFiles(uri: Uri, chartId: Collection<UUID>, delimiter: Delimiter) {
        ExportCsvFiles(
            applicationScope,
            fileManager,
            chartId,
            uri.toString(),
            delimiter,
            chartRepository,
            resources
        ).also { task ->
            events.postValue(task)
            task.execute()
        }
    }
}