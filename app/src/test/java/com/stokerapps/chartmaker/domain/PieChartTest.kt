/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.domain

import org.junit.Assert.*
import org.junit.Test

class PieChartTest {

    @Test
    fun testUpdate() {
        val chart = PieChart.createPieChartWithData()
        Thread.sleep(10)
        val updatedChart = chart.update()
        assertEquals(chart.version + 1, updatedChart.version)
        assertNotEquals(chart.dateModified, updatedChart.dateModified)
    }
}