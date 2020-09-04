/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common

import android.content.Context
import com.stokerapps.chartmaker.domain.Resources

class AndroidResources(private val context: Context): Resources {

    override fun getString(resId: Int): String {
        return context.getString(resId)
    }
}