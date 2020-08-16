/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.explorer

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.text.color
import androidx.paging.PagedListAdapter
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.databinding.ViewPieChartItemBinding
import com.stokerapps.chartmaker.domain.Chart
import com.stokerapps.chartmaker.domain.PieChart
import com.stokerapps.chartmaker.ui.common.getColorAttribute
import com.stokerapps.chartmaker.ui.common.getColorCompat
import com.stokerapps.chartmaker.ui.piechart.updateAsIcon
import java.text.DateFormat
import java.text.SimpleDateFormat

class ChartAdapter(private val callback: Callback? = null) :
    PagedListAdapter<Chart, ChartViewHolder>(
        object : DiffUtil.ItemCallback<Chart>() {
            override fun areItemsTheSame(oldItem: Chart, newItem: Chart) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Chart, newItem: Chart) =
                oldItem == newItem
        }
    ) {

    var selectionTracker: SelectionTracker<String>? = null

    companion object {
        const val VIEW_TYPE_UNKNOWN = 0
        const val VIEW_TYPE_PIE_CHART = 1
    }

    interface Callback {
        fun onItemPressed(chart: Chart, view: View)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_PIE_CHART -> PieChartViewHolder(
                ViewPieChartItemBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )
            else -> throw IllegalStateException("Illegal view type $viewType")

        }.apply {
            itemView.setOnClickListener {
                val tag = itemView.tag
                if (tag is Chart) {
                    callback?.onItemPressed(tag, itemView)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ChartViewHolder, position: Int) {
        val chart = getItem(position)
        holder.bindTo(
            chart,
            selectionTracker?.isSelected(chart?.id.toString()) ?: false
        )
        holder.itemView.tag = chart
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is PieChart -> VIEW_TYPE_PIE_CHART
            else -> VIEW_TYPE_UNKNOWN
        }
}

abstract class ChartViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    abstract fun bindTo(chart: Chart?, isSelected: Boolean)

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<String>? =
        object : ItemDetailsLookup.ItemDetails<String>() {
            override fun getPosition(): Int = adapterPosition
            override fun getSelectionKey(): String? = (itemView.tag as? Chart)?.id.toString()
        }
}

class PieChartViewHolder(
    private val binding: ViewPieChartItemBinding
) : ChartViewHolder(binding.root) {

    companion object {
        private val dateTimeFormatter: DateFormat = SimpleDateFormat.getDateTimeInstance()
        private const val noText = ""
    }

    override fun bindTo(chart: Chart?, isSelected: Boolean) {

        with(binding) {

            root.isActivated = isSelected

            with(binding.chart) {
                updateAsIcon(
                    if (chart is PieChart && chart.entries.isNotEmpty()) {
                        chart
                    } else {
                        PieChart.placeholder
                    },
                    context.getColorCompat(R.color.colorSurface),
                    context.getColorCompat(R.color.colorOnSurface)
                )
            }

            description.text = getDescription(chart)
            dateModified.text =
                if (chart == null) noText else dateTimeFormatter.format(chart.dateModified)
        }
    }

    private fun getDescription(chart: Chart?): CharSequence =
        when (chart) {
            null -> noText
            else -> chart.getDescription() ?: when {
                chart.isEmpty() -> getGreyColoredString(R.string.empty_chart)
                else -> getGreyColoredString(R.string.no_description)
            }
        }

    private fun getGreyColoredString(@StringRes resId: Int): SpannableStringBuilder {
        val context = binding.root.context
        return SpannableStringBuilder()
            .color(context.getColorAttribute(R.attr.colorOnSurface_60)) {
                append(context.getString(resId))
            }
    }
}