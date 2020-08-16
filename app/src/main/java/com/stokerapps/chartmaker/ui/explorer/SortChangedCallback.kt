/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.explorer

import com.stokerapps.chartmaker.domain.Sort

internal interface SortChangedCallback {
    fun onSortChanged(sort: Sort)
}