/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.piechart.properties

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.stokerapps.chartmaker.ui.common.Fonts
import java.util.*

class FontSizeAdapter(val context: Context) : BaseAdapter() {

    val fontSizes = Fonts.sizes

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return if (convertView == null) {
            LayoutInflater.from(context)
                .inflate(android.R.layout.simple_spinner_item, parent, false) as TextView
        } else {
            convertView as TextView

        }.apply {
            gravity = Gravity.END
            text = format(fontSizes[position])
        }
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return if (convertView == null) {
            LayoutInflater.from(context)
                .inflate(
                    android.R.layout.simple_spinner_dropdown_item,
                    parent,
                    false
                ) as TextView
        } else {
            convertView as TextView

        }.apply {
            fontSizes[position].let { fontSize ->
                text = format(fontSize)
                textSize = fontSize
            }
        }
    }

    private fun format(size: Float) = String.format(Locale.getDefault(), "%d", size.toInt())

    fun getFontSize(position: Int) = getItem(position)

    override fun getItem(position: Int) = fontSizes[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = fontSizes.size
}