/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common.options_menu

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class Option(
    @DrawableRes val icon: Int,
    @StringRes val description: Int
)