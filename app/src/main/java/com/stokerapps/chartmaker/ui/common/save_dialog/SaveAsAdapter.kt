/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common.save_dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.databinding.ViewCheckedTextBinding

class SaveAsAdapter(private val callback: Callback) :
    RecyclerView.Adapter<SaveAsAdapter.ViewHolder>() {

    interface Callback {
        fun onItemClicked(item: SaveAs)
    }

    private val items = SaveAs.values()

    var selected: SaveAs? = null
        set(value) {
            val previousSelectedIndex = items.indexOf(field)
            val selectedIndex = items.indexOf(value)
            field = value
            notifyItemChanged(previousSelectedIndex)
            notifyItemChanged(selectedIndex)
        }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.view_text, parent, false) as TextView
        ).apply {
            view.setOnClickListener {
                callback.onItemClicked(view.tag as SaveAs)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.view.tag = item
        holder.view.isSelected = item == selected
        holder.view.setText(item.description)
    }

    override fun getItemCount() = items.size

    override fun getItemId(position: Int) = position.toLong()

    class ViewHolder(val view: TextView) : RecyclerView.ViewHolder(view)
}