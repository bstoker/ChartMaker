/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.piechart.properties

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.stokerapps.chartmaker.databinding.ViewPieChartPropertyLabelsBinding


class LabelPropertiesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ConstraintLayout(
        context, attrs, defStyleAttr
    ) {

    interface Callback {
        fun onLabelClearFormatClicked()
        fun onLabelExpandChanged(isExpanded: Boolean)
        fun onLabelFontChanged(name: String)
        fun onLabelTextColorClicked()
        fun onLabelTextSizeChanged(size: Float)
        fun onLabelTextStyleBoldChanged(isBold: Boolean)
        fun onLabelTextStyleItalicChanged(isItalic: Boolean)
        fun onLabelVisibilityClicked()
    }

    var callback: Callback? = null

    private lateinit var binding: ViewPieChartPropertyLabelsBinding

    private val fontFamilyAdapter = FontFamilyAdapter(context)
    private val fontSizeAdapter = FontSizeAdapter(context)

    override fun onFinishInflate() {
        super.onFinishInflate()

        binding = ViewPieChartPropertyLabelsBinding.bind(this)

        with(binding) {

            color.setOnClickListener { callback?.onLabelTextColorClicked() }

            expand.setOnClickListener { onExpandClicked() }

            fontFamily.adapter = fontFamilyAdapter
            fontFamily.onItemSelectedListener = object : OnItemSelected() {
                override fun onItemSelected(position: Int) {
                    callback?.onLabelFontChanged(fontFamilyAdapter.getFontName(position))
                }
            }

            fontSize.adapter = fontSizeAdapter
            fontSize.onItemSelectedListener = object : OnItemSelected() {
                override fun onItemSelected(position: Int) {
                    callback?.onLabelTextSizeChanged(fontSizeAdapter.getFontSize(position))
                }
            }

            formatBold.setOnClickListener { onTextStyleBoldClicked() }
            formatClear.setOnClickListener { onClearFormatClicked() }
            formatItalic.setOnClickListener { onTextStyleItalicClicked() }

            header.setOnClickListener { onExpandClicked() }

            visible.setOnClickListener {
                it.isActivated = it.isActivated.not()
                callback?.onLabelVisibilityClicked()
            }
        }
    }

    fun updateView(data: LabelProperties) {

        with(binding) {

            color.setColor(data.textColor)

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
        callback?.onLabelClearFormatClicked()
    }

    private fun onExpandClicked() {
        val isExpanded = binding.options.isVisible.not()
        updateCollapsibleMenu(isExpanded, true)
        callback?.onLabelExpandChanged(isExpanded)
    }

    private fun onTextStyleBoldClicked() {
        with(binding.formatBold) {
            isSelected = isSelected.not()
            callback?.onLabelTextStyleBoldChanged(isSelected)
        }
    }

    private fun onTextStyleItalicClicked() {
        with(binding.formatItalic) {
            isSelected = isSelected.not()
            callback?.onLabelTextStyleItalicChanged(isSelected)
        }
    }

    abstract inner class OnItemSelected : AdapterView.OnItemSelectedListener {

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