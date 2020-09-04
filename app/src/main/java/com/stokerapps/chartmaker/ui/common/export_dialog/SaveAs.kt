/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common.export_dialog

import androidx.annotation.StringRes
import com.stokerapps.chartmaker.R

enum class SaveAs(@StringRes val description: Int) {
    CSV(R.string.csv),
    TSV(R.string.tsv)
}