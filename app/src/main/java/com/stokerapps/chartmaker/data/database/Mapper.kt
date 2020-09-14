/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data.database

import com.stokerapps.chartmaker.domain.Editor
import com.stokerapps.chartmaker.domain.PieChart
import com.stokerapps.chartmaker.domain.PieChartEntry
import com.stokerapps.chartmaker.domain.Sort
import java.util.*

internal fun Editor.toDatabaseEntity(): EditorEntity {
    return EditorEntity(
        id = id,
        selectedEditTab = selectedEditTab,
        sortBy = sort.sortBy,
        sortOrder = sort.sortOrder,
        isEditorSidebarExpanded = isEditSidebarExpanded,
        isGeneralPropertySectionExpanded = isGeneralPropertySectionExpanded,
        isLabelsPropertySectionExpanded = isLabelPropertySectionExpanded,
        isLegendPropertySectionExpanded = isLegendPropertySectionExpanded,
        isValuesPropertySectionExpanded = isValuePropertySectionExpanded
    )
}

internal fun PieChart.toDatabaseEntity(): ChartEntity {
    return ChartEntity(
        currencyCode = currencyCode,
        dateCreated = dateCreated,
        dateModified = dateModified,
        descriptionFontName = descriptionFontName,
        descriptionTextColor = descriptionTextColor,
        descriptionTextSize = descriptionTextSize,
        descriptionTextStyleBold = descriptionTextStyleBold,
        descriptionTextStyleItalic = descriptionTextStyleItalic,
        donutRadius = donutRadius,
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
}

internal fun PieChartEntry.toDatabaseEntity(chartId: UUID, order: Int): EntryEntity2 {
    return EntryEntity2(id, chartId, label, value, color, order)
}

internal fun ChartWithEntriesEntity.toDomainModel(): PieChart =
    PieChart(
        currencyCode = chart.currencyCode,
        dateCreated = chart.dateCreated,
        dateModified = chart.dateModified,
        name = chart.name,
        descriptionFontName = chart.descriptionFontName,
        descriptionTextColor = chart.descriptionTextColor,
        descriptionTextSize = chart.descriptionTextSize,
        descriptionTextStyleBold = chart.descriptionTextStyleBold,
        descriptionTextStyleItalic = chart.descriptionTextStyleItalic,
        entries = entries.sortedBy { it.order }.map { it.toDomainModel() },
        donutRadius = chart.donutRadius,
        id = chart.id,
        areLabelsVisible = chart.areLabelsVisible,
        isDescriptionVisible = chart.isDescriptionVisible,
        isLegendVisible = chart.isLegendVisible,
        isRotationEnabled = chart.isRotationEnabled,
        areValuesVisible = chart.areValuesVisible,
        labelFontName = chart.labelFontName,
        labelTextColor = chart.labelTextColor,
        labelTextSize = chart.labelTextSize,
        labelTextStyleBold = chart.labelTextStyleBold,
        labelTextStyleItalic = chart.labelTextStyleItalic,
        legendFontName = chart.legendFontName,
        legendTextColor = chart.legendTextColor,
        legendTextSize = chart.legendTextSize,
        legendTextStyleBold = chart.legendTextStyleBold,
        legendTextStyleItalic = chart.legendTextStyleItalic,
        sliceSpace = chart.sliceSpace,
        valueDecimals = chart.valueDecimals,
        valueFontName = chart.valueFontName,
        valueTextColor = chart.valueTextColor,
        valueTextSize = chart.valueTextSize,
        valueTextStyleBold = chart.valueTextStyleBold,
        valueTextStyleItalic = chart.valueTextStyleItalic,
        valueType = chart.valueType,
        version = chart.version
    )

internal fun EditorWithColorsEntity.toDomainModel(): Editor =
    Editor(
        editor.id,
        LinkedList(colors.sortedBy { it.order }.map { it.argb }),
        editor.selectedEditTab,
        Sort(editor.sortBy, editor.sortOrder),
        editor.isEditorSidebarExpanded,
        editor.isGeneralPropertySectionExpanded,
        editor.isLabelsPropertySectionExpanded,
        editor.isLegendPropertySectionExpanded,
        editor.isValuesPropertySectionExpanded
    )

internal fun EntryEntity2.toDomainModel(): PieChartEntry {
    return PieChartEntry(id, label, value, color)
}