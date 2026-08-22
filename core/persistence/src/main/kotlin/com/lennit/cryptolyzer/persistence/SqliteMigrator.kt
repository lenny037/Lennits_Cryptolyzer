package com.lennit.cryptolyzer.persistence

import com.lennit.cryptolyzer.contracts.Clock
import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.contracts.PlatformError
import java.sql.Connection

/**
 * Applies [MigrationDefinition]s to a SQLite database, forward only, once each, and verifiably.
 *
 * The ledger table `schema_migrations` records every applied version with the checksum of the SQL
 * that was applied. That record is what makes the following failures loud instead of silent:
 *
 *  - **an edited shipped migration** — the recorded checksum no longer matches the compiled one.
 *    There is no safe automatic response, because the database in hand may have been built either
 *    way; the only honest action is to refuse to open and say so.
 *  - **a database from a newer build** — a downgraded application must not run against a schema it
 *    does not understand. Refusing to open loses nothing; opening may destroy data.
 *  - **a gap in the ledger** — versions must be contiguous. A gap means a migration was skipped or
 *    the ledger was edited, and neither is recoverable by guessing.
 *
 * Each migration runs in its own transaction and is recorded in the same transaction, so a crash
 * between two migrations leaves a consistent, resumable database rather than a half-applied one.
 *
 * `schema_meta` is kept in step with the ledger for compatibility with databases written before the
 * ledger existed, and because the Android/Room build reads a single version integer.
 */
