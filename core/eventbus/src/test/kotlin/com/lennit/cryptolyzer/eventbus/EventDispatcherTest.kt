package com.lennit.cryptolyzer.eventbus

import com.lennit.cryptolyzer.contracts.MutableClock
import com.lennit.cryptolyzer.contracts.ModuleId
import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.contracts.PlatformError
import com.lennit.cryptolyzer.telemetry.RecordingTelemetry
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Verifies the dispatcher's disposition table exhaustively. The final test is the important one:
 * it asserts the Phase 3 gate as a property, not as an example.
 */
class EventDispatcherTest {

    private val clock = MutableClock(1_000)
    private val telemetry = RecordingTelemetry()

    private fun envelope(id: String, type: EventType = EventType.SIGNAL_PRODUCED) = EventEnvelope(
        eventId = id,
        type = type,
        schemaVersion = 1,
        producer = ModuleId.Ingestion,
        occurredAtEpochMillis = 1_000,
        recordedAtEpochMillis = 1_000,
        idempotencyKey = id,
        payload = mapOf("id" to id),
    )

    @Test
    fun `successful handling marks the event processed`() = runTest {
        val store = InMemoryEventStore()
        val dispatcher = EventDispatcher(store, clock, telemetry = telemetry)
        dispatcher.register(EventType.SIGNAL_PRODUCED) { Outcome.success(Unit) }
        dispatcher.publish(envelope("e1"))

        val report = (dispatcher.drainOnce() as Outcome.Success).value
        assertEquals(1, report.processed)
        assertEquals(EventStatus.Processed, requireNotNull(store.find("e1")).status)
    }

    @Test
    fun `retryable failure is rescheduled with backoff and stays pending`() = runTest {
        val store = InMemoryEventStore()
        val dispatcher = EventDispatcher(store, clock, telemetry = telemetry)
        dispatcher.register(EventType.SIGNAL_PRODUCED) {
            Outcome.failure(PlatformError.Transport("socket reset"))
        }
        dispatcher.publish(envelope("e1"))

        val report = (dispatcher.drainOnce() as Outcome.Success).value
        assertEquals(1, report.retried)
        val stored = requireNotNull(store.find("e1"))
        assertEquals(EventStatus.Pending, stored.status)
        assertEquals(1, stored.attempt)
        assertTrue(stored.nextAttemptAtEpochMillis > clock.nowEpochMillis(), "backoff was not applied")
    }

    @Test
    fun `non-retryable failure is dead lettered immediately`() = runTest {
        val store = InMemoryEventStore()
        val dispatcher = EventDispatcher(store, clock, telemetry = telemetry)
        dispatcher.register(EventType.SIGNAL_PRODUCED) {
            Outcome.failure(PlatformError.Validation("payload missing 'amount'"))
        }
        dispatcher.publish(envelope("e1"))

        val report = (dispatcher.drainOnce() as Outcome.Success).value
        assertEquals(1, report.deadLettered)
        assertEquals(EventStatus.DeadLettered, requireNotNull(store.find("e1")).status)
    }

    @Test
    fun `a throwing handler is treated as a defect and dead lettered, not swallowed`() = runTest {
        val store = InMemoryEventStore()
        val dispatcher = EventDispatcher(store, clock, telemetry = telemetry)
        dispatcher.register(EventType.SIGNAL_PRODUCED) { error("boom") }
        dispatcher.publish(envelope("e1"))

        val report = (dispatcher.drainOnce() as Outcome.Success).value
        assertEquals(1, report.deadLettered)
        assertTrue(telemetry.records.any { it.name == "event.dead_lettered" })
    }

    @Test
    fun `an event with no registered handler stays pending instead of being discarded`() = runTest {
        val store = InMemoryEventStore()
        val dispatcher = EventDispatcher(store, clock, telemetry = telemetry)
        dispatcher.publish(envelope("e1", EventType.CHAIN_BLOCK_OBSERVED))

        val report = (dispatcher.drainOnce() as Outcome.Success).value
        assertEquals(1, report.unroutable)
        val stored = requireNotNull(store.find("e1"))
        assertEquals(EventStatus.Pending, stored.status)
        assertEquals(0, stored.attempt, "an unroutable event must not burn its retry budget")
    }

    @Test
    fun `retries are bounded and end in the dead letter queue`() = runTest {
        val store = InMemoryEventStore()
        val policy = RetryPolicy(maxAttempts = 3, baseDelayMillis = 10, maxDelayMillis = 100)
        val dispatcher = EventDispatcher(store, clock, policy, telemetry)
        dispatcher.register(EventType.SIGNAL_PRODUCED) {
            Outcome.failure(PlatformError.Transport("still down"))
        }
        dispatcher.publish(envelope("e1"))

        repeat(6) {
            dispatcher.drainOnce()
            clock.advanceBy(1_000)
        }

        val stored = requireNotNull(store.find("e1"))
        assertEquals(EventStatus.DeadLettered, stored.status)
        assertTrue(stored.lastError?.contains("retry budget exhausted") == true, stored.lastError ?: "")
    }

    @Test
    fun `duplicate publication is suppressed so handlers see the fact once`() = runTest {
        val store = InMemoryEventStore()
        val dispatcher = EventDispatcher(store, clock, telemetry = telemetry)
        var handled = 0
        dispatcher.register(EventType.SIGNAL_PRODUCED) {
            handled++
            Outcome.success(Unit)
        }
        dispatcher.publish(envelope("e1"))
        dispatcher.publish(envelope("e2").copy(idempotencyKey = "e1"))
        dispatcher.drainOnce()

        assertEquals(1, handled)
    }

    @Test
    fun `no event is ever unaccounted for regardless of handler behaviour`() = runTest {
        val store = InMemoryEventStore()
        val policy = RetryPolicy(maxAttempts = 2, baseDelayMillis = 10, maxDelayMillis = 50)
        val dispatcher = EventDispatcher(store, clock, policy, telemetry)

        // A deliberately hostile handler: succeeds, fails transiently, fails permanently, throws.
        var call = 0
        dispatcher.register(EventType.SIGNAL_PRODUCED) {
            when (call++ % 4) {
                0 -> Outcome.success(Unit)
                1 -> Outcome.failure(PlatformError.Transport("flap"))
                2 -> Outcome.failure(PlatformError.Validation("bad"))
                else -> error("defect")
            }
        }
        val total = 40
        repeat(total) { index -> dispatcher.publish(envelope("e$index")) }

        repeat(12) {
            dispatcher.drainOnce(batchSize = 16)
            clock.advanceBy(5_000)
        }

        val stats = store.stats()
        assertEquals(total, stats.total, "events went missing: $stats")
        assertEquals(0, stats.inFlight, "an event was left leased forever: $stats")
        assertEquals(
            total,
            stats.processed + stats.deadLettered + stats.pending,
            "every event must be in exactly one accounted state: $stats",
        )
    }
}
