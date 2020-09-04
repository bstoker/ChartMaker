/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.common

private val FILE_EXTENSION_REGEX = "^.*\\.[^.]{1,3}$".toRegex()

fun String.removeExtension() = when {
    matches(FILE_EXTENSION_REGEX) -> {
        val index = indexOfLast { it == '.' }
        subSequence(0, length - (length - index))
    }
    else -> this
}