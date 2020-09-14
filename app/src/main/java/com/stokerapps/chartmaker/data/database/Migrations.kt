/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQueryBuilder

val MIGRATION_1_2 = object : Migration(1, 2) {

    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            database.beginTransactionNonExclusive()

            val entries = database.getEntries()
            database.deleteEntryTable()
            database.createEntryTable()
            database.insertEntries(entries.map { it.toEntryEntity2() })
            database.setTransactionSuccessful()

        } finally {
            database.endTransaction()
        }
    }

    private fun SupportSQLiteDatabase.getEntries(): List<EntryEntity1> {

        val getEntries = SupportSQLiteQueryBuilder
            .builder("entry")
            .create()

        return query(getEntries).use {
            val entries = mutableListOf<EntryEntity1>()
            while (it.moveToNext()) {
                entries.add(
                    EntryEntity1(
                        it.getInt(it.getColumnIndexOrThrow("id")),
                        Converters.toUUID(it.getString(it.getColumnIndexOrThrow("chartId"))),
                        it.getString(it.getColumnIndexOrThrow("label")),
                        it.getFloat(it.getColumnIndexOrThrow("value")),
                        it.getInt(it.getColumnIndexOrThrow("color")),
                        it.getInt(it.getColumnIndexOrThrow("order"))
                    )
                )
            }
            entries
        }
    }

    private fun SupportSQLiteDatabase.deleteEntryTable() {
        execSQL("DROP TABLE entry")
    }

    private fun SupportSQLiteDatabase.createEntryTable() {
        execSQL(
            "CREATE TABLE entry\n" +
                    "( id INTEGER NOT NULL,\n" +
                    "  chartId TEXT NOT NULL,\n" +
                    "  label TEXT NOT NULL,\n" +
                    "  value TEXT NOT NULL,\n" +
                    "  color INTEGER NOT NULL,\n" +
                    "  `order` INTEGER NOT NULL,\n" +
                    "  PRIMARY KEY (id, chartId)\n" +
                    ")"
        )
    }

    fun SupportSQLiteDatabase.insertEntries(entries: List<EntryEntity2>) {
        val values = ContentValues()
        entries.forEach { entry ->
            values.apply {
                put("id", entry.id)
                put("chartId", Converters.fromUUID(entry.chartId))
                put("label", entry.label)
                put("value", Converters.fromBigDecimal(entry.value))
                put("color", entry.color)
                put("`order`", entry.order)
            }
            insert("entry", SQLiteDatabase.CONFLICT_REPLACE, values)
            values.clear()
        }
    }
}