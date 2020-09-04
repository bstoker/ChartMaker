/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.domain.usecases

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.DataSource
import com.stokerapps.chartmaker.common.Result
import com.stokerapps.chartmaker.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.io.InputStream
import java.io.OutputStream
import java.util.*

@ExperimentalCoroutinesApi
class ImportCsvFilesTest {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    private val mainThreadSurrogate = newSingleThreadContext("main")

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Test
    fun testUserAction() = runBlockingTest {

        val repository = TestRepository()

        ImportCsvFiles(
            this,
            TestFileManager(),
            listOf(""),
            repository
        ).execute()

        val chart = repository.chart
        val entries = chart!!.entries

        assertEquals(3, entries.size)
        assertEquals("my_chart_name", repository.chart?.name)

        assertEquals("Travel Stuff", entries[0].label)
        assertEquals(20f, entries[0].value)

        assertEquals("Food", entries[1].label)
        assertEquals(10.5f, entries[1].value)

        assertEquals("Gas", entries[2].label)
        assertEquals(20f, entries[2].value)
    }

    private class TestFileManager : FileManager {

        override fun getMetaData(uriString: String): FileManager.MetaData {
            return FileManager.MetaData("my_chart_name")
        }

        override fun getMimeType(uriString: String): String? {
            return "text/tsv"
        }

        override fun read(uriString: String): InputStream {
            return ClassLoader.getSystemResourceAsStream("test.tsv")
        }

        override fun write(uriString: String): OutputStream {
            TODO("Not yet implemented")
        }

        override fun write(
            directoryUriString: String,
            filename: String,
            mimeType: String
        ): OutputStream {
            TODO("Not yet implemented")
        }
    }

    private class TestRepository : ChartRepository {

        var chart: PieChart? = null

        override fun getChartsPaged(sort: Sort): DataSource.Factory<Int, Chart> {
            TODO("Not yet implemented")
        }

        override suspend fun getPieChart(id: UUID): PieChart? {
            TODO("Not yet implemented")
        }

        override fun getPieChartFlow(id: UUID): Flow<Result<PieChart>> {
            TODO("Not yet implemented")
        }

        override suspend fun store(chart: Chart) {
            when (chart) {
                is PieChart -> store(chart)
            }
        }

        override suspend fun store(chart: PieChart) {
            this.chart = chart
        }

        override suspend fun store(entries: List<PieChartEntry>, chart: PieChart) {
            TODO("Not yet implemented")
        }

        override suspend fun delete(entry: PieChartEntry, chart: PieChart) {
            TODO("Not yet implemented")
        }

        override suspend fun delete(chart: PieChart) {
            TODO("Not yet implemented")
        }

        override suspend fun delete(charts: Collection<UUID>) {
            TODO("Not yet implemented")
        }
    }

}