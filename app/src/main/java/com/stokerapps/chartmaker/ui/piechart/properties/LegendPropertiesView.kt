/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.piechart.properties

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.stokerapps.chartmaker.databinding.ViewPieChartPropertyLegendBinding
import com.stokerapps.chartmaker.ui.common.getColorAttribute


class LegendPropertiesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ConstraintLayout(
        context, attrs, defStyleAttr
    ) {

    interface Callback {
        fun onLegendClearFormatClicked()
        fun onLegendExpandChanged(isExpanded: Boolean)
        fun onLegendFontChanged(name: String)
        fun onLegendTextColorClicked()
        fun onLegendTextSizeChanged(size: Float)
        fun onLegendTextStyleBoldChanged(isBold: Boolean)
        fun onLegendTextStyleItalicChanged(isItalic: Boolean)
        fun onLegendVisibilityClicked()
    }

    var callback: Callback? = null

    private lateinit var binding: ViewPieChartPropertyLegendBinding

    private val fontFamilyAdapter = FontFamilyAdapter(context)
    private val fontSizeAdapter = FontSizeAdapter(context)

    override fun onFinishInflate() {
        super.onFinishInflate()

        binding = ViewPieChartPropertyLegendBinding.bind(this)

        with(binding) {

            color.setOnClickListener { callback?.onLegendTextColorClicked() }
            expand.setOnClickListener { onExpandClicked() }

            fontFamily.adapter = fontFamilyAdapter
            fontFamily.onItemSelectedListener = object : OnItemSelected() {
                override fun onItemSelected(position: Int) {
                    callback?.onLegendFontChanged(fontFamilyAdapter.getFontName(position))
                }
            }

            fontSize.adapter = fontSizeAdapter
            fontSize.onItemSelectedListener = object : OnItemSelected() {
                override fun onItemSelected(position: Int) {
                    callback?.onLegendTextSizeChanged(fontSizeAdapter.getFontSize(position))
                }
            }

            formatBold.setOnClickListener { onTextStyleBoldClicked() }
            formatClear.setOnClickListener { onClearFormatClicked() }
            formatItalic.setOnClickListener { onTextStyleItalicClicked() }

            header.setOnClickListener { onExpandClicked() }

            visible.setOnClickListener {
                it.isActivated = it.isActivated.not()
                callback?.onLegendVisibilityClicked()
            }
        }
    }

    fun updateView(data: LegendProperties) {

        with(binding) {

            color.setColor(
                data.textColor
                    ?: context?.getColorAttribute(android.R.attr.textColor)
                    ?: Color.WHITE
            )

            fontFamily.setSelection(fontFamilyAdapter.fontNames.indexOf(data.fontName))
            fontSize.setSelection(fontSizeAdapter.fontSizes.indexOf(data.textSize))

            formatBold.isSelected = data.isTextStyleBold
            formatItalic.isSelected = data.isTextStyleItalic

            visible.isActivated = data.isVisible
        }
        updateCollapsibleMenu(data.isExpanded, false)
    }

    private fun updateCollapsibleMenu(isExpanded: Boolean, fromUser: Boolean) {
        binding.options.visibility = if (isExpanded) View.VISIBLE else View.GONE
        if (fromUser) {
            binding.expand.animate().setDuration(200).rotation(if (isExpanded) 0f else -90f)
        } else {
            binding.expand.rotation = if (isExpanded) 0f else -90f
        }
    }

    private fun onClearFormatClicked() {
        with(binding) {
            formatBold.isSelected = false
            formatItalic.isSelected = false
        }
        callback?.onLegendClearFormatClicked()
    }

    private fun onExpandClicked() {
        val isExpanded = binding.options.isVisible.not()
        updateCollapsibleMenu(isExpanded, true)
        callback?.onLegendExpandChanged(isExpanded)
    }

    private fun onTextStyleBoldClicked() {
        with(binding.formatBold) {
            isSelected = isSelected.not()
            callback?.onLegendTextStyleBoldChanged(isSelected)
        }
    }

    private fun onTextStyleItalicClicked() {
        with(binding.formatItalic) {
            isSelected = isSelected.not()
            callback?.onLegendTextStyleItalicChanged(isSelected)
        }
    }

    abstract class OnItemSelected : AdapterView.OnItemSelectedListener {

        abstract fun onItemSelected(position: Int)

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            onItemSelected(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }
}