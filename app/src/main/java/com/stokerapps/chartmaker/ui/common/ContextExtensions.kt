/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common

import android.content.Context
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat


fun Context.showToast(@StringRes text: Int, duration: Int = Toast.LENGTH_LONG): Toast =
    Toast.makeText(this, text, duration).also { toast ->
        toast.show()
        toast
    }

fun Context.showToast(text: CharSequence, duration: Int = Toast.LENGTH_LONG): Toast =
    Toast.makeText(this, text, duration).also { toast ->
        toast.show()
        toast
    }

@ColorInt
fun Context.getColorCompat(@ColorRes colorRes: Int) =
    ContextCompat.getColor(this, colorRes)

@ColorInt
fun Context.getColorAttribute(@AttrRes attributeId: Int): Int {
    val resolvedAttr = TypedValue()
    if (theme.resolveAttribute(attributeId, resolvedAttr, true)) {
        if (resolvedAttr.isColorTypeCompat()) {
            val colorRes = resolvedAttr.resourceId
            return ContextCompat.getColor(this, colorRes)
        }
    }
    return -1
}

private fun TypedValue.isColorTypeCompat() = (type >= TypedValue.TYPE_FIRST_COLOR_INT
        && type <= TypedValue.TYPE_LAST_COLOR_INT)