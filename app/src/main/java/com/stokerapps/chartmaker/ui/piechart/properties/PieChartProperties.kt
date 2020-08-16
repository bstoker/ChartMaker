/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.piechart.properties

import com.stokerapps.chartmaker.domain.ValueType

data class GeneralProperties(
    val isExpanded: Boolean,
    val description: String?,
    val isTextStyleBold: Boolean,
    val isTextStyleItalic: Boolean,
    val isDescriptionVisible: Boolean,
    val fontName: String,
    val textColor: Int?,
    val textSize: Float,
    val sliceSpace: Float,
    val donutRadius: Float,
    val isRotatable: Boolean
)

data class LabelProperties(
    val isExpanded: Boolean,
    val isTextStyleBold: Boolean,
    val isTextStyleItalic: Boolean,
    val isVisible: Boolean,
    val fontName: String,
    val textColor: Int,
    val textSize: Float
)

data class LegendProperties(
    val isExpanded: Boolean,
    val isTextStyleBold: Boolean,
    val isTextStyleItalic: Boolean,
    val isVisible: Boolean,
    val fontName: String,
    val textColor: Int?,
    val textSize: Float
)

data class ValueProperties(
    val isExpanded: Boolean,
    val isTextStyleBold: Boolean,
    val isTextStyleItalic: Boolean,
    val isVisible: Boolean,
    val currencyCode: String,
    val decimals: Int,
    val fontName: String,
    val textColor: Int,
    val textSize: Float,
    val type: ValueType
)