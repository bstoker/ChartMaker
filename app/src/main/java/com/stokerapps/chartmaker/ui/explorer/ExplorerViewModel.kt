/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.explorer

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.stokerapps.chartmaker.domain.Chart
import com.stokerapps.chartmaker.domain.Sort
import com.stokerapps.chartmaker.ui.common.Event
import com.stokerapps.chartmaker.ui.common.LiveEvent
import java.util.*

abstract class ExplorerViewModel : ViewModel() {

    internal abstract val events: LiveEvent<Event>

    internal abstract val viewState: LiveData<ViewState>

    abstract fun createPieChart(): Chart

    abstract fun sort(newSort: Sort)

    abstract fun delete(chartIds: Collection<UUID>)

    abstract fun undoDelete()
}