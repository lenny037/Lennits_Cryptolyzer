package com.lennit.cryptolyzer.persistence

import com.lennit.cryptolyzer.contracts.MutableClock
import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.contracts.PlatformError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager

/**
 * The migration engine, tested through its failure modes.
 *
 * A migrator that only works on the happy path is worth very little: on a phone, the interesting
 * cases are the database that came from a newer build, the migration someone edited after release,
 * and the upgrade that died halfway through.
 */
class SqliteMigratorTest {

    private val clock = MutableClock(1_700_000_000_000)

    private fun connection(): Connection = DriverManager.getConnection("jdbc:sqlite::memory:")

    private fun definition(version: Int, name: String, sql: String) =
        MigrationDefinition(version, name, listOf(sql))

    private val v1 = definition(1, "create_widgets", "CREATE TABLE IF NOT EXISTS widgets (id TEXT PRIMARY KEY)")
    private val v2 = definition(2, "add_widget_label", "ALTER TABLE widgets ADD COLUMN label TEXT")
    private val v3 = definition(3, "index_widget_label", "CREATE INDEX IF NOT EXISTS idx_label ON widgets (label)")

    private fun ledgerVersions(connection: Connection): List<Int> =
        SqliteMigrator.ledgerOf(connection).map { it.version }

