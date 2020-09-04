/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data.files

import androidx.annotation.VisibleForTesting
import com.stokerapps.chartmaker.domain.PieChart
import com.stokerapps.chartmaker.domain.PieChartEntry
import com.stokerapps.chartmaker.domain.Delimiter
import java.io.BufferedWriter
import java.io.OutputStream
import java.nio.charset.Charset
import java.text.DecimalFormat
import java.util.*

class CsvWriter(
    private val charset: Charset = Charsets.UTF_8,
    private val delimiter: Delimiter = Delimiter.COMMA
) {

    fun write(chart: PieChart, outputStream: OutputStream) {

        val output: BufferedWriter = when (outputStream) {
            is BufferedWriter -> outputStream
            else -> outputStream.bufferedWriter(charset)
        }

        chart.entries.forEach { entry ->
            output.write(
                entry.toLine(delimiter)
            )
        }
        output.flush()
    }

    companion object {

        private val specialCharacterRegex = "[,;:\\\\\\s\"]".toRegex()
        private val formatter =
            DecimalFormat.getNumberInstance(Locale.US).apply {
                isGroupingUsed = false
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
        fun Float.formatted(): String = formatter.format(this.toDouble())

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