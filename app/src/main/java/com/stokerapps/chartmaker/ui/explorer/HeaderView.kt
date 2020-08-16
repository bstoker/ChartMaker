/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.explorer

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.stokerapps.chartmaker.databinding.ViewChartsHeaderBinding
import com.stokerapps.chartmaker.domain.Sort
import com.stokerapps.chartmaker.domain.SortBy
import com.stokerapps.chartmaker.domain.SortOrder

internal class HeaderView @kotlin.jvm.JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ConstraintLayout(
        context, attrs, defStyleAttr
    ) {

    var callback: SortChangedCallback? = null

    private lateinit var binding: ViewChartsHeaderBinding

    override fun onFinishInflate() {
        super.onFinishInflate()

        binding = ViewChartsHeaderBinding.bind(this)
        binding.dateModified.setOnClickListener { onDateModifiedPressed() }
        binding.description.setOnClickListener { onDescriptionPressed() }
    }

    private fun onDateModifiedPressed() {
        val isSelected = binding.dateModified.isSelected.not()
            .also { binding.dateModified.isSelected = it }
        val sortOrder = if (isSelected) SortOrder.ASCENDING else SortOrder.DESCENDING
        onSortChanged(Sort(SortBy.DATE_MODIFIED, sortOrder))
    }

    private fun onDescriptionPressed() {
        val isSelected = binding.description.isSelected.not()
            .also { binding.description.isSelected = it }
        val sortOrder = if (isSelected) SortOrder.ASCENDING else SortOrder.DESCENDING
        onSortChanged(Sort(SortBy.DESCRIPTION, sortOrder))
    }

    private fun onSortChanged(sort: Sort) {
        callback?.onSortChanged(sort)
    }

    fun update(sort: Sort) {
        with(binding) {
            when (sort.toPair()) {
                Pair(SortBy.DATE_MODIFIED, SortOrder.ASCENDING) -> {
                    dateModifiedArrowDown.visibility = View.GONE
                    dateModifiedArrowUp.visibility = View.VISIBLE
                    descriptionArrowDown.visibility = View.GONE
                    descriptionArrowUp.visibility = View.GONE
                }
                Pair(SortBy.DATE_MODIFIED, SortOrder.DESCENDING) -> {
                    dateModifiedArrowDown.visibility = View.VISIBLE
                    dateModifiedArrowUp.visibility = View.GONE
                    descriptionArrowDown.visibility = View.GONE
                    descriptionArrowUp.visibility = View.GONE
                }
                Pair(SortBy.DESCRIPTION, SortOrder.ASCENDING) -> {
                    dateModifiedArrowDown.visibility = View.GONE
                    dateModifiedArrowUp.visibility = View.GONE
                    descriptionArrowDown.visibility = View.GONE
                    descriptionArrowUp.visibility = View.VISIBLE
                }
                Pair(SortBy.DESCRIPTION, SortOrder.DESCENDING) -> {
                    dateModifiedArrowDown.visibility = View.GONE
                    dateModifiedArrowUp.visibility = View.GONE
                    descriptionArrowDown.visibility = View.VISIBLE
                    descriptionArrowUp.visibility = View.GONE
                }
            }
        }
    }
}