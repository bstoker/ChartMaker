/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.piechart.properties

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.databinding.ViewPieChartPropertyValuesBinding
import com.stokerapps.chartmaker.domain.ValueType
import java.util.*


class ValuePropertiesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ConstraintLayout(
        context, attrs, defStyleAttr
    ) {

    interface Callback {
        fun onValueClearFormatClicked()
        fun onValueCurrencyClicked()
        fun onValueCurrencyChanged(currencyCode: String)
        fun onValueDecimalsChanged(decimals: Int)
        fun onValueExpandChanged(isExpanded: Boolean)
        fun onValueFontChanged(name: String)
        fun onValueTextColorClicked()
        fun onValueTextSizeChanged(size: Float)
        fun onValueTextStyleBoldChanged(isBold: Boolean)
        fun onValueTextStyleItalicChanged(isItalic: Boolean)
        fun onValueTypeChanged(type: ValueType)
        fun onValueVisibilityClicked()
    }

    var callback: Callback? = null

    private lateinit var binding: ViewPieChartPropertyValuesBinding

    private val fontFamilyAdapter = FontFamilyAdapter(context)
    private val fontSizeAdapter = FontSizeAdapter(context)

    override fun onFinishInflate() {
        super.onFinishInflate()

        binding = ViewPieChartPropertyValuesBinding.bind(this)

        with(binding) {

            color.setOnClickListener { callback?.onValueTextColorClicked() }
            currency.setOnClickListener {
                onValueTypeClicked(ValueType.CURRENCY)
                callback?.onValueCurrencyClicked()
            }

            decimals.adapter = createDecimalsAdapter()
            decimals.onItemSelectedListener = object : OnItemSelected() {
                override fun onItemSelected(position: Int) {
                    callback?.onValueDecimalsChanged(position)
                }
            }

            expand.setOnClickListener { onExpandClicked() }

            fontFamily.adapter = fontFamilyAdapter
            fontFamily.onItemSelectedListener = object : OnItemSelected() {
                override fun onItemSelected(position: Int) {
                    callback?.onValueFontChanged(fontFamilyAdapter.getFontName(position))
                }
            }

            fontSize.adapter = fontSizeAdapter
            fontSize.onItemSelectedListener = object : OnItemSelected() {
                override fun onItemSelected(position: Int) {
                    callback?.onValueTextSizeChanged(fontSizeAdapter.getFontSize(position))
                }
            }

            formatBold.setOnClickListener { onTextStyleBoldClicked() }
            formatClear.setOnClickListener { onClearFormatClicked() }
            formatItalic.setOnClickListener { onTextStyleItalicClicked() }

            header.setOnClickListener { onExpandClicked() }

            number.setOnClickListener { onValueTypeClicked(ValueType.NUMBER) }
            percentage.setOnClickListener { onValueTypeClicked(ValueType.PERCENTAGE) }

            visible.setOnClickListener {
                it.isActivated = it.isActivated.not()
                callback?.onValueVisibilityClicked()
            }
        }
    }

    fun updateView(data: ValueProperties) {

        with(binding) {

            color.setColor(data.textColor)

            decimals.setSelection(data.decimals, false)
            fontFamily.setSelection(fontFamilyAdapter.fontNames.indexOf(data.fontName))
            fontSize.setSelection(fontSizeAdapter.fontSizes.indexOf(data.textSize))

            formatBold.isSelected = data.isTextStyleBold
            formatItalic.isSelected = data.isTextStyleItalic

            visible.isActivated = data.isVisible
        }

        updateCollapsibleMenu(data.isExpanded, false)
        updateCurrencyView(data.currencyCode)
        updateValueTypeButtons(data.type)
    }

    private fun updateCollapsibleMenu(isExpanded: Boolean, fromUser: Boolean) {
        binding.options.visibility = if (isExpanded) View.VISIBLE else View.GONE
        if (fromUser) {
            binding.expand.animate().setDuration(200).rotation(if (isExpanded) 0f else -90f)
        } else {
            binding.expand.rotation = if (isExpanded) 0f else -90f
        }
    }

    fun updateCurrencyView(currencyCode: String) {
        binding.currency.text = getCurrencySymbol(currencyCode)
    }

    private fun getCurrencySymbol(currencyCode: String): String {
        return try {
            Currency.getInstance(currencyCode).symbol
        } catch (e: IllegalArgumentException) {
            "€"
        }
    }

    private fun updateValueTypeButtons(type: ValueType) {
        with(binding) {
            currency.isSelected = type == ValueType.CURRENCY
            currency.isActivated = type == ValueType.CURRENCY
            number.isSelected = type == ValueType.NUMBER
            percentage.isSelected = type == ValueType.PERCENTAGE
        }
    }

    private fun onClearFormatClicked() {
        with(binding) {
            formatBold.isSelected = false
            formatItalic.isSelected = false
        }
        callback?.onValueClearFormatClicked()
    }

    private fun onExpandClicked() {
        val isExpanded = binding.options.isVisible.not()
        updateCollapsibleMenu(isExpanded, true)
        callback?.onValueExpandChanged(isExpanded)
    }

    private fun onTextStyleBoldClicked() {
        with(binding.formatBold) {
            isSelected = isSelected.not()
            callback?.onValueTextStyleBoldChanged(isSelected)
        }
    }

    private fun onTextStyleItalicClicked() {
        with(binding.formatItalic) {
            isSelected = isSelected.not()
            callback?.onValueTextStyleItalicChanged(isSelected)
        }
    }

    private fun onValueTypeClicked(type: ValueType) {
        callback?.onValueTypeChanged(type)
        updateValueTypeButtons(type)
    }

    private fun createDecimalsAdapter() = ArrayAdapter.createFromResource(
        context,
        R.array.decimals,
        android.R.layout.simple_spinner_item
    ).apply {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
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