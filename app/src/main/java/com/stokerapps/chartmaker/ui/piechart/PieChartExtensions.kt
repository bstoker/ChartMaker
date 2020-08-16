/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.piechart

import android.graphics.Color
import android.graphics.Typeface
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.charts.PieChart as PieChartView
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.domain.PieChart
import com.stokerapps.chartmaker.domain.PieChartEntry
import com.stokerapps.chartmaker.domain.ValueType
import com.stokerapps.chartmaker.ui.common.Fonts
import com.stokerapps.chartmaker.ui.common.getColorCompat
import java.util.*
import kotlin.math.min

internal const val ICON_WIDTH = 48f
internal const val CHART_OFFSET = 16f

internal val PieChartView.defaultColorBackground: Int
    get() = context.getColorCompat(R.color.colorBackgroundChart)

internal val PieChartView.defaultColorOnBackground: Int
    get() = context.getColorCompat(R.color.colorOnBackgroundChart)

internal fun PieChartView.updateAsIcon(
    pieChart: PieChart,
    colorBackground: Int = defaultColorBackground,
    colorOnBackground: Int = defaultColorOnBackground
) {
    val scaleFactor =
        ICON_WIDTH / (context.resources.configuration.smallestScreenWidthDp - CHART_OFFSET)
    data = createPieData(
        colors = pieChart.entries.map { it.color },
        entries = pieChart.entries.map { it.toPieChartEntry() },
        sliceSpace = pieChart.sliceSpace * scaleFactor
    )
    data.setDrawValues(false)

    if (pieChart.isEmpty()) {
        centerText = context.getText(R.string.no_data_2_lines)
        setCenterTextColor(colorOnBackground)
        setCenterTextSize(11f)
        setDrawCenterText(true)

    } else {
        setDrawCenterText(false)
    }

    description = null

    legend.isEnabled = false
    setDrawEntryLabels(false)

    setHoleColor(Color.TRANSPARENT)
    setTransparentCircleColor(colorBackground)

    holeRadius = pieChart.donutRadius
    isDrawHoleEnabled = pieChart.isDrawHoleEnabled()
    transparentCircleRadius = pieChart.donutRadius + 3

    isRotationEnabled = false
}

internal fun PieChartView.update(
    pieChart: PieChart,
    colorBackground: Int = defaultColorBackground,
    colorOnBackground: Int = defaultColorOnBackground
) {

    updateLegend(pieChart, colorOnBackground)
    updateLabels(pieChart)
    updateValues(pieChart)

    centerText = pieChart.name
    setCenterTextColor(pieChart.descriptionTextColor ?: defaultColorOnBackground)
    setCenterTextSize(pieChart.descriptionTextSize)
    setCenterTextTypeface(
        Fonts.get(
            pieChart.descriptionFontName,
            context,
            getTextStyle(pieChart.descriptionTextStyleBold, pieChart.descriptionTextStyleItalic)
        )
    )
    setDrawCenterText(pieChart.isDescriptionVisible)

    with(description) {
        isEnabled = false // we use centerText
        text = pieChart.name
        textColor =
            pieChart.descriptionTextColor ?: colorOnBackground
        textSize = pieChart.descriptionTextSize
        typeface = Fonts.get(
            pieChart.descriptionFontName,
            context,
            getTextStyle(pieChart.descriptionTextStyleBold, pieChart.descriptionTextStyleItalic)
        )
    }

    isDrawHoleEnabled = pieChart.isDrawHoleEnabled()
    holeRadius = pieChart.donutRadius
    setHoleColor(Color.TRANSPARENT)
    transparentCircleRadius = min(pieChart.donutRadius + 3, PieChart.MAX_DONUT_RADIUS)
    setTransparentCircleColor(colorBackground)
    setTransparentCircleAlpha(110)

    isRotationEnabled = pieChart.isRotationEnabled

    setUsePercentValues(pieChart.valueType == ValueType.PERCENTAGE)
    highlightValues(null)
    invalidate()
}

private fun PieChartView.updateLegend(
    pieChart: PieChart,
    defaultColorOnBackground: Int
) {
    with(legend) {
        isEnabled = pieChart.isLegendVisible
        verticalAlignment = Legend.LegendVerticalAlignment.TOP
        horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        orientation = Legend.LegendOrientation.VERTICAL
        setDrawInside(false)
        xEntrySpace = 7f
        yEntrySpace = 0f
        yOffset = 0f
        textColor = pieChart.legendTextColor ?: defaultColorOnBackground
        textSize = pieChart.legendTextSize
        typeface = Fonts.get(
            pieChart.legendFontName,
            context,
            getTextStyle(pieChart.legendTextStyleBold, pieChart.legendTextStyleItalic)
        )
    }
}

private fun PieChartView.updateLabels(pieChart: PieChart) {
    setDrawEntryLabels(pieChart.areLabelsVisible)
    setEntryLabelColor(pieChart.labelTextColor)
    setEntryLabelTypeface(
        Fonts.get(
            pieChart.labelFontName,
            context,
            getTextStyle(pieChart.labelTextStyleBold, pieChart.labelTextStyleItalic)
        )
    )
    setEntryLabelTextSize(pieChart.labelTextSize)
}

private fun PieChartView.updateValues(pieChart: PieChart) {
    data = createPieData(
        colors = pieChart.entries.map { it.color },
        entries = pieChart.entries.map { it.toPieChartEntry() },
        sliceSpace = pieChart.sliceSpace
    )
    data.setDrawValues(pieChart.areValuesVisible)
    data.setValueFormatter(
        when (pieChart.valueType) {
            ValueType.CURRENCY -> CurrencyFormatter(
                pieChart.valueDecimals,
                getLocale(pieChart.currencyCode)
            )
            ValueType.NUMBER -> NumberFormatter(pieChart.valueDecimals)
            ValueType.PERCENTAGE -> PercentFormatter(pieChart.valueDecimals)
        }
    )
    data.setValueTextColor(pieChart.valueTextColor)
    data.setValueTextSize(pieChart.valueTextSize)
    data.setValueTypeface(
        Fonts.get(
            pieChart.valueFontName,
            context,
            getTextStyle(pieChart.valueTextStyleBold, pieChart.valueTextStyleItalic)
        )
    )
}

private fun createPieData(
    entries: List<PieEntry>,
    label: String = "",
    drawIcons: Boolean = false,
    sliceSpace: Float = 0f,
    iconsOffset: MPPointF = MPPointF(0f, 40f),
    selectionShift: Float = 5f,
    colors: List<Int> = PieChartEntry.COLORS
): PieData {
    val dataSet = PieDataSet(entries, label)
    dataSet.setDrawIcons(drawIcons)
    dataSet.sliceSpace = sliceSpace
    dataSet.iconsOffset = iconsOffset
    dataSet.selectionShift = selectionShift
    dataSet.colors = colors
    return PieData(dataSet)
}

private fun getLocale(currencyCode: String): Locale {
    return Locale.getAvailableLocales()
        .filter {
            try {
                Currency.getInstance(it) != null
            } catch (e: IllegalArgumentException) {
                false
            }
        }
        .find { Currency.getInstance(it).currencyCode == currencyCode } ?: Locale.getDefault()
}

private fun PieChartEntry.toPieChartEntry(): PieEntry {
    return PieEntry(value, label, this)
}

private fun getTextStyle(isBold: Boolean, isItalic: Boolean): Int {
    return when {
        isBold && isItalic -> Typeface.BOLD_ITALIC
        isBold -> Typeface.BOLD
        isItalic -> Typeface.ITALIC
        else -> Typeface.NORMAL
    }
}