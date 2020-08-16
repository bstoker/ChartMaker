/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.explorer

import androidx.paging.PagedList
import com.stokerapps.chartmaker.domain.Chart
import com.stokerapps.chartmaker.domain.Sort

internal interface ViewState

object Loading : ViewState

data class Loaded(val charts: PagedList<Chart>, val sort: Sort) : ViewState

data class Empty(val charts: PagedList<Chart>, val sort: Sort) : ViewState

