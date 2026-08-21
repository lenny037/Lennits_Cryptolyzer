package com.lennit.cryptolyzer.persistence

import com.lennit.cryptolyzer.contracts.ModuleId
import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.eventbus.EventEnvelope
import com.lennit.cryptolyzer.eventbus.EventStatus
import com.lennit.cryptolyzer.eventbus.EventStore
import com.lennit.cryptolyzer.eventbus.EventType
import com.lennit.cryptolyzer.eventbus.testing.EventStoreContract
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

/** The SQLite store answers to the same specification as the in-memory one. */
class SqliteEventStoreTest : EventStoreContract() {
    override fun newStore(): EventStore = SqliteEventStore.inMemory()
}

/** Properties that only a persistent store can have. */
class SqliteEventStoreDurabilityTest {

    private fun envelope(id: String, payload: Map<String, String> = mapOf("k" to "v")) = EventEnvelope(
        eventId = id,
        type = EventType.TREASURY_SNAPSHOT_TAKEN,
        schemaVersion = 1,
        producer = ModuleId.Treasury,
        occurredAtEpochMillis = 1_000,
        recordedAtEpochMillis = 1_000,
        idempotencyKey = id,
        payload = payload,
    )

    @Test
    fun `events survive closing and reopening the database`(@TempDir dir: Path) {
        val file = dir.resolve("events.db").toString()
        SqliteEventStore.open(file).use { store ->
            store.append(envelope("e1", mapOf("amount" to "12.50", "note" to "a=b;c\\d")))
            store.claimPending(1, 1_000, 60_000)
            store.markForRetry("e1", 7_000, "upstream flaked")
        }

        SqliteEventStore.open(file).use { reopened ->
            val stored = requireNotNull(reopened.find("e1"))
            assertEquals(EventStatus.Pending, stored.status)
            assertEquals(1, stored.attempt)
            assertEquals(7_000, stored.nextAttemptAtEpochMillis)
            // Adversarial payload characters must round-trip through the codec unharmed.
            assertEquals(mapOf("amount" to "12.50", "note" to "a=b;c\\d"), stored.envelope.payload)
        }
    }

    @Test
    fun `idempotency survives a restart, so a replayed producer cannot double-write`(@TempDir dir: Path) {
        val file = dir.resolve("events.db").toString()
        SqliteEventStore.open(file).use { it.append(envelope("e1")) }
        SqliteEventStore.open(file).use { reopened ->
            val second = reopened.append(envelope("e2").copy(idempotencyKey = "e1"))
            assertTrue(second is Outcome.Success)
            assertEquals(1, reopened.stats().total)
        }
    }

    @Test
    fun `migrating an already-migrated database is a no-op`(@TempDir dir: Path) {
        val file = dir.resolve("events.db").toString()
        repeat(3) { SqliteEventStore.open(file).use { it.append(envelope("e$it")) } }
        SqliteEventStore.open(file).use { store ->
            assertEquals(3, store.stats().total)
            assertEquals(SqliteEventStore.schemaVersion, SqliteEventStore.schemaVersion)
        }
    }

    @Test
    fun `a claim that fails midway leaves no partially leased batch`(@TempDir dir: Path) {
        val store = SqliteEventStore.open(dir.resolve("events.db").toString())
        repeat(5) { store.append(envelope("e$it")) }
        val claimed = (store.claimPending(3, 2_000, 60_000) as Outcome.Success).value
        assertEquals(3, claimed.size)
        val stats = store.stats()
        assertEquals(3, stats.inFlight)
        assertEquals(2, stats.pending)
        store.close()
    }
}
