package com.stokerapps.chartmaker.ui.common.color_picker

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min


class ColorWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private var huePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var saturationPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var brightnessOverlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        alpha = brightnessToAlpha(DEFAULT_BRIGHTNESS)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        radius = min(w, h) * 0.5f
        centerX = w * 0.5f
        centerY = h * 0.5f
        if (radius > 0) {
            recomputeShader()
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(centerX, centerY, radius, huePaint)
        canvas.drawCircle(centerX, centerY, radius, saturationPaint)
        canvas.drawCircle(centerX, centerY, radius, brightnessOverlayPaint)
    }

    private fun recomputeShader() {

        huePaint.shader = SweepGradient(
            centerX, centerY, intArrayOf(
                Color.RED, Color.MAGENTA, Color.BLUE, Color.CYAN,
                Color.GREEN, Color.YELLOW, Color.RED
            ), floatArrayOf(
                0.000f, 0.166f, 0.333f, 0.499f,
                0.666f, 0.833f, 0.999f
            )
        )

        saturationPaint.shader = RadialGradient(
            centerX, centerY, radius,
            Color.WHITE, 0x00FFFFFF,
            Shader.TileMode.CLAMP
        )
    }

    fun getRadius() = radius

    fun setBrightness(brightness: Int) {
        brightnessOverlayPaint.alpha = brightnessToAlpha(brightness)
        invalidate()
    }

    private fun brightnessToAlpha(brightness: Int): Int {
        return 255 - brightness
    }

    companion object {
        private const val DEFAULT_BRIGHTNESS = 224
    }
}