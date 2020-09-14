/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.piechart

import com.stokerapps.chartmaker.domain.PieChartEntry
import java.math.BigDecimal

data class PieChartEntryItem(
    val id: Int,
    var label: String,
    var value: BigDecimal,
    var color: Int
) {
    fun toDomainModel() = PieChartEntry(id, label, value, color)
}

fun PieChartEntry.toItem() = PieChartEntryItem(
    this.id,
    this.label,
    this.value,
    this.color
)