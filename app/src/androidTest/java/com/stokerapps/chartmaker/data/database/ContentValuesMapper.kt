/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data.database

import android.content.ContentValues

private fun clearOrCreateContentValues(contentValues: ContentValues? = null) =
    when (contentValues) {
        null -> ContentValues()
        else -> {
            if (contentValues.isEmpty.not()) {
                contentValues.clear()
            }
            contentValues
        }
    }

internal fun ChartEntity.toContentValues(convertValues: ContentValues? = null) =
    clearOrCreateContentValues(convertValues).apply {
        put("id", Converters.fromUUID(id))
        put("areLabelsVisible", areLabelsVisible)
        put("areValuesVisible", areValuesVisible)
        put("currencyCode", currencyCode)
        put("dateCreated", Converters.fromDate(dateCreated))
        put("dateModified", Converters.fromDate(dateModified))
        put("descriptionFontName", descriptionFontName)
        put("descriptionTextColor", descriptionTextColor)
        put("descriptionTextSize", descriptionTextSize)
        put("descriptionTextStyleBold", descriptionTextStyleBold)
        put("descriptionTextStyleItalic", descriptionTextStyleItalic)
        put("donutRadius", donutRadius)
        put("isDescriptionVisible", isDescriptionVisible)
        put("isLegendVisible", isLegendVisible)
        put("isRotationEnabled", isRotationEnabled)
        put("labelFontName", labelFontName)
        put("labelTextColor", labelTextColor)
        put("labelTextSize", labelTextSize)
        put("labelTextStyleBold", labelTextStyleBold)
        put("labelTextStyleItalic", labelTextStyleItalic)
        put("legendFontName", legendFontName)
        put("legendTextColor", legendTextColor)
        put("legendTextSize", legendTextSize)
        put("legendTextStyleBold", legendTextStyleBold)
        put("legendTextStyleItalic", legendTextStyleItalic)
        put("name", name)
        put("sliceSpace", sliceSpace)
        put("valueDecimals", valueDecimals)
        put("valueFontName", valueFontName)
        put("valueTextColor", valueTextColor)
        put("valueTextSize", valueTextSize)
        put("valueTextStyleBold", valueTextStyleBold)
        put("valueTextStyleItalic", valueTextStyleItalic)
        put("valueType", Converters.fromValueType(valueType))
        put("version", version)
    }

internal fun EntryEntity1.toContentValues(convertValues: ContentValues? = null) =
    clearOrCreateContentValues(convertValues).apply {
        put("id", id)
        put("chartId", Converters.fromUUID(chartId))
        put("label", label)
        put("value", value)
        put("color", color)
        put("`order`", order)
    }