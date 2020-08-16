/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.piechart.properties

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.stokerapps.chartmaker.ui.common.Fonts

class FontFamilyAdapter(val context: Context) : BaseAdapter() {

    val fontNames = Fonts.names

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return if (convertView == null) {
            LayoutInflater.from(context)
                .inflate(android.R.layout.simple_spinner_item, parent, false) as TextView
        } else {
            convertView as TextView

        }.apply {
            fontNames[position].let { fontName ->
                text = fontName
                typeface = getTypeface(fontName)
            }
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
            fontNames[position].let { fontName ->
                text = fontName
                typeface = getTypeface(fontName)
            }
        }
    }

    private fun getTypeface(fontName: String): Typeface {
        return Fonts.get(fontName, context)
    }

    fun getFontName(position: Int): String = getItem(position)

    override fun getItem(position: Int): String = fontNames[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = fontNames.size
}