public class SqliteMigrator(
    public val definitions: List<MigrationDefinition>,
    private val clock: Clock = Clock.system(),
) {
    init {
        require(definitions.isNotEmpty()) { "A migrator needs at least one migration" }
        val versions = definitions.map { it.version }
        require(versions == versions.sorted()) { "Migrations must be declared in version order" }
        require(versions == (1..versions.size).toList()) {
            "Migration versions must be contiguous from 1, got $versions"
        }
        val names = definitions.map { it.name }
        require(names.distinct().size == names.size) { "Migration names must be unique, got $names" }
    }

    /** The schema version this build expects after a successful migration. */
    public val targetVersion: Int get() = definitions.size

    public fun migrate(connection: Connection): Outcome<MigrationReport> =
        try {
            migrateOrThrow(connection)
        } catch (error: Exception) {
            Outcome.failure(
                PlatformError.Storage("Schema migration failed: ${error.message}", cause = error),
            )
        }

    // ------------------------------------------------------------------ internals

    private fun migrateOrThrow(connection: Connection): Outcome<MigrationReport> {
        ensureLedger(connection)

        val recorded = readLedger(connection)
        val legacyVersion = readLegacyVersion(connection)

        // A database written before the ledger existed records only `schema_meta`. Its rows were
        // produced by the migrations this build compiles, so adopting the compiled checksums is
        // correct; there is no other source of truth for what was applied.
        val ledger = if (recorded.isEmpty() && legacyVersion > 0) {
            when (val backfilled = backfillLedger(connection, legacyVersion)) {
                is Outcome.Failure -> return backfilled
                is Outcome.Success -> backfilled.value
            }
        } else {
            recorded
        }

        val installed = ledger.keys.maxOrNull() ?: 0

        if (installed > targetVersion) {
            return Outcome.failure(
                PlatformError.Storage(
                    "Database schema is version $installed but this build understands at most " +
                        "$targetVersion. Refusing to open: a downgrade must never migrate " +
                        "backwards or reinterpret rows written by a newer build. Install a build " +
                        "at version $installed or later.",
                ),
            )
        }

        if (ledger.keys.sorted() != (1..installed).toList()) {
            return Outcome.failure(
                PlatformError.Storage(
                    "Migration ledger has gaps: recorded versions ${ledger.keys.sorted()} are not " +
                        "contiguous from 1. The database cannot be repaired automatically.",
                ),
            )
        }

        for ((version, applied) in ledger) {
            val definition = definitions.firstOrNull { it.version == version }
                ?: return Outcome.failure(
                    PlatformError.Storage(
                        "Database records migration $version, which this build does not define.",
                    ),
                )
            if (applied.checksum != definition.checksum) {
                return Outcome.failure(
                    PlatformError.Storage(
                        "Migration $version (${definition.name}) does not match the version applied " +
                            "to this database: recorded ${applied.checksum}, compiled " +
                            "${definition.checksum}. A shipped migration must never be edited — " +
                            "append a new migration instead. This database was built by different " +
                            "SQL and cannot be verified.",
                    ),
                )
            }
        }

        val pending = definitions.filter { it.version > installed }
        for (definition in pending) {
            applyOne(connection, definition)
        }

        return Outcome.success(
            MigrationReport(
                fromVersion = installed,
                toVersion = targetVersion,
                appliedVersions = pending.map { it.version },
            ),
        )
    }

    /**
     * One migration, one transaction, recorded in that same transaction. A crash mid-upgrade
     * therefore leaves the database at the last fully applied version.
     */
    private fun applyOne(connection: Connection, definition: MigrationDefinition) {
        val restoreAutoCommit = connection.autoCommit
        connection.autoCommit = false
        try {
            connection.createStatement().use { statement ->
                definition.statements.forEach(statement::execute)
            }
            connection.prepareStatement(
                "INSERT INTO $LEDGER_TABLE (version, name, checksum, applied_at) VALUES (?,?,?,?)",
            ).use { statement ->
                statement.setInt(1, definition.version)
                statement.setString(2, definition.name)
                statement.setString(3, definition.checksum)
                statement.setLong(4, clock.nowEpochMillis())
                statement.executeUpdate()
            }
            writeLegacyVersion(connection, definition.version)
            connection.commit()
        } catch (error: Throwable) {
            connection.rollback()
            throw error
        } finally {
            connection.autoCommit = restoreAutoCommit
        }
    }

    private fun ensureLedger(connection: Connection) {
        connection.createStatement().use { statement ->
            statement.execute(
                """
                CREATE TABLE IF NOT EXISTS $LEDGER_TABLE (
                    version    INTEGER PRIMARY KEY,
                    name       TEXT    NOT NULL,
                    checksum   TEXT    NOT NULL,
                    applied_at INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            statement.execute("CREATE TABLE IF NOT EXISTS schema_meta (version INTEGER NOT NULL)")
        }
    }

    private fun readLedger(connection: Connection): Map<Int, AppliedMigration> =
        connection.createStatement().use { statement ->
            statement.executeQuery(
                "SELECT version, name, checksum, applied_at FROM $LEDGER_TABLE ORDER BY version",
            ).use { rows ->
                val applied = LinkedHashMap<Int, AppliedMigration>()
                while (rows.next()) {
                    applied[rows.getInt("version")] = AppliedMigration(
                        version = rows.getInt("version"),
                        name = rows.getString("name"),
                        checksum = rows.getString("checksum"),
                        appliedAtEpochMillis = rows.getLong("applied_at"),
                    )
                }
                applied
            }
        }

    private fun backfillLedger(
        connection: Connection,
        legacyVersion: Int,
    ): Outcome<Map<Int, AppliedMigration>> {
        if (legacyVersion > targetVersion) {
            return Outcome.failure(
                PlatformError.Storage(
                    "Database reports schema version $legacyVersion but this build understands at " +
                        "most $targetVersion. Refusing to open.",
                ),
            )
        }
        val now = clock.nowEpochMillis()
        val restoreAutoCommit = connection.autoCommit
        connection.autoCommit = false
        try {
            connection.prepareStatement(
                "INSERT INTO $LEDGER_TABLE (version, name, checksum, applied_at) VALUES (?,?,?,?)",
            ).use { statement ->
                for (version in 1..legacyVersion) {
                    val definition = definitions.first { it.version == version }
                    statement.setInt(1, definition.version)
                    statement.setString(2, definition.name)
                    statement.setString(3, definition.checksum)
                    statement.setLong(4, now)
                    statement.executeUpdate()
                }
            }
            connection.commit()
        } catch (error: Throwable) {
            connection.rollback()
            throw error
        } finally {
            connection.autoCommit = restoreAutoCommit
        }
        return Outcome.success(readLedger(connection))
    }

    private fun readLegacyVersion(connection: Connection): Int =
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT version FROM schema_meta LIMIT 1").use { rows ->
                if (rows.next()) rows.getInt(1) else 0
            }
        }

    private fun writeLegacyVersion(connection: Connection, version: Int) {
        connection.createStatement().use { statement ->
            statement.execute("DELETE FROM schema_meta")
            statement.execute("INSERT INTO schema_meta (version) VALUES ($version)")
        }
    }

    /** A row of the migration ledger. */
    public class AppliedMigration(
        public val version: Int,
        public val name: String,
        public val checksum: String,
        public val appliedAtEpochMillis: Long,
    )

    public companion object {
        public const val LEDGER_TABLE: String = "schema_migrations"

        /** The migrator this build ships. */
        public fun released(clock: Clock = Clock.system()): SqliteMigrator =
            SqliteMigrator(Migrations.RELEASED, clock)

        /** Reads the ledger of an already-migrated database, for diagnostics and tests. */
        public fun ledgerOf(connection: Connection): List<AppliedMigration> {
            val migrator = SqliteMigrator(Migrations.RELEASED)
            migrator.ensureLedger(connection)
            return migrator.readLedger(connection).values.toList()
        }
    }
}
