/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data.database

import androidx.room.TypeConverter
import com.stokerapps.chartmaker.domain.*
import java.math.BigDecimal
import java.util.*

internal class Converters {

    companion object {
        @TypeConverter
        @JvmStatic
        fun fromUUID(uuid: UUID) = uuid.toString()

        @TypeConverter
        @JvmStatic
        fun toUUID(uuid: String): UUID = UUID.fromString(uuid)

        @TypeConverter
        @JvmStatic
        fun fromBigDecimal(value: BigDecimal) = value.toString()

        @TypeConverter
        @JvmStatic
        fun toBigDecimal(value: String): BigDecimal = BigDecimal(value)

        @TypeConverter
        @JvmStatic
        fun fromValueType(type: ValueType) = type.toString()

        @TypeConverter
        @JvmStatic
        fun toValueType(typeName: String) = ValueType.valueOf(typeName)

        @TypeConverter
        @JvmStatic
        fun fromDate(date: Date) = date.time

        @TypeConverter
        @JvmStatic
        fun toDate(time: Long) = Date(time)

        @TypeConverter
        @JvmStatic
        fun fromPropertyTab(tab: Editor.EditTab) = tab.name

        @TypeConverter
        @JvmStatic
        fun toPropertyTab(name: String) = Editor.EditTab.valueOf(name)

        @TypeConverter
        @JvmStatic
        fun fromSortBy(sortBy: SortBy) = sortBy.name

        @TypeConverter
        @JvmStatic
        fun toSortBy(name: String) = SortBy.valueOf(name)

        @TypeConverter
        @JvmStatic
        fun fromSortOrder(sortOrder: SortOrder) = sortOrder.name

        @TypeConverter
        @JvmStatic
        fun toSortOrder(name: String) = SortOrder.valueOf(name)
    }
}
