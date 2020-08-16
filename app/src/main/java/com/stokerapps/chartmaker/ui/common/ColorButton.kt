/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import com.stokerapps.chartmaker.R
import timber.log.Timber


class ColorButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var color: Int = TRANSPARENT
    private val colorBackground by lazy { context.getColorCompat(R.color.colorSurface) }
    private val colorBorder by lazy { context.getColorCompat(R.color.colorOnSurface) }

    init {
        isSaveEnabled = true
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return SavedState(superState, color)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        setColor(savedState.getColor())
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (drawable == null) {
            super.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.color_button,
                    context.theme
                )
            )
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        Timber.w("Setting a custom drawable can break ColorButton.setColor()")
    }

    fun getColor() = color

    fun setColor(color: Int) {
        this.color = color
        drawable?.let { drawable ->
            if (drawable is GradientDrawable) {
                val strokeWidth = if (color.isNotVisibleOn(colorBackground)) 1.dp else 0
                drawable.setColor(color)
                drawable.setStroke(strokeWidth, colorBorder)
            }
        }
    }

    class SavedState : BaseSavedState {

        private var color: Int = TRANSPARENT

        constructor(parcelable: Parcelable?, color: Int) : super(parcelable) {
            this.color = color
        }

        private constructor(parcel: Parcel) : super(parcel) {
            color = parcel.readInt()
        }

        fun getColor() = color

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeInt(color)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}