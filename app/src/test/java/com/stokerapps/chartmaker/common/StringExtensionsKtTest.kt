/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.common

import org.junit.Test

import org.junit.Assert.*

class StringExtensionsKtTest {

    @Test
    fun removeExtension() {
        assertEquals("file", "file.csv".removeExtension())
        assertEquals("file", "file.CSV".removeExtension())
        assertEquals("file", "file.tsv".removeExtension())
        assertEquals("file", "file.TSV".removeExtension())
        assertEquals("file", "file.YES".removeExtension())
        assertEquals("file", "file.NO".removeExtension())
        assertEquals("file.cs", "file.cs.v".removeExtension())
        assertEquals("", ".csv".removeExtension())
        assertEquals("..", "...a".removeExtension())
    }
}