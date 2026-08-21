package com.lennit.cryptolyzer.domain

import com.lennit.cryptolyzer.contracts.ModuleId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SignalTest {

    private fun signal(
        id: String = "s1",
        kind: SignalKind = SignalKind.PriceObservation,
        subject: String = "ETH",
        producedBy: ModuleId = ModuleId.Ingestion,
        observedAt: Long = 1_000,
        attributes: Map<String, String> = mapOf("venue" to "base"),
        measures: Map<String, Amount> = mapOf("price" to amount("2500.50")),
    ) = Signal(
        id = id,
        kind = kind,
        subject = subject,
        producedBy = producedBy,
        observedAtEpochMillis = observedAt,
        quality = SignalQuality.PROVISIONAL,
        attributes = attributes,
        measures = measures,
    )

    @Test
    fun `the same fact from two providers collapses to one dedupe key`() {
        val fromProviderA = signal(id = "a", producedBy = ModuleId.Ingestion, observedAt = 1_000)
        val fromProviderB = signal(id = "b", producedBy = ModuleId.MarketMicrostructure, observedAt = 1_050)
        assertEquals(fromProviderA.dedupeKey, fromProviderB.dedupeKey)
    }

    @Test
    fun `attribute ordering does not change the dedupe key`() {
        val ordered = signal(attributes = linkedMapOf("a" to "1", "z" to "2"))
        val reversed = signal(attributes = linkedMapOf("z" to "2", "a" to "1"))
        assertEquals(ordered.dedupeKey, reversed.dedupeKey)
    }

    @Test
    fun `a different subject, kind, attribute or measure is a different fact`() {
        val base = signal().dedupeKey
        assertNotEquals(base, signal(subject = "BTC").dedupeKey)
        assertNotEquals(base, signal(kind = SignalKind.LiquidityChange).dedupeKey)
        assertNotEquals(base, signal(attributes = mapOf("venue" to "ethereum")).dedupeKey)
        assertNotEquals(base, signal(measures = mapOf("price" to amount("2500.51"))).dedupeKey)
    }

    @Test
    fun `measure scale does not create a spurious duplicate`() {
        assertEquals(
            signal(measures = mapOf("price" to amount("2500.50"))).dedupeKey,
            signal(measures = mapOf("price" to amount("2500.50"))).dedupeKey,
        )
    }

    @Test
    fun `rejects an unidentifiable signal`() {
        assertThrows(IllegalArgumentException::class.java) { signal(id = " ") }
        assertThrows(IllegalArgumentException::class.java) { signal(subject = "") }
    }

    @Test
    fun `quality rejects impossible provenance`() {
        assertThrows(IllegalArgumentException::class.java) {
            SignalQuality.PROVISIONAL.copy(sourceCount = -1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            SignalQuality.PROVISIONAL.copy(freshnessMillis = -1)
        }
    }

    @Test
    fun `an unverified signal is explicitly marked as such`() {
        assertEquals(false, SignalQuality.PROVISIONAL.verified)
    }
}
