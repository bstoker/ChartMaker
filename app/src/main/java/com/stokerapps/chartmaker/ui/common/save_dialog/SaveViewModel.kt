/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common.save_dialog

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.stokerapps.chartmaker.ChartMakerApp
import com.stokerapps.chartmaker.domain.ChartRepository
import com.stokerapps.chartmaker.ui.common.Event
import com.stokerapps.chartmaker.ui.common.LiveEvent
import com.stokerapps.chartmaker.ui.common.SaveChart
import com.stokerapps.chartmaker.ui.common.save_dialog.csv.Delimiter
import com.stokerapps.chartmaker.ui.common.save_dialog.csv.SaveAsCsvTask
import kotlinx.coroutines.CoroutineScope
import java.io.FileNotFoundException
import java.util.*

class SaveViewModel(
    application: Application,
    private val applicationScope: CoroutineScope,
    private val chartRepository: ChartRepository
) : AndroidViewModel(application) {

    val events = LiveEvent<Event>()

    fun saveAsCsv(uri: Uri, chartId: UUID, delimiter: Delimiter) {

        val event = SaveChart(uri, "text/csv")
        events.postValue(event)

        try {
            val contentResolver = getApplication<ChartMakerApp>().applicationContext.contentResolver
            val outputStream = contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                SaveAsCsvTask(
                    outputStream,
                    chartId,
                    delimiter,
                    chartRepository,
                    applicationScope,
                    event
                ).execute()
            } else {
                event.setState(SaveChart.Failed(NullPointerException("OutputStream is null!")))
            }
        } catch (e: FileNotFoundException) {
            event.setState(SaveChart.Failed(e))
        }
    }
}