    private fun schemaMetaVersion(connection: Connection): Int =
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT version FROM schema_meta LIMIT 1").use { rows ->
                if (rows.next()) rows.getInt(1) else 0
            }
        }

    // ------------------------------------------------------------------ declaration

    @Test
    fun `a migrator refuses a non-contiguous history`() {
        assertThrows(IllegalArgumentException::class.java) { SqliteMigrator(listOf(v1, v3)) }
    }

    @Test
    fun `a migrator refuses duplicate migration names`() {
        assertThrows(IllegalArgumentException::class.java) {
            SqliteMigrator(listOf(v1, definition(2, "create_widgets", "SELECT 1")))
        }
    }

    @Test
    fun `a checksum ignores reformatting but not a change to the SQL`() {
        val original = definition(1, "create_widgets", "CREATE TABLE widgets (id TEXT PRIMARY KEY)")
        val reindented = definition(
            1,
            "create_widgets",
            """
            CREATE TABLE widgets    (id TEXT
               PRIMARY KEY)
            """.trimIndent(),
        )
        val changed = definition(1, "create_widgets", "CREATE TABLE widgets (id INTEGER PRIMARY KEY)")
        assertEquals(original.checksum, reindented.checksum)
        assertTrue(original.checksum != changed.checksum)
    }

    // ------------------------------------------------------------------ applying

    @Test
    fun `a fresh database is migrated to the target version and the ledger records it`() {
        connection().use { db ->
            val report = requireNotNull(SqliteMigrator(listOf(v1, v2), clock).migrate(db).getOrNull())
            assertEquals(0, report.fromVersion)
            assertEquals(2, report.toVersion)
            assertEquals(listOf(1, 2), report.appliedVersions)
            assertEquals(listOf(1, 2), ledgerVersions(db))
            assertEquals(2, schemaMetaVersion(db))

            val applied = SqliteMigrator.ledgerOf(db)
            assertEquals(v1.checksum, applied.first { it.version == 1 }.checksum)
            assertEquals(1_700_000_000_000, applied.first { it.version == 1 }.appliedAtEpochMillis)
        }
    }

    @Test
    fun `re-running the same migrator applies nothing`() {
        connection().use { db ->
            val migrator = SqliteMigrator(listOf(v1, v2), clock)
            migrator.migrate(db)
            val second = requireNotNull(migrator.migrate(db).getOrNull())
            assertTrue(!second.didUpgrade)
            assertEquals(2, second.fromVersion)
            assertEquals(listOf(1, 2), ledgerVersions(db))
        }
    }

    @Test
    fun `a later build upgrades an existing database without touching applied versions`() {
        connection().use { db ->
            SqliteMigrator(listOf(v1), clock).migrate(db)
            val firstApplied = SqliteMigrator.ledgerOf(db).single().appliedAtEpochMillis

            clock.setTo(1_800_000_000_000)
            val report = requireNotNull(SqliteMigrator(listOf(v1, v2, v3), clock).migrate(db).getOrNull())
            assertEquals(1, report.fromVersion)
            assertEquals(listOf(2, 3), report.appliedVersions)
            assertEquals(listOf(1, 2, 3), ledgerVersions(db))
            assertEquals(
                firstApplied,
                SqliteMigrator.ledgerOf(db).first { it.version == 1 }.appliedAtEpochMillis,
            )
        }
    }

    // ------------------------------------------------------------------ refusals

    @Test
    fun `an edited shipped migration is refused instead of silently accepted`() {
        connection().use { db ->
            SqliteMigrator(listOf(v1, v2), clock).migrate(db)

            // Somebody "fixed" migration 1 after release. Two populations of devices now have
            // different schemas at the same version number, and only the checksum can tell.
            val edited = definition(1, "create_widgets", "CREATE TABLE IF NOT EXISTS widgets (id INTEGER PRIMARY KEY)")
            val error = SqliteMigrator(listOf(edited, v2), clock).migrate(db).errorOrNull()

            assertTrue(error is PlatformError.Storage)
            assertTrue(error!!.message.contains("must never be edited"))
            assertTrue(error.message.contains(edited.checksum))
        }
    }

    @Test
    fun `a database from a newer build is refused rather than downgraded`() {
        connection().use { db ->
            SqliteMigrator(listOf(v1, v2, v3), clock).migrate(db)

            val error = SqliteMigrator(listOf(v1, v2), clock).migrate(db).errorOrNull()
            assertTrue(error is PlatformError.Storage)
            assertTrue(error!!.message.contains("Refusing to open"))
            // Nothing was changed on the way out.
            assertEquals(listOf(1, 2, 3), ledgerVersions(db))
            assertEquals(3, schemaMetaVersion(db))
        }
    }

    @Test
    fun `a ledger with a gap is refused`() {
        connection().use { db ->
            SqliteMigrator(listOf(v1, v2, v3), clock).migrate(db)
            db.createStatement().use { it.execute("DELETE FROM schema_migrations WHERE version = 2") }

            val error = SqliteMigrator(listOf(v1, v2, v3), clock).migrate(db).errorOrNull()
            assertTrue(error is PlatformError.Storage)
            assertTrue(error!!.message.contains("gaps"))
        }
    }

    @Test
    fun `a failing migration leaves the database at its previous version`() {
        connection().use { db ->
            SqliteMigrator(listOf(v1), clock).migrate(db)

            val broken = definition(2, "broken_migration", "ALTER TABLE nonexistent_table ADD COLUMN x TEXT")
            val result = SqliteMigrator(listOf(v1, broken), clock).migrate(db)

            assertTrue(result is Outcome.Failure)
            assertEquals(listOf(1), ledgerVersions(db))
            assertEquals(1, schemaMetaVersion(db))
            // And the successful part of the run is still usable.
            db.createStatement().use { it.execute("INSERT INTO widgets (id) VALUES ('w1')") }
        }
    }

    @Test
    fun `a partial multi-version upgrade keeps the versions it completed`() {
        connection().use { db ->
            val broken = definition(3, "broken_third", "ALTER TABLE nonexistent_table ADD COLUMN x TEXT")
            val result = SqliteMigrator(listOf(v1, v2, broken), clock).migrate(db)

            assertTrue(result is Outcome.Failure)
            assertEquals(listOf(1, 2), ledgerVersions(db))
            assertEquals(2, schemaMetaVersion(db))

            // A later, fixed build resumes from 2 rather than starting over.
            val fixed = requireNotNull(SqliteMigrator(listOf(v1, v2, v3), clock).migrate(db).getOrNull())
            assertEquals(2, fixed.fromVersion)
            assertEquals(listOf(3), fixed.appliedVersions)
        }
    }

    // ------------------------------------------------------------------ legacy databases

    @Test
    fun `a pre-ledger database is adopted by backfilling the checksums it must have used`() {
        connection().use { db ->
            // Exactly what the pre-ledger build left behind: schema_meta and nothing else.
            db.createStatement().use { statement ->
                statement.execute("CREATE TABLE widgets (id TEXT PRIMARY KEY)")
                statement.execute("CREATE TABLE schema_meta (version INTEGER NOT NULL)")
                statement.execute("INSERT INTO schema_meta (version) VALUES (1)")
            }

            val report = requireNotNull(SqliteMigrator(listOf(v1, v2), clock).migrate(db).getOrNull())
            assertEquals(1, report.fromVersion)
            assertEquals(listOf(2), report.appliedVersions)
            assertEquals(listOf(1, 2), ledgerVersions(db))
            assertEquals(v1.checksum, SqliteMigrator.ledgerOf(db).first { it.version == 1 }.checksum)
        }
    }

    @Test
    fun `a pre-ledger database from a newer build is refused`() {
        connection().use { db ->
            db.createStatement().use { statement ->
                statement.execute("CREATE TABLE schema_meta (version INTEGER NOT NULL)")
                statement.execute("INSERT INTO schema_meta (version) VALUES (7)")
            }
            val error = SqliteMigrator(listOf(v1, v2), clock).migrate(db).errorOrNull()
            assertTrue(error is PlatformError.Storage)
            assertTrue(error!!.message.contains("Refusing to open"))
        }
    }

    // ------------------------------------------------------------------ the released history

    @Test
    fun `the released migrator opens a real store and records version 1`() {
        SqliteEventStore.inMemory().use { store ->
            assertEquals(1, SqliteEventStore.schemaVersion)
            assertEquals(Migrations.RELEASED.size, SqliteMigrator.released().targetVersion)
            assertEquals(0, store.stats().total)
        }
    }

    @Test
    fun `openChecked reports a schema failure as a value instead of throwing`(@TempDir dir: Path) {
        val file = dir.resolve("newer.db").toString()
        DriverManager.getConnection("jdbc:sqlite:$file").use { db ->
            db.createStatement().use { statement ->
                statement.execute("CREATE TABLE schema_meta (version INTEGER NOT NULL)")
                statement.execute("INSERT INTO schema_meta (version) VALUES (99)")
            }
        }

        val result = SqliteEventStore.openChecked(file)
        assertTrue(result is Outcome.Failure)
        assertTrue((result as Outcome.Failure).error is PlatformError.Storage)

        // And the constructor path fails loudly rather than handing back a half-open store.
        assertThrows(IllegalStateException::class.java) { SqliteEventStore.open(file) }
    }

    @Test
    fun `openChecked returns a usable store for a healthy database`(@TempDir dir: Path) {
        val file = dir.resolve("ok.db").toString()
        val store = requireNotNull(SqliteEventStore.openChecked(file).getOrNull())
        store.use { assertEquals(0, it.stats().total) }
    }

    @Test
    fun `every released migration name and version is unique and checksummed`() {
        val released = Migrations.RELEASED
        assertEquals(released.map { it.version }, (1..released.size).toList())
        assertEquals(released.size, released.map { it.name }.distinct().size)
        assertEquals(released.size, released.map { it.checksum }.distinct().size)
        released.forEach { assertTrue(it.checksum.startsWith("sha256:")) }
    }
}
