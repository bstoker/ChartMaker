/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common

import android.view.Menu
import android.view.MenuItem
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.size

fun Menu.setIconTint(color: Int) {
    (0 until size).forEach { index ->
        getItem(index).setIconTint(color)
    }
}

fun MenuItem.setIconTint(color: Int) {
    icon?.let {
        val drawable = DrawableCompat.wrap(it)
        DrawableCompat.setTint(drawable, color)
        icon = drawable
    }
}