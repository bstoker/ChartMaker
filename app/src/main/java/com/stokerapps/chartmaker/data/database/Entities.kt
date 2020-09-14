/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.stokerapps.chartmaker.domain.Editor
import com.stokerapps.chartmaker.domain.SortBy
import com.stokerapps.chartmaker.domain.SortOrder
import com.stokerapps.chartmaker.domain.ValueType
import java.math.BigDecimal
import java.util.*

@Entity(tableName = "chart")
internal data class ChartEntity(
    @PrimaryKey val id: UUID,
    val areLabelsVisible: Boolean,
    val areValuesVisible: Boolean,
    val currencyCode: String,
    val dateCreated: Date,
    val dateModified: Date,
    val descriptionFontName: String,
    val descriptionTextColor: Int?,
    val descriptionTextSize: Float,
    val descriptionTextStyleBold: Boolean,
    val descriptionTextStyleItalic: Boolean,
    val donutRadius: Float,
    val isDescriptionVisible: Boolean,
    val isLegendVisible: Boolean,
    val isRotationEnabled: Boolean,
    val labelFontName: String,
    val labelTextColor: Int,
    val labelTextSize: Float,
    val labelTextStyleBold: Boolean,
    val labelTextStyleItalic: Boolean,
    val legendFontName: String,
    val legendTextColor: Int?,
    val legendTextSize: Float,
    val legendTextStyleBold: Boolean,
    val legendTextStyleItalic: Boolean,
    val name: String?,
    val sliceSpace: Float,
    val valueDecimals: Int,
    val valueFontName: String,
    val valueTextColor: Int,
    val valueTextSize: Float,
    val valueTextStyleBold: Boolean,
    val valueTextStyleItalic: Boolean,
    val valueType: ValueType,
    val version: Int
)

internal class ChartWithEntriesEntity(
    @Embedded val chart: ChartEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "chartId",
        entity = EntryEntity2::class
    )
    val entries: List<EntryEntity2>
)

@Entity(tableName = "entry", primaryKeys = ["id", "chartId"])
internal data class EntryEntity2(
    val id: Int,
    val chartId: UUID,
    val label: String,
    val value: BigDecimal,
    val color: Int,
    val order: Int
)

@Entity(tableName = "color")
internal data class ColorEntity(
    @PrimaryKey val argb: Int,
    val editorId: Int,
    val order: Int
)


@Entity(tableName = "editor")
internal data class EditorEntity(
    @PrimaryKey val id: Int,
    val selectedEditTab: Editor.EditTab,
    val sortBy: SortBy,
    val sortOrder: SortOrder,
    val isEditorSidebarExpanded: Boolean,
    val isGeneralPropertySectionExpanded: Boolean,
    val isLabelsPropertySectionExpanded: Boolean,
    val isLegendPropertySectionExpanded: Boolean,
    val isValuesPropertySectionExpanded: Boolean
)

internal class EditorWithColorsEntity(
    @Embedded val editor: EditorEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "editorId",
        entity = ColorEntity::class
    )
    val colors: List<ColorEntity>
)

internal data class EntryEntity1(
    val id: Int,
    val chartId: UUID,
    val label: String,
    val value: Float,
    val color: Int,
    val order: Int
) {
    fun toEntryEntity2() = EntryEntity2(
        id,
        chartId,
        label,
        value.toBigDecimal(),
        color,
        order
    )
}