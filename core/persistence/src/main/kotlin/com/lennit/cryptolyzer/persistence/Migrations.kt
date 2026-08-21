package com.lennit.cryptolyzer.persistence

import java.sql.Connection

/**
 * Forward-only, numbered schema migrations.
 *
 * Rules that this list must keep obeying, because an installed app's database cannot be dropped
 * and recreated the way a server's can:
 *  1. never edit a shipped migration, only append a new one;
 *  2. every migration is idempotent where SQLite allows it (IF NOT EXISTS);
 *  3. no migration deletes event rows.
 */
internal object Migrations {

    val statements: List<List<String>> = listOf(
        // --- version 1: durable event log -------------------------------------------------
        listOf(
            """
            CREATE TABLE IF NOT EXISTS events (
                event_id         TEXT    PRIMARY KEY,
                type             TEXT    NOT NULL,
                schema_version   INTEGER NOT NULL,
                producer         TEXT    NOT NULL,
                occurred_at      INTEGER NOT NULL,
                recorded_at      INTEGER NOT NULL,
                idempotency_key  TEXT    NOT NULL UNIQUE,
                payload          TEXT    NOT NULL,
                trace_id         TEXT,
                status           TEXT    NOT NULL,
                attempt          INTEGER NOT NULL DEFAULT 0,
                next_attempt_at  INTEGER NOT NULL,
                lease_expires_at INTEGER,
                last_error       TEXT
            )
            """.trimIndent(),
            // Drives claimPending: the only query on the hot path.
            "CREATE INDEX IF NOT EXISTS idx_events_claim ON events (status, next_attempt_at)",
            // Drives lease-expiry recovery after process death.
            "CREATE INDEX IF NOT EXISTS idx_events_lease ON events (status, lease_expires_at)",
            "CREATE INDEX IF NOT EXISTS idx_events_type_time ON events (type, occurred_at)",
        ),
    )

    val currentVersion: Int get() = statements.size

    fun apply(connection: Connection) {
        connection.createStatement().use { statement ->
            statement.execute("CREATE TABLE IF NOT EXISTS schema_meta (version INTEGER NOT NULL)")
        }
        val installed = readVersion(connection)
        if (installed >= currentVersion) return

        connection.autoCommit = false
        try {
            for (version in (installed + 1)..currentVersion) {
                connection.createStatement().use { statement ->
                    statements[version - 1].forEach(statement::execute)
                }
            }
            writeVersion(connection, currentVersion)
            connection.commit()
        } catch (error: Throwable) {
            connection.rollback()
            throw error
        } finally {
            connection.autoCommit = true
        }
    }

    private fun readVersion(connection: Connection): Int =
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT version FROM schema_meta LIMIT 1").use { rows ->
                if (rows.next()) rows.getInt(1) else 0
            }
        }

    private fun writeVersion(connection: Connection, version: Int) {
        connection.createStatement().use { statement ->
            statement.execute("DELETE FROM schema_meta")
            statement.execute("INSERT INTO schema_meta (version) VALUES ($version)")
        }
    }
}
