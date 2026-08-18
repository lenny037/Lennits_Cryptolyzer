package com.lennit.cryptolyzer.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS agent_memory (" +
                "namespace TEXT NOT NULL, " +
                "`key` TEXT NOT NULL, " +
                "valueJson TEXT NOT NULL, " +
                "updatedAtEpochMs INTEGER NOT NULL, " +
                "PRIMARY KEY(namespace, `key`)" +
                ")"
        )
    }
}
