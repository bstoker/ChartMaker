/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common.options_menu

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import com.stokerapps.chartmaker.databinding.ViewOptionBinding

class OptionView @kotlin.jvm.JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private lateinit var binding: ViewOptionBinding

    val icon by lazy { binding.icon }
    val text by lazy { binding.text }

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding = ViewOptionBinding.bind(this)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }
}