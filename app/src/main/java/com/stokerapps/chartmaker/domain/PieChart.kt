/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.domain

import com.stokerapps.chartmaker.ui.common.*
import java.util.*
import kotlin.math.roundToInt

enum class ValueType { NUMBER, PERCENTAGE, CURRENCY }

data class PieChart(
    override val dateCreated: Date = Date(),
    override val id: UUID = UUID.randomUUID(),
    override var dateModified: Date = dateCreated,
    override var name: String? = null,
    val currencyCode: String = "EUR",
    val areLabelsVisible: Boolean = true,
    val areValuesVisible: Boolean = true,
    val descriptionFontName: String = Fonts.DEFAULT_FONT_NAME,
    val descriptionTextColor: Int? = null,
    val descriptionTextSize: Float = Fonts.DEFAULT_TEXT_SIZE,
    val descriptionTextStyleBold: Boolean = false,
    val descriptionTextStyleItalic: Boolean = false,
    val donutRadius: Float = 0f,
    val entries: List<PieChartEntry> = listOf(),
    val isDescriptionVisible: Boolean = false,
    val isLegendVisible: Boolean = true,
    val isRotationEnabled: Boolean = false,
    val labelFontName: String = Fonts.DEFAULT_FONT_NAME,
    val labelTextColor: Int = WHITE,
    val labelTextSize: Float = Fonts.DEFAULT_TEXT_SIZE,
    val labelTextStyleBold: Boolean = false,
    val labelTextStyleItalic: Boolean = false,
    val legendFontName: String = Fonts.DEFAULT_FONT_NAME,
    val legendTextColor: Int? = null,
    val legendTextSize: Float = Fonts.DEFAULT_TEXT_SIZE,
    val legendTextStyleBold: Boolean = false,
    val legendTextStyleItalic: Boolean = false,
    val sliceSpace: Float = 3f,
    val valueDecimals: Int = 0,
    val valueFontName: String = Fonts.DEFAULT_FONT_NAME,
    val valueTextColor: Int = WHITE,
    val valueTextSize: Float = Fonts.DEFAULT_TEXT_SIZE,
    val valueTextStyleBold: Boolean = false,
    val valueTextStyleItalic: Boolean = false,
    val valueType: ValueType = ValueType.NUMBER,
    val version: Int = 0
) : Chart {

    companion object {

        const val MIN_DONUT_RADIUS = 0f
        const val MAX_DONUT_RADIUS = 99f
        const val MIN_SLICE_SPACE = 0f
        const val MAX_SLICE_SPACE = 20f

        private const val SEPARATOR = ", "

        val newChart = PieChart(
            entries = listOf(
                PieChartEntry(value = 120f, color = SUMMER_SKY),
                PieChartEntry(value = 180f, color = GOLDEN_POPPY),
                PieChartEntry(value = 60f, color = OLD_ROSE)
            )
        )

        val placeholder = PieChart(
            entries = listOf(
                PieChartEntry(value = 120f, color = DARK_GREY),
                PieChartEntry(value = 180f, color = SCARPA_FLOW),
                PieChartEntry(value = 60f, color = MINESHAFT_DARK)
            )
        )

        fun createPieChartWithData() = PieChart(
            entries = listOf(
                PieChartEntry(label = "Gas", value = 120f),
                PieChartEntry(label = "Food", value = 180f),
                PieChartEntry(label = "Activities", value = 60f)
            )
        )
    }

    fun isDrawHoleEnabled() = donutRadius > 0f

    override fun isEmpty() = entries.isEmpty()

    fun getAverageValue(): Float? = if (isEmpty()) {
        null
    } else {
        (entries.sumBy { it.value.roundToInt() } / entries.size).toFloat()
    }

    override fun getDescription(): String? =
        when {
            name.isNullOrBlank().not() -> name
            entries.any { it.label.isBlank().not() } -> getDescriptionOfEntries()
            else -> null
        }

    private fun getDescriptionOfEntries(): String {
        val entriesString = StringBuilder()
        val nonBlankEntries = entries.filter { it.label.isBlank().not() }
        if (nonBlankEntries.isNotEmpty()) {
            nonBlankEntries.forEachIndexed { index, entry ->
                entriesString.append(entry.label)
                if (index < nonBlankEntries.size - 1) {
                    entriesString.append(SEPARATOR)
                }
            }
        }
        return entriesString.toString()
    }

    /**
     * @return a deep copy of this [PieChart] chart that has it's version incremented and
     * it's date modified set to now.
     */
    fun update(
        areLabelsVisible: Boolean = this.areValuesVisible,
        areValuesVisible: Boolean = this.areValuesVisible,
        currencyCode: String = this.currencyCode,
        dateCreated: Date = this.dateCreated,
        dateModified: Date = Date(),
        descriptionFontName: String = this.descriptionFontName,
        descriptionTextColor: Int? = this.descriptionTextColor,
        descriptionTextSize: Float = this.descriptionTextSize,
        descriptionTextStyleBold: Boolean = this.descriptionTextStyleBold,
        descriptionTextStyleItalic: Boolean = this.descriptionTextStyleItalic,
        donutRadius: Float = this.donutRadius,
        entries: List<PieChartEntry> = this.entries.map { it.copy() },
        id: UUID = this.id,
        isDescriptionVisible: Boolean = this.isDescriptionVisible,
        isLegendVisible: Boolean = this.isLegendVisible,
        isRotationEnabled: Boolean = this.isRotationEnabled,
        labelFontName: String = this.labelFontName,
        labelTextColor: Int = this.labelTextColor,
        labelTextSize: Float = this.labelTextSize,
        labelTextStyleBold: Boolean = this.labelTextStyleBold,
        labelTextStyleItalic: Boolean = this.labelTextStyleItalic,
        legendFontName: String = this.legendFontName,
        legendTextColor: Int? = this.legendTextColor,
        legendTextSize: Float = this.legendTextSize,
        legendTextStyleBold: Boolean = this.legendTextStyleBold,
        legendTextStyleItalic: Boolean = this.legendTextStyleItalic,
        name: String? = this.name,
        sliceSpace: Float = this.sliceSpace,
        valueDecimals: Int = this.valueDecimals,
        valueFontName: String = this.valueFontName,
        valueTextColor: Int = this.valueTextColor,
        valueTextSize: Float = this.valueTextSize,
        valueTextStyleBold: Boolean = this.valueTextStyleBold,
        valueTextStyleItalic: Boolean = this.valueTextStyleItalic,
        valueType: ValueType = this.valueType,
        version: Int = this.version + 1
    ) = PieChart(
        currencyCode = currencyCode,
        dateCreated = dateCreated,
        dateModified = dateModified,
        descriptionFontName = descriptionFontName,
        descriptionTextColor = descriptionTextColor,
        descriptionTextSize = descriptionTextSize,
        descriptionTextStyleBold = descriptionTextStyleBold,
        descriptionTextStyleItalic = descriptionTextStyleItalic,
        donutRadius = donutRadius,
        entries = entries,
        id = id,
        isDescriptionVisible = isDescriptionVisible,
        areLabelsVisible = areLabelsVisible,
        isLegendVisible = isLegendVisible,
        isRotationEnabled = isRotationEnabled,
        areValuesVisible = areValuesVisible,
        labelFontName = labelFontName,
        labelTextColor = labelTextColor,
        labelTextSize = labelTextSize,
        labelTextStyleBold = labelTextStyleBold,
        labelTextStyleItalic = labelTextStyleItalic,
        legendFontName = legendFontName,
        legendTextColor = legendTextColor,
        legendTextSize = legendTextSize,
        legendTextStyleBold = legendTextStyleBold,
        legendTextStyleItalic = legendTextStyleItalic,
        name = name,
        sliceSpace = sliceSpace,
        valueDecimals = valueDecimals,
        valueFontName = valueFontName,
        valueTextColor = valueTextColor,
        valueTextSize = valueTextSize,
        valueTextStyleBold = valueTextStyleBold,
        valueTextStyleItalic = valueTextStyleItalic,
        valueType = valueType,
        version = version
    )

    override fun toString() =
        "${javaClass.simpleName}(id=${shortId()}, size=${entries.size}, v=$version)@${Integer.toHexString(
            hashCode()
        )}"

    private fun shortId() = id.toString().substring(30)
}

// TODO: Store value as BigDecimal
data class PieChartEntry(
    val id: Int = random.nextInt(),
    val label: String = "",
    val value: Float = defaultValue,
    val color: Int = getColor()
) {
    companion object {
        const val defaultValue = 20f
        val random = Random()
        val COLORS = listOf(
            ATOLL,
            CRANBERRY,
            DARK_ORANGE,
            ELECTRIC_VIOLET,
            MEDIUM_TURQUOISE,
            MOON_YELLOW,
            OLD_ROSE,
            OLIVE_DRAB,
            TICKLE_ME_PINK
        ).shuffled()

        private fun getColor(): Int {
            return COLORS[colorIndex++ % COLORS.size]
        }

        private var colorIndex = 0
    }

    override fun toString() = "PieChartEntry(${shortId()}, $label, $value, $color)"

    private fun shortId() = Integer.toHexString(id)
}