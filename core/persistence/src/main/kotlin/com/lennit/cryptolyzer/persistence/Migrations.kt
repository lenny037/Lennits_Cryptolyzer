package com.lennit.cryptolyzer.persistence

/**
 * The released schema history.
 *
 * Rules, restated because they are enforced by [SqliteMigrator] and by
 * `core/persistence/src/test/resources/fixtures`, not by good intentions:
 *
 *  1. **Never edit a released entry.** Its checksum is recorded in every database it built.
 *     Append a new [MigrationDefinition] instead. `SqliteMigratorTest` proves an edit is caught.
 *  2. **Never delete event rows.** The log is the audit trail; a migration that loses rows loses
 *     the only record of what the system decided.
 *  3. **Use `IF NOT EXISTS` where SQLite allows it,** so a partially applied migration from an
 *     older, pre-ledger build can still be completed.
 *
 * Authoring guide: `docs/persistence/MIGRATIONS.md`.
 */
public object Migrations {

    /**
     * Version 1 — the durable event log.
     *
     * Frozen. Every database in the field was built by exactly this SQL, and
     * `core/persistence/src/test/resources/fixtures/schema-v1` is a text copy of the result.
     */
    private val DURABLE_EVENT_LOG = MigrationDefinition(
        version = 1,
        name = "durable_event_log",
        statements = listOf(
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

    /** Released migrations, in order. Append only. */
    public val RELEASED: List<MigrationDefinition> = listOf(DURABLE_EVENT_LOG)

    /** The schema version this build expects. */
    public val currentVersion: Int get() = RELEASED.size
}
