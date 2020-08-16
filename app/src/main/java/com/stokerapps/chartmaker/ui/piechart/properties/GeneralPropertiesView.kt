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
import androidx.core.widget.addTextChangedListener
import com.stokerapps.chartmaker.databinding.ViewPieChartPropertyGeneralBinding
import com.stokerapps.chartmaker.ui.common.getColorAttribute
import com.stokerapps.chartmaker.ui.common.hideKeyboard
import com.stokerapps.chartmaker.ui.common.setOnKeyboardDonePress


class GeneralPropertiesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ConstraintLayout(
        context, attrs, defStyleAttr
    ) {

    interface Callback {
        fun onDescriptionClearFormatClicked()
        fun onGeneralExpandChanged(isExpanded: Boolean)
        fun onDescriptionChanged(description: String)
        fun onDescriptionFontChanged(name: String)
        fun onDescriptionTextColorClicked()
        fun onDescriptionTextSizeChanged(size: Float)
        fun onDescriptionTextStyleBoldChanged(isBold: Boolean)
        fun onDescriptionTextStyleItalicChanged(isItalic: Boolean)
        fun onDescriptionVisibilityClicked()
        fun onDonutRadiusChanged(donutRadius: Float)
        fun onDonutRadiusDecrement()
        fun onDonutRadiusIncrement()
        fun onShowDescriptionInCenterChanged(isEnabled: Boolean)
        fun onSliceSpaceChanged(sliceSpace: Float)
        fun onSliceSpaceDecrement()
        fun onSliceSpaceIncrement()
    }

    var callback: Callback? = null

    private lateinit var binding: ViewPieChartPropertyGeneralBinding

    private val fontFamilyAdapter = FontFamilyAdapter(context)
    private val fontSizeAdapter = FontSizeAdapter(context)

    override fun onFinishInflate() {
        super.onFinishInflate()

        binding = ViewPieChartPropertyGeneralBinding.bind(this)

        with(binding) {

            centerDescription.setOnCheckedChangeListener { _, isChecked ->
                callback?.onShowDescriptionInCenterChanged(isChecked)
            }

            color.setOnClickListener { callback?.onDescriptionTextColorClicked() }

            description.addTextChangedListener { text ->
                callback?.onDescriptionChanged(text.toString())
            }
            description.setOnKeyboardDonePress {
                hideKeyboard()
                clearFocus()
            }

            donutRadius.addTextChangedListener { text ->
                callback?.onDonutRadiusChanged(text?.toString()?.toFloatOrNull() ?: 0f)
            }
            donutRadiusDecrement.setOnClickListener { callback?.onDonutRadiusDecrement() }
            donutRadiusIncrement.setOnClickListener { callback?.onDonutRadiusIncrement() }

            expand.setOnClickListener { onExpandClicked() }

            fontFamily.adapter = fontFamilyAdapter
            fontFamily.onItemSelectedListener = object : OnItemSelected() {
                override fun onItemSelected(position: Int) {
                    callback?.onDescriptionFontChanged(fontFamilyAdapter.getFontName(position))
                }
            }

            fontSize.adapter = fontSizeAdapter
            fontSize.onItemSelectedListener = object : OnItemSelected() {
                override fun onItemSelected(position: Int) {
                    callback?.onDescriptionTextSizeChanged(fontSizeAdapter.getFontSize(position))
                }
            }

            formatBold.setOnClickListener { onTextStyleBoldClicked() }
            formatClear.setOnClickListener { onClearFormatClicked() }
            formatItalic.setOnClickListener { onTextStyleItalicClicked() }

            header.setOnClickListener { onExpandClicked() }

            sliceSpace.addTextChangedListener { text ->
                callback?.onSliceSpaceChanged(
                    text?.toString()?.toFloatOrNull() ?: 0f
                )
            }
            sliceSpaceDecrement.setOnClickListener { callback?.onSliceSpaceDecrement() }
            sliceSpaceIncrement.setOnClickListener { callback?.onSliceSpaceIncrement() }
        }
    }

    fun updateView(properties: GeneralProperties) {

        with(binding) {

            centerDescription.isChecked = properties.isDescriptionVisible

            color.setColor(
                properties.textColor
                    ?: context?.getColorAttribute(android.R.attr.textColor)
                    ?: Color.WHITE
            )

            if (properties.description != description.text.toString()) {
                description.setText(properties.description)
                description.clearFocus()
                description.hideKeyboard()
            }

            fontFamily.setSelection(fontFamilyAdapter.fontNames.indexOf(properties.fontName))
            fontSize.setSelection(fontSizeAdapter.fontSizes.indexOf(properties.textSize))

            formatBold.isSelected = properties.isTextStyleBold
            formatItalic.isSelected = properties.isTextStyleItalic
        }
        updateDonutRadius(properties.donutRadius)
        updateSliceSpace(properties.sliceSpace)
        updateCollapsibleMenu(properties.isExpanded, false)
    }

    fun updateDonutRadius(newDonutRadius: Float) =
        newDonutRadius.toInt().toString().let { newDonutRadiusText ->
            with(binding.donutRadius) {
                if (newDonutRadiusText != text.toString()) {
                    setText(newDonutRadiusText)
                    clearFocus()
                    hideKeyboard()
                }
            }
        }

    fun updateSliceSpace(newSliceSpace: Float) =
        newSliceSpace.toInt().toString().let { newSliceSpaceText ->
            with(binding.sliceSpace) {
                if (newSliceSpaceText != text.toString()) {
                    setText(newSliceSpaceText)
                    clearFocus()
                    hideKeyboard()
                }
            }
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
        callback?.onDescriptionClearFormatClicked()
    }

    private fun onExpandClicked() {
        val isExpanded = binding.options.isVisible.not()
        updateCollapsibleMenu(isExpanded, true)
        callback?.onGeneralExpandChanged(isExpanded)
    }

    private fun onTextStyleBoldClicked() {
        with(binding.formatBold) {
            isSelected = isSelected.not()
            callback?.onDescriptionTextStyleBoldChanged(isSelected)
        }
    }

    private fun onTextStyleItalicClicked() {
        with(binding.formatItalic) {
            isSelected = isSelected.not()
            callback?.onDescriptionTextStyleItalicChanged(isSelected)
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