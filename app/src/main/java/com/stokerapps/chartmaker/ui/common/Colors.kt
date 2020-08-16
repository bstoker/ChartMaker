/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

@file:Suppress("unused")

package com.stokerapps.chartmaker.ui.common

import android.graphics.Color
import android.os.Build
import androidx.annotation.IntRange
import kotlin.math.abs
import kotlin.math.sqrt

const val ACAPULCO = 0xff6aa786.toInt()
const val ATOLL = 0xff2a6d82.toInt()
const val CINNABAR = 0xffe74c3c.toInt()
const val COLUMBIA_BLUE = 0xff8ceaff.toInt()
const val CRANBERRY = 0xffd9508a.toInt()
const val DARK_GREY = 0xffa1a1a1.toInt()
const val DARK_ORANGE = 0xfffe9507.toInt()
const val ELECTRIC_VIOLET = 0xffdd00ff.toInt()
const val GOLDEN_POPPY = 0xfff5c700.toInt()
const val MATISSE = 0xff405980.toInt()
const val MEDIUM_TURQUOISE = 0xff35c2d1.toInt()
const val MINESHAFT_DARK = 0xff2b2b2b.toInt()
const val MOON_YELLOW = 0xfff1c40f.toInt()
const val MORNING_GLORY = 0xff94d4d4.toInt()
const val NEPTUNE = 0xff76aeaf.toInt()
const val NERO = 0xff212121.toInt()
const val OLD_ROSE = 0xffb33050.toInt()
const val OLIVE_DRAB = 0xff6a961f.toInt()
const val SAFETY_ORANGE = 0xffff6600.toInt()
const val SCARPA_FLOW = 0xff5a595b.toInt()
const val SHAMROCK = 0xff2ecc71.toInt()
const val SILVER = 0xffbdbdbd.toInt()
const val SUMMER_SKY = 0xff33b5e5.toInt()
const val SUVA_GREY = 0xff8d8d8d.toInt()
const val TICKLE_ME_PINK = 0xffff8c9d.toInt()
const val TRANSPARENT = 0
const val WHITE = -1
const val WHITE_SMOKE = 0xffefefef.toInt()

fun Int.luminance(): Float {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Color.luminance(this) * 255f

    } else {
        val r = Color.red(this)
        val g = Color.green(this)
        val b = Color.blue(this)
        return sqrt(r * r * .241 + g * g * .691 + b * b * .068).toFloat()
    }
}

fun Int.isBrightColor(): Boolean {
    return luminance() > 130
}

fun Int.isNotVisibleOn(color: Int): Boolean {
    return abs(luminance() - color.luminance()) < 5
}

fun rgb(
    @IntRange(from = 0, to = 255) red: Int,
    @IntRange(from = 0, to = 255) green: Int,
    @IntRange(from = 0, to = 255) blue: Int
): Int {
    return -0x1000000 or (red shl 16) or (green shl 8) or blue
}

class Hsv(var hue: Float = 0f, var saturation: Float = 1f, var value: Float = 1f) {

    companion object {
        fun fromColor(color: Int): Hsv {
            val hsv = FloatArray(3)
            Color.RGBToHSV(
                Color.red(color),
                Color.green(color),
                Color.blue(color),
                hsv
            )
            return Hsv(
                hsv[0],
                hsv[1],
                hsv[2]
            )
        }
    }

    private var hsv = FloatArray(3)

    fun toColor(): Int {
        hsv[0] = hue
        hsv[1] = saturation
        hsv[2] = value
        return Color.HSVToColor(hsv)
    }
}