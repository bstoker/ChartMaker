/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.stokerapps.chartmaker.domain.ValueType
import com.stokerapps.chartmaker.ui.common.Fonts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    companion object {
        const val TEST_DB = "migration-test"

        private val ALL_MIGRATIONS = arrayOf(
            MIGRATION_1_2
        )
    }

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrateAll() {

        helper.createDatabase(TEST_DB, 1).apply {
            close()
        }

        Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java,
            TEST_DB
        ).addMigrations(*ALL_MIGRATIONS).build().apply {
            openHelper.writableDatabase
            close()
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {

        val chart = ChartEntity(
            UUID.randomUUID(),
            areLabelsVisible = true,
            areValuesVisible = true,
            currencyCode = "EUR",
            dateCreated = Date(),
            dateModified = Date(),
            descriptionFontName = Fonts.DEFAULT_FONT_NAME,
            descriptionTextColor = Color.BLACK,
            descriptionTextSize = Fonts.DEFAULT_TEXT_SIZE,
            descriptionTextStyleBold = false,
            descriptionTextStyleItalic = false,
            donutRadius = 0f,
            isDescriptionVisible = false,
            isLegendVisible = true,
            isRotationEnabled = false,
            labelFontName = Fonts.DEFAULT_FONT_NAME,
            labelTextColor = Color.BLACK,
            labelTextSize = Fonts.DEFAULT_TEXT_SIZE,
            labelTextStyleBold = false,
            labelTextStyleItalic = false,
            legendFontName = Fonts.DEFAULT_FONT_NAME,
            legendTextColor = Color.BLACK,
            legendTextSize = Fonts.DEFAULT_TEXT_SIZE,
            legendTextStyleBold = false,
            legendTextStyleItalic = false,
            name = "name",
            sliceSpace = 3f,
            valueDecimals = 0,
            valueFontName = Fonts.DEFAULT_FONT_NAME,
            valueTextColor = Color.BLACK,
            valueTextSize = Fonts.DEFAULT_TEXT_SIZE,
            valueTextStyleBold = false,
            valueTextStyleItalic = false,
            valueType = ValueType.PERCENTAGE,
            version = 1
        )
        val entry1 = EntryEntity1(1, chart.id, "1", 1f, Color.RED, 1)
        val entry2 = EntryEntity1(2, chart.id, "2", 2f, Color.WHITE, 2)

        helper.createDatabase(TEST_DB, 1).apply {

            val values = ContentValues()

            insert("chart", SQLiteDatabase.CONFLICT_REPLACE, chart.toContentValues(values))
            insert("entry", SQLiteDatabase.CONFLICT_REPLACE, entry1.toContentValues(values))
            insert("entry", SQLiteDatabase.CONFLICT_REPLACE, entry2.toContentValues(values))
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2).apply {

            val entry1FromDb = getEntry(entry1.id, chart.id)!!
            assertNotNull(entry1FromDb)
            assertEquals(entry1.id, entry1FromDb.id)
            assertEquals(entry1.chartId, entry1FromDb.chartId)
            assertEquals(entry1.label, entry1FromDb.label)
            assertEquals(entry1.value.toBigDecimal(), entry1FromDb.value)
            assertEquals(entry1.color, entry1FromDb.color)
            assertEquals(entry1.order, entry1FromDb.order)

            val entry2FromDb = getEntry(entry2.id, chart.id)!!
            assertNotNull(entry2FromDb)
            assertEquals(entry2.id, entry2FromDb.id)
            assertEquals(entry2.chartId, entry2FromDb.chartId)
            assertEquals(entry2.label, entry2FromDb.label)
            assertEquals(entry2.value.toBigDecimal(), entry2FromDb.value)
            assertEquals(entry2.color, entry2FromDb.color)
            assertEquals(entry2.order, entry2FromDb.order)

            close()
        }
    }

    private fun SupportSQLiteDatabase.getEntry(id: Int, chartId: UUID): EntryEntity2? {

        val getEntry = SupportSQLiteQueryBuilder
            .builder("entry")
            .selection("id = ? AND chartId = ?", arrayOf(id, Converters.fromUUID(chartId)))
            .create()

        return query(getEntry).use {
            when {
                it.moveToNext() ->
                    EntryEntity2(
                        it.getInt(it.getColumnIndexOrThrow("id")),
                        Converters.toUUID(it.getString(it.getColumnIndexOrThrow("chartId"))),
                        it.getString(it.getColumnIndexOrThrow("label")),
                        Converters.toBigDecimal(it.getString(it.getColumnIndexOrThrow("value"))),
                        it.getInt(it.getColumnIndexOrThrow("color")),
                        it.getInt(it.getColumnIndexOrThrow("order"))
                    )
                else -> null
            }
        }
    }
}