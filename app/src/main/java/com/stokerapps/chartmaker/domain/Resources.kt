/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.domain

import androidx.annotation.StringRes

interface Resources {
    fun getString(@StringRes resId: Int): String
}