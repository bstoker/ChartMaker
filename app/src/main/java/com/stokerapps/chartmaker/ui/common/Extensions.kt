@file:Suppress("unused")

package com.stokerapps.chartmaker.ui.common

import android.content.res.Resources
import android.util.TypedValue

/** Converts this value (in dp) to pixels */
val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

/** Converts this value (in pixels) to dp */
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()