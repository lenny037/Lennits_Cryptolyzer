package com.lennit.cryptolyzer.eventbus.testing

import com.lennit.cryptolyzer.contracts.ModuleId
import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.eventbus.AppendResult
import com.lennit.cryptolyzer.eventbus.EventEnvelope
import com.lennit.cryptolyzer.eventbus.EventStatus
import com.lennit.cryptolyzer.eventbus.EventStore
import com.lennit.cryptolyzer.eventbus.EventType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Executable specification of the [EventStore] port.
 *
 * Every implementation must extend this class. The suite encodes the Phase 3 gate directly: the
 * durability, idempotency and lease-recovery properties are asserted, not documented.
 */
public abstract class EventStoreContract {

    protected abstract fun newStore(): EventStore

    private fun envelope(
        id: String,
        idempotencyKey: String = id,
        occurredAt: Long = 1_000,
        type: EventType = EventType.SIGNAL_PRODUCED,
        payload: Map<String, String> = mapOf("k" to "v"),
    ): EventEnvelope = EventEnvelope(
        eventId = id,
        type = type,
        schemaVersion = 1,
        producer = ModuleId.Ingestion,
        occurredAtEpochMillis = occurredAt,
        recordedAtEpochMillis = occurredAt,
        idempotencyKey = idempotencyKey,
        payload = payload,
    )

    @Test
    public fun `appends and reads back an event with payload intact`() {
        val store = newStore()
        val appended = store.append(envelope("e1", payload = mapOf("a" to "1", "b" to "two")))
        assertTrue(appended is Outcome.Success && appended.value is AppendResult.Appended)

        val stored = requireNotNull(store.find("e1"))
        assertEquals(EventStatus.Pending, stored.status)
        assertEquals(0, stored.attempt)
        assertEquals(mapOf("a" to "1", "b" to "two"), stored.envelope.payload)
    }

    @Test
    public fun `suppresses a duplicate idempotency key instead of double-appending`() {
        val store = newStore()
        store.append(envelope("e1", idempotencyKey = "same"))
        val second = store.append(envelope("e2", idempotencyKey = "same"))

        assertTrue(second is Outcome.Success)
        val result = (second as Outcome.Success).value
        assertTrue(result is AppendResult.Duplicate, "expected Duplicate, got $result")
        assertEquals("e1", (result as AppendResult.Duplicate).existingEventId)
        assertEquals(1, store.stats().pending)
        assertNull(store.find("e2"))
    }

    @Test
    public fun `claims only events that are due`() {
        val store = newStore()
        store.append(envelope("due", occurredAt = 100))
        store.append(envelope("later", occurredAt = 100))
        store.markForRetry("later", nextAttemptAtEpochMillis = 5_000, error = "transient")

        val claimed = (store.claimPending(10, nowEpochMillis = 1_000, leaseMillis = 100) as Outcome.Success).value
        assertEquals(listOf("due"), claimed.map { it.eventId })
        assertEquals(EventStatus.InFlight, requireNotNull(store.find("due")).status)
    }

    @Test
    public fun `does not hand the same event to two concurrent claims`() {
        val store = newStore()
        store.append(envelope("e1"))
        val first = (store.claimPending(10, 2_000, leaseMillis = 60_000) as Outcome.Success).value
        val second = (store.claimPending(10, 2_000, leaseMillis = 60_000) as Outcome.Success).value

        assertEquals(1, first.size)
        assertTrue(second.isEmpty(), "a leased event must not be claimable again before lease expiry")
    }

    @Test
    public fun `recovers a leased event once its lease expires`() {
        val store = newStore()
        store.append(envelope("e1"))
        store.claimPending(10, nowEpochMillis = 1_000, leaseMillis = 500)

        // Simulates process death between lease and acknowledgement.
        val reclaimed = (store.claimPending(10, nowEpochMillis = 1_600, leaseMillis = 500) as Outcome.Success).value
        assertEquals(listOf("e1"), reclaimed.map { it.eventId })
    }

    @Test
    public fun `retry increments the attempt counter and records the error`() {
        val store = newStore()
        store.append(envelope("e1"))
        store.claimPending(10, 1_000, 500)
        store.markForRetry("e1", nextAttemptAtEpochMillis = 4_000, error = "upstream 503")

        val stored = requireNotNull(store.find("e1"))
        assertEquals(EventStatus.Pending, stored.status)
        assertEquals(1, stored.attempt)
        assertEquals(4_000, stored.nextAttemptAtEpochMillis)
        assertTrue(stored.lastError?.contains("503") == true)
        assertNull(stored.leaseExpiresAtEpochMillis)
    }

    @Test
    public fun `releasing a lease does not consume an attempt`() {
        val store = newStore()
        store.append(envelope("e1"))
        store.claimPending(10, 1_000, 500)
        store.releaseLease("e1", nextAttemptAtEpochMillis = 2_000)

        val stored = requireNotNull(store.find("e1"))
        assertEquals(EventStatus.Pending, stored.status)
        assertEquals(0, stored.attempt)
    }

    @Test
    public fun `dead letters are retained and queryable`() {
        val store = newStore()
        store.append(envelope("e1"))
        store.markDeadLettered("e1", reason = "non-retryable", nowEpochMillis = 2_000)

        assertEquals(1, store.stats().deadLettered)
        assertEquals(listOf("e1"), store.deadLetters(10).map { it.eventId })
        assertTrue(store.find("e1") != null, "a dead letter must never be deleted")
    }

    @Test
    public fun `terminal states are excluded from claiming`() {
        val store = newStore()
        store.append(envelope("done"))
        store.append(envelope("dead"))
        store.markProcessed("done", 2_000)
        store.markDeadLettered("dead", "gave up", 2_000)

        val claimed = (store.claimPending(10, 9_999, 500) as Outcome.Success).value
        assertTrue(claimed.isEmpty())
    }

    @Test
    public fun `stats account for every event exactly once`() {
        val store = newStore()
        repeat(4) { index -> store.append(envelope("e$index")) }
        store.markProcessed("e0", 2_000)
        store.markDeadLettered("e1", "x", 2_000)
        store.claimPending(1, 2_000, 60_000)

        val stats = store.stats()
        assertEquals(4, stats.total, "no event may vanish from the accounting: $stats")
        assertEquals(1, stats.processed)
        assertEquals(1, stats.deadLettered)
        assertEquals(1, stats.inFlight)
        assertEquals(1, stats.pending)
    }

    @Test
    public fun `reports a failure for an unknown event id rather than silently succeeding`() {
        val store = newStore()
        assertTrue(store.markProcessed("nope", 1_000) is Outcome.Failure)
    }

    @Test
    public fun `claims in occurrence order`() {
        val store = newStore()
        store.append(envelope("third", occurredAt = 300))
        store.append(envelope("first", occurredAt = 100))
        store.append(envelope("second", occurredAt = 200))

        val claimed = (store.claimPending(3, 1_000, 500) as Outcome.Success).value
        assertEquals(listOf("first", "second", "third"), claimed.map { it.eventId })
    }
}
