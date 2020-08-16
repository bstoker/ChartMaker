/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.domain

import java.util.*

interface Chart {
    val id: UUID
    val dateCreated: Date
    var dateModified: Date
    var name: String?

    fun getDescription(): String?

    fun isEmpty(): Boolean

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int
}