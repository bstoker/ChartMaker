/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.new_chart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.stokerapps.chartmaker.databinding.ViewChartTileBinding
import com.stokerapps.chartmaker.domain.Chart
import com.stokerapps.chartmaker.domain.PieChart
import com.stokerapps.chartmaker.ui.piechart.updateAsIcon

const val VIEW_TYPE_PIE_CHART = 0

internal class NewChartAdapter(val callback: Callback) : RecyclerView.Adapter<ViewHolder>() {

    interface Callback {
        fun onChartPressed(chart: Chart)
    }

    private val items = mutableListOf<Chart>()
    private val onItemClick = View.OnClickListener { v ->
        v?.let { view ->
            callback.onChartPressed(view.tag as Chart)
        }
    }

    fun update(items: List<Chart>) {
        this.items.clear()
        this.items.addAll(items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_PIE_CHART -> PieChartViewHolder(
                ViewChartTileBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException("Unknown view type!")
        }.apply {
            itemView.setOnClickListener(onItemClick)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chart = items[position]
        holder.itemView.tag = chart
        holder.bindTo(chart)
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) = when (items[position]) {
        is PieChart -> VIEW_TYPE_PIE_CHART
        else -> throw IllegalArgumentException("Could not determine view type for ${items[position]}!")
    }
}

abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bindTo(chart: Chart)
}

class PieChartViewHolder(private val binding: ViewChartTileBinding) : ViewHolder(binding.root) {

    override fun bindTo(chart: Chart) {
        if (chart is PieChart) {
            binding.chart.updateAsIcon(chart)
        }
    }
}