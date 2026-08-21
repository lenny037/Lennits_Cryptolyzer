package com.lennit.cryptolyzer.eventbus

import com.lennit.cryptolyzer.contracts.IdGenerator
import com.lennit.cryptolyzer.contracts.ModuleId
import com.lennit.cryptolyzer.contracts.MutableClock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EventFactoryTest {

    private val clock = MutableClock(5_000)
    private var counter = 0
    private val factory = EventFactory(clock, IdGenerator { "id-${counter++}" }, ModuleId.BlockchainDataPlane)

    @Test
    fun `namespaces the idempotency key by type and schema version`() {
        val event = factory.create(EventType.CHAIN_BLOCK_OBSERVED, mapOf("n" to "1"), idempotencyKey = "8453:19")
        assertEquals("chain.block_observed:v1:8453:19", event.idempotencyKey)
    }

    @Test
    fun `the same natural key under different types does not collide`() {
        val a = factory.create(EventType.CHAIN_BLOCK_OBSERVED, emptyMap(), idempotencyKey = "42")
        val b = factory.create(EventType.SIGNAL_PRODUCED, emptyMap(), idempotencyKey = "42")
        assertNotEquals(a.idempotencyKey, b.idempotencyKey)
    }

    @Test
    fun `record time never precedes an occurrence time observed while offline`() {
        val event = factory.create(
            EventType.CHAIN_BLOCK_OBSERVED,
            emptyMap(),
            idempotencyKey = "k",
            occurredAtEpochMillis = 9_999,
        )
        assertTrue(event.recordedAtEpochMillis >= event.occurredAtEpochMillis)
    }
}
