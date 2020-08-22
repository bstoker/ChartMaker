/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common.save_dialog.csv

import androidx.annotation.VisibleForTesting
import com.stokerapps.chartmaker.domain.ChartRepository
import com.stokerapps.chartmaker.domain.PieChartEntry
import com.stokerapps.chartmaker.ui.common.SaveChart
import com.stokerapps.chartmaker.ui.common.save_dialog.SaveTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class SaveAsCsvTask(
    private val outputStream: OutputStream,
    private val chartId: UUID,
    private val delimiter: Delimiter,
    private val repository: ChartRepository,
    private val scope: CoroutineScope,
    private val event: SaveChart
) : SaveTask {

    override fun execute() {

        scope.launch {

            event.setState(SaveChart.Running())
            val chart = repository.getPieChartFlow(chartId).first().getOrNull()
            if (chart == null) {
                event.setState(SaveChart.Failed(IllegalArgumentException("Could not fetch chart $chartId!")))

            } else {
                outputStream.bufferedWriter(Charsets.UTF_8).use {
                    chart.entries.forEach { entry ->
                        it.write(
                            entry.toLine(delimiter)
                        )
                    }
                    it.flush()
                }
                event.setState(SaveChart.Completed())
            }
        }
    }

    companion object {

        private val specialCharacterRegex = "[,;:\\\\\\s\"]".toRegex()
        private val formatter =
            DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.US)).apply {
                isGroupingUsed = false
                isDecimalSeparatorAlwaysShown = false
            }

        @VisibleForTesting
        fun PieChartEntry.toLine(delimiter: Delimiter): String {

            val label = if (hasSpecialCharacters(label)) {
                quote(escape(label))
            } else {
                label
            }

            return "$label${delimiter.symbol}${value.formatted()}\n"
        }

        @VisibleForTesting
        fun Float.formatted(): String = formatter.format(this)

        @VisibleForTesting
        fun hasSpecialCharacters(text: String) = text.contains(specialCharacterRegex)

        @VisibleForTesting
        fun escape(text: String): String {
            return text.replace("\"", "\\\"")
        }

        @VisibleForTesting
        fun quote(text: String): String {
            return "\"$text\""
        }
    }
}