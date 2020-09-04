/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.explorer

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.stokerapps.chartmaker.domain.PieChart
import com.stokerapps.chartmaker.domain.Sort
import com.stokerapps.chartmaker.ui.common.LiveEvent
import java.util.*

abstract class ExplorerViewModel : ViewModel() {

    internal abstract val events: LiveEvent<Any>

    internal abstract val viewState: LiveData<ViewState>

    abstract fun createPieChart(chart: PieChart)

    abstract fun sort(newSort: Sort)

    abstract fun delete(chartIds: Collection<UUID>)

    abstract fun undoDelete()

    abstract fun import(files: List<String>)
}