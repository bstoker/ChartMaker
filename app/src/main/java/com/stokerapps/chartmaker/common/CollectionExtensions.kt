/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.common

fun <E> MutableCollection<E>.setAll(elements: Collection<E>) {
    clear()
    addAll(elements)
}