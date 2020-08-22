/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common.save_dialog.csv

import com.stokerapps.chartmaker.domain.PieChartEntry
import com.stokerapps.chartmaker.ui.common.save_dialog.csv.SaveAsCsvTask.Companion.escape
import com.stokerapps.chartmaker.ui.common.save_dialog.csv.SaveAsCsvTask.Companion.hasSpecialCharacters
import com.stokerapps.chartmaker.ui.common.save_dialog.csv.SaveAsCsvTask.Companion.quote
import com.stokerapps.chartmaker.ui.common.save_dialog.csv.SaveAsCsvTask.Companion.toLine
import org.junit.Assert.*
import org.junit.Test

class SaveAsCsvTaskTest {

    @Test
    fun testToLine() {

        fun entry(label: String, value: Float = 200f) = PieChartEntry(label = label, value = value)

        assertEquals("food,200\n", entry("food").toLine(Delimiter.COMMA))
        assertEquals("food,200.0123\n", entry("food", 200.0123f).toLine(Delimiter.COMMA))
        assertEquals("\"hello world\",200\n", entry("hello world").toLine(Delimiter.COMMA))
        assertEquals("\"\\\"hello\\\"\",200\n", entry("\"hello\"").toLine(Delimiter.COMMA))
    }

    @Test
    fun testHasSpecialCharacters() {
        assertTrue(hasSpecialCharacters(";"))
        assertTrue(hasSpecialCharacters("\t"))
        assertTrue(hasSpecialCharacters("\""))
        assertTrue(hasSpecialCharacters(":"))
        assertTrue(hasSpecialCharacters(","))
        assertTrue(hasSpecialCharacters(" "))
        assertTrue(hasSpecialCharacters("\n"))
        assertTrue(hasSpecialCharacters("\\"))
    }

    @Test
    fun testQuote() {
        assertEquals("\"\"", quote(""))
        assertEquals("\"text\"", quote("text"))
        assertEquals("\"text,comma\"", quote("text,comma"))
        assertEquals("\"text;semicolon\"", quote("text;semicolon"))
        assertEquals("\"text space\"", quote("text space"))
        assertEquals("\"text\ttab\"", quote("text\ttab"))
        assertEquals("\"text\nnewline\"", quote("text\nnewline"))
        assertEquals("\"text\"apostrophe\"\"", quote("text\"apostrophe\""))
    }

    @Test
    fun testEscape() {
        assertEquals("\\\"", escape("\""))
    }
}