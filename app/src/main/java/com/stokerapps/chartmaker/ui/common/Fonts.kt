/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common

import android.content.Context
import android.graphics.Typeface
import androidx.collection.arrayMapOf
import androidx.core.content.res.ResourcesCompat
import com.stokerapps.chartmaker.R

object Fonts {

    const val DEFAULT_FONT_NAME = "sans-serif"
    const val DEFAULT_TEXT_SIZE = 14f

    private val fontCache = arrayMapOf<String, Typeface>()
    private val fontResources = arrayMapOf(
        "opensans-light" to R.font.opensans_light,
        "opensans-regular" to R.font.opensans_regular
    )
    private val systemFonts = listOf(
        "sans-serif",
        "sans-serif-light",
        "sans-serif-condensed",
        "sans-serif-black",
        "sans-serif-thin",
        "sans-serif-medium"
    )
    
    val names = systemFonts + fontResources.keys
    val sizes = List(12) { it + 11f }

    fun get(name: String, context: Context?, style: Int = Typeface.NORMAL): Typeface {
        if (!fontCache.contains(name)) {
            val resourceId = fontResources[name]
            when {
                resourceId == null -> return Typeface.create(name, style)
                    .also { fontCache[name] = it }
                context == null -> return Typeface.DEFAULT
                else -> fontCache[name] = ResourcesCompat.getFont(context, resourceId)
            }
        }
        return Typeface.create(fontCache[name], style)
    }
}