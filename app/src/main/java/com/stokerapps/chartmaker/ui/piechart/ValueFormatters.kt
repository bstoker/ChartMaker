/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.piechart

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class CurrencyFormatter(
    private val decimals: Int,
    locale: Locale = Locale.getDefault()
) : ValueFormatter() {

    private val formatter = NumberFormat.getCurrencyInstance(locale).apply {
        maximumFractionDigits = decimals
    }

    override fun getFormattedValue(value: Float): String? {
        return formatter.format(value.toDouble())
    }
}

class NumberFormatter(
    private val decimals: Int,
    locale: Locale = Locale.getDefault()
) : ValueFormatter() {

    private val formatter = DecimalFormat.getNumberInstance(locale).apply {
        maximumFractionDigits = decimals
    }

    override fun getFormattedValue(value: Float): String? {
        return formatter.format(value.toDouble())
    }
}

class PercentFormatter(
    private val decimals: Int,
    locale: Locale = Locale.getDefault()
) : ValueFormatter() {

    private val formatter = DecimalFormat.getPercentInstance(locale).apply {
        maximumFractionDigits = decimals
    }

    override fun getFormattedValue(value: Float): String? {
        return formatter.format(value.toDouble() / 100)
    }
}