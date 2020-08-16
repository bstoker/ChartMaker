/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.domain

data class Sort(val sortBy: SortBy, val sortOrder: SortOrder) {

    companion object {
        val default = Sort(SortBy.DATE_MODIFIED, SortOrder.DESCENDING)
    }

    fun isAscending() = sortOrder == SortOrder.ASCENDING

    fun isDescending() = sortOrder == SortOrder.DESCENDING

    fun toPair() = Pair(sortBy, sortOrder)

    override fun toString() = toPair().toString()
}

enum class SortBy {
    DATE_MODIFIED,
    DESCRIPTION
}

enum class SortOrder {
    ASCENDING,
    DESCENDING
}