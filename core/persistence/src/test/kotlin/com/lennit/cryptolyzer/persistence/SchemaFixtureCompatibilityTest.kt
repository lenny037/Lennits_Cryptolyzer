package com.lennit.cryptolyzer.persistence

import com.lennit.cryptolyzer.contracts.MutableClock
import com.lennit.cryptolyzer.eventbus.EventStatus
import com.lennit.cryptolyzer.eventbus.RegisteredEventStore
import com.lennit.cryptolyzer.eventbus.testing.RegistryFixtures
import com.lennit.cryptolyzer.telemetry.RecordingTelemetry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.sql.DriverManager

/**
 * Phase 4's real gate: a database written by an earlier release must still open, still decode, and
 * still lose nothing.
 *
 * Everything else in this phase is a mechanism. This is the test that would actually catch the
 * change that bricks an installed application, because it does not ask the current code whether it
 * is self-consistent — it replays a frozen artefact from the version 1 release and demands that the
 * current build read it.
 *
 * The fixture lives in `src/test/resources/fixtures/schema-v1/events.sql` and is text, not a `.db`:
 * a binary fixture is both rejected by the repository guards and impossible to review.
 */
class SchemaFixtureCompatibilityTest {

    private val clock = MutableClock(1_800_000_000_000)

    private fun materialiseV1Fixture(dir: Path): String {
        val sql = requireNotNull(
            javaClass.getResourceAsStream("/fixtures/schema-v1/events.sql"),
        ) { "the version 1 schema fixture is missing from test resources" }
            .use { it.readBytes().decodeToString() }

        val file = dir.resolve("fixture-v1.db").toString()
        DriverManager.getConnection("jdbc:sqlite:$file").use { connection ->
            connection.createStatement().use { statement ->
                sql.split(";\n")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && !it.lines().all { line -> line.startsWith("--") } }
                    .forEach { statement.execute(it) }
            }
        }
        return file
    }

    private val fixtureEventIds =
        listOf("evt-0001", "evt-0002", "evt-0003", "evt-0004", "evt-0005")

    @Test
    fun `a version 1 database opens on the current build`(@TempDir dir: Path) {
        val file = materialiseV1Fixture(dir)
        SqliteEventStore.open(file).use { store ->
            assertEquals(5, store.stats().total)
            fixtureEventIds.forEach { id ->
                assertNotNull(store.find(id), "$id was lost when the database was opened")
            }
        }
    }

    @Test
    fun `every retained row decodes to the values it was written with`(@TempDir dir: Path) {
        val file = materialiseV1Fixture(dir)
        SqliteEventStore.open(file).use { store ->
            val first = requireNotNull(store.find("evt-0001"))
            assertEquals(RegistryFixtures.WIDGET_OBSERVED, first.envelope.type)
            assertEquals(1, first.envelope.schemaVersion)
            assertEquals(mapOf("source" to "rpc", "value_raw" to "42"), first.envelope.payload)
            assertEquals("trace-a", first.envelope.traceId)
            assertEquals(EventStatus.Pending, first.status)

            val retried = requireNotNull(store.find("evt-0002"))
            assertEquals(2, retried.attempt)
            assertEquals(1_700_000_060_000, retried.nextAttemptAtEpochMillis)
            assertEquals("upstream flaked", retried.lastError)

            val processed = requireNotNull(store.find("evt-0003"))
            assertEquals(EventStatus.Processed, processed.status)
            // Money stayed a decimal string across the release boundary (ADR-0003).
            assertEquals("12.50", processed.envelope.payload["amount"])

            // Escaped delimiters survive: the codec's wire format is part of the contract.
            val dead = requireNotNull(store.find("evt-0004"))
            assertEquals(
                mapOf("sensor" to "s=1", "reading" to "7", "note" to "a;b"),
                dead.envelope.payload,
            )
            assertEquals(EventStatus.DeadLettered, dead.status)

            val inFlight = requireNotNull(store.find("evt-0005"))
            assertEquals(EventStatus.InFlight, inFlight.status)
            assertEquals(1_700_000_034_000, inFlight.leaseExpiresAtEpochMillis)
        }
    }

    @Test
    fun `opening a pre-ledger database backfills the ledger with the released checksum`(
        @TempDir dir: Path,
    ) {
        val file = materialiseV1Fixture(dir)
        SqliteEventStore.open(file).use { }

        DriverManager.getConnection("jdbc:sqlite:$file").use { connection ->
            val ledger = SqliteMigrator.ledgerOf(connection)
            assertEquals(1, ledger.size)
            assertEquals(1, ledger.single().version)
            assertEquals("durable_event_log", ledger.single().name)
            assertEquals(Migrations.RELEASED.first().checksum, ledger.single().checksum)
        }
    }

    @Test
    fun `an event written before the current schema version is delivered at the current one`(
        @TempDir dir: Path,
    ) {
        val file = materialiseV1Fixture(dir)
        val telemetry = RecordingTelemetry()
        SqliteEventStore.open(file).use { raw ->
            val store = RegisteredEventStore(raw, RegistryFixtures.registry, telemetry)

            val claimed = requireNotNull(
                store.claimPending(10, 1_800_000_000_000, 30_000).getOrNull(),
            )
            val widget = claimed.first { it.eventId == "evt-0001" }

            // Written at version 1 by a release that predates both later revisions.
            assertEquals(3, widget.envelope.schemaVersion)
            assertEquals(
                mapOf("source" to "rpc", "value" to "42", "unit" to "wei"),
                widget.envelope.payload,
            )

            // The lease-expired in-flight row is recovered by the same pass, also upcast.
            val recovered = claimed.first { it.eventId == "evt-0005" }
            assertEquals(1, recovered.envelope.schemaVersion)
            assertEquals("s2", recovered.envelope.payload["sensor"])

            // And the stored bytes are unchanged, so an application downgrade still reads them.
            assertEquals(
                mapOf("source" to "rpc", "value_raw" to "42"),
                requireNotNull(raw.find("evt-0001")).envelope.payload,
            )
        }
    }

    @Test
    fun `version 1 rows survive a forward migration to a later schema version`(@TempDir dir: Path) {
        val file = materialiseV1Fixture(dir)
        // Opening once adopts the ledger, as a shipped build would.
        SqliteEventStore.open(file).use { }

        // A hypothetical version 2, applied by a test-only migrator: the released history has one
        // entry today, and this test must keep working when it gains a second without being edited.
        val futureMigration = MigrationDefinition(
            version = Migrations.RELEASED.size + 1,
            name = "test_only_future_column",
            statements = listOf("ALTER TABLE events ADD COLUMN reserved_for_test TEXT"),
        )
        DriverManager.getConnection("jdbc:sqlite:$file").use { connection ->
            val migrator = SqliteMigrator(Migrations.RELEASED + futureMigration, clock)
            val report = requireNotNull(migrator.migrate(connection).getOrNull())
            assertEquals(listOf(futureMigration.version), report.appliedVersions)

            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT COUNT(*) FROM events").use { rows ->
                    rows.next()
                    assertEquals(5, rows.getInt(1))
                }
                // The upgrade must not have rewritten stored payloads.
                statement.executeQuery(
                    "SELECT payload FROM events WHERE event_id = 'evt-0001'",
                ).use { rows ->
                    rows.next()
                    assertEquals("source=rpc;value_raw=42", rows.getString(1))
                }
            }
        }
    }

    @Test
    fun `the fixture is text, so its schema stays reviewable in a diff`() {
        val bytes = requireNotNull(javaClass.getResourceAsStream("/fixtures/schema-v1/events.sql"))
            .use { it.readBytes() }
        assertTrue(bytes.isNotEmpty())
        assertTrue(bytes.none { it == 0.toByte() }, "the schema fixture must not be binary")
        val text = bytes.decodeToString()
        // The exact v1 DDL is the point of the fixture; a rewrite that loosens it defeats the test.
        assertTrue(text.contains("idempotency_key  TEXT    NOT NULL UNIQUE"))
        assertTrue(text.contains("INSERT INTO schema_meta (version) VALUES (1)"))
    }
}
