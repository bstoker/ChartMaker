/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.explorer

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.databinding.ViewCheckedTextBinding
import com.stokerapps.chartmaker.databinding.ViewSortBinding
import com.stokerapps.chartmaker.domain.Sort
import com.stokerapps.chartmaker.domain.SortBy
import com.stokerapps.chartmaker.domain.SortOrder

internal class SortView @kotlin.jvm.JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    var callback: SortChangedCallback? = null

    private lateinit var binding: ViewSortBinding

    override fun onFinishInflate() {
        super.onFinishInflate()

        binding = ViewSortBinding.bind(this)
        with(binding) {

            dateModified.text.setText(R.string.date_modified)
            dateModified.root.setOnClickListener { onSortByPressed(SortBy.DATE_MODIFIED) }

            description.text.setText(R.string.description)
            description.root.setOnClickListener { onSortByPressed(SortBy.DESCRIPTION) }

            ascending.text.setText(R.string.ascending)
            ascending.root.setOnClickListener { onSortOrderPressed(SortOrder.ASCENDING) }

            descending.text.setText(R.string.descending)
            descending.root.setOnClickListener { onSortOrderPressed(SortOrder.DESCENDING) }
        }
    }

    fun update(sort: Sort) {
        with(binding) {
            dateModified.isSelected = sort.sortBy == SortBy.DATE_MODIFIED
            description.isSelected = sort.sortBy == SortBy.DESCRIPTION
            ascending.isSelected = sort.isAscending()
            descending.isSelected = sort.isDescending()
        }
    }

    private fun onSortByPressed(sortBy: SortBy) {
        val sort = Sort(
            sortBy,
            if (binding.ascending.root.isSelected) SortOrder.ASCENDING else SortOrder.DESCENDING
        )
        update(sort)
        callback?.onSortChanged(sort)
    }

    private fun onSortOrderPressed(sortOrder: SortOrder) {
        callback?.onSortChanged(
            Sort(
                if (binding.dateModified.isSelected) SortBy.DATE_MODIFIED else SortBy.DESCRIPTION,
                sortOrder
            )
        )
    }

    private var ViewCheckedTextBinding.isSelected: Boolean
        get() = root.isSelected
        set(value) {
            checked.isVisible = value
            root.isSelected = value
        }
}