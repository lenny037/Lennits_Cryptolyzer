package com.lennit.cryptolyzer.eventbus

import com.lennit.cryptolyzer.contracts.ModuleId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class EventEnvelopeTest {

    private fun envelope(occurredAt: Long = 10, recordedAt: Long = 10) = EventEnvelope(
        eventId = "e1",
        type = EventType.SIGNAL_PRODUCED,
        schemaVersion = 1,
        producer = ModuleId.Ingestion,
        occurredAtEpochMillis = occurredAt,
        recordedAtEpochMillis = recordedAt,
        idempotencyKey = "k",
        payload = emptyMap(),
    )

    @Test
    fun `rejects a record time that precedes the occurrence time`() {
        assertThrows(IllegalArgumentException::class.java) { envelope(occurredAt = 100, recordedAt = 99) }
    }

    @Test
    fun `rejects a blank idempotency key`() {
        assertThrows(IllegalArgumentException::class.java) {
            envelope().copy(idempotencyKey = "  ")
        }
    }

    @Test
    fun `rejects an unversioned schema`() {
        assertThrows(IllegalArgumentException::class.java) { envelope().copy(schemaVersion = 0) }
    }

    @Test
    fun `event types must be lowercase dotted segments`() {
        assertThrows(IllegalArgumentException::class.java) { EventType("Chain.Block") }
        assertThrows(IllegalArgumentException::class.java) { EventType("chain") }
        assertEquals("chain.block_observed", EventType("chain.block_observed").name)
    }
}
