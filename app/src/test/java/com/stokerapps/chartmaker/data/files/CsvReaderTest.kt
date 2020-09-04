/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data.files

import org.junit.Assert.assertEquals
import org.junit.Test

class CsvReaderTest {

    @Test
    fun testCsvReader() {

        val csvReader = CsvReader()

        ClassLoader.getSystemResourceAsStream("test.csv").use { input ->
            assertEquals(
                listOf(
                    listOf("Food", "20"),
                    listOf("Travel Stuff", "10.5"),
                    listOf("comma, in text", "20"),
                    listOf("", "10"),
                    listOf("tab\tin text", "10"),
                    listOf("\"quote\"", "5")
                ), csvReader.readAll(input)
            )
        }

        ClassLoader.getSystemResourceAsStream("test.tsv").use { input ->
            assertEquals(
                listOf(
                    listOf("Travel Stuff", "20"),
                    listOf("Food", "10.5"),
                    listOf("Gas")
                ), csvReader.readAll(input, "text/tsv")
            )
        }

        ClassLoader.getSystemResourceAsStream("test.tsv").use { input ->
            assertEquals(
                listOf(
                    listOf("Travel", "Stuff", "20"),
                    listOf("Food", "10.5"),
                    listOf("Gas")
                ), csvReader.readAll(input)
            )
        }
    }
}