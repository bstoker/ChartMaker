/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.domain

import com.stokerapps.chartmaker.common.setAll
import java.util.*


data class Editor(
    val id: Int,
    private val _colors: LinkedList<Int>,
    val selectedEditTab: EditTab,
    val sort: Sort,
    val isEditSidebarExpanded: Boolean,
    val isGeneralPropertySectionExpanded: Boolean,
    val isLabelPropertySectionExpanded: Boolean,
    val isLegendPropertySectionExpanded: Boolean,
    val isValuePropertySectionExpanded: Boolean
) {
    enum class EditTab {
        COLLABORATION, ENTRIES, PROPERTIES
    }

    companion object {

        private val random = Random()

        private const val MAX_COLORS = 16

        val default = Editor(
            random.nextInt(),
            LinkedList(),
            EditTab.ENTRIES,
            Sort.default,
            isEditSidebarExpanded = true,
            isGeneralPropertySectionExpanded = true,
            isLabelPropertySectionExpanded = false,
            isLegendPropertySectionExpanded = false,
            isValuePropertySectionExpanded = true
        )
    }

    val colors: List<Int> = _colors

    fun addColor(color: Int): Boolean {

        if (_colors.firstOrNull() == color) {
            return false
        }

        val removed = _colors.remove(color)
        _colors.addFirst(color)

        if (!removed && _colors.size > MAX_COLORS) {
            setColors(_colors.take(MAX_COLORS))
        }
        return true
    }

    private fun setColors(colors: List<Int>) {
        _colors.setAll(if (colors.size > MAX_COLORS) colors.take(MAX_COLORS) else colors)
    }

    override fun toString() = "Editor@${hashCode().toUInt().toString(16)}"
}