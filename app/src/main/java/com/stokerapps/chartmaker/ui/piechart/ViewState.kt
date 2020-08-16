/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.piechart

import com.stokerapps.chartmaker.domain.PieChart

interface ViewState

object Loading : ViewState

data class Loaded(val chart: PieChart, val isEditSidebarExpanded: Boolean) : ViewState

data class Empty(val chart: PieChart) : ViewState

data class ErrorLoading(val exception: Throwable?) : ViewState

