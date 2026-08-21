package com.lennit.cryptolyzer.persistence

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SqliteEventDatabase(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    "cryptolyzer.db",
    null,
    1
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE domain_events (
                event_id TEXT PRIMARY KEY NOT NULL,
                event_type TEXT NOT NULL,
                aggregate_id TEXT,
                occurred_at_epoch_ms INTEGER NOT NULL,
                payload_json TEXT NOT NULL,
                idempotency_key TEXT NOT NULL UNIQUE,
                attempt_count INTEGER NOT NULL DEFAULT 0,
                available_at_epoch_ms INTEGER NOT NULL,
                processed_at_epoch_ms INTEGER,
                last_error TEXT
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX idx_events_pending ON domain_events(available_at_epoch_ms, occurred_at_epoch_ms)")
        db.execSQL("CREATE INDEX idx_events_aggregate ON domain_events(aggregate_id, occurred_at_epoch_ms)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 1) onCreate(db)
    }
}
