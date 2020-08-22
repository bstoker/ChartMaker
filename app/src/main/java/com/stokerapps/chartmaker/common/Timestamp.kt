/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.common

import java.text.SimpleDateFormat
import java.util.*

class Timestamp {

    companion object {
        const val FORMAT = "yyyyMMdd_hhmmss"

        override fun toString(): String {
            return SimpleDateFormat(FORMAT, Locale.US).format(Date())
        }
    }

    private val date = Date()

    override fun toString(): String {
        return SimpleDateFormat(FORMAT, Locale.US).format(date)
    }
}