/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data.database

import com.stokerapps.chartmaker.domain.Editor
import com.stokerapps.chartmaker.domain.PieChart

internal class TestData {
    val editor = Editor.default
    val pieChart = PieChart.createPieChart()
}
