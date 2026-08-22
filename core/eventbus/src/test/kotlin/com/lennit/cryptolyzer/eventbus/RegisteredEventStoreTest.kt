package com.lennit.cryptolyzer.eventbus

import com.lennit.cryptolyzer.contracts.ModuleId
import com.lennit.cryptolyzer.eventbus.testing.RegistryFixtures
import com.lennit.cryptolyzer.telemetry.RecordingTelemetry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The enforcement point. These are the tests that decide whether the registry is a control or a
 * document, so the failure modes matter more than the happy path.
 */
class RegisteredEventStoreTest {

    private val telemetry = RecordingTelemetry()
    private val delegate = InMemoryEventStore()
    private val store = RegisteredEventStore(delegate, RegistryFixtures.registry, telemetry)

    private fun sensorEvent(id: String, payload: Map<String, String>) = RegistryFixtures.envelope(
        eventId = id,
        type = RegistryFixtures.SENSOR_READING,
        schemaVersion = 1,
        payload = payload,
    )

    @Test
    fun `a valid event is appended and stored`() {
        val result = store.append(sensorEvent("e1", mapOf("sensor" to "s1", "reading" to "10")))
        assertTrue(result.isSuccess, "${result.errorOrNull()}")
        assertNotNull(delegate.find("e1"))
    }

    @Test
    fun `an unregistered event type never reaches storage`() {
        val envelope = RegistryFixtures.envelope(
            eventId = "e1",
            type = RegistryFixtures.NEVER_REGISTERED,
            schemaVersion = 1,
            payload = emptyMap(),
        )
        assertTrue(!store.append(envelope).isSuccess)
        assertEquals(null, delegate.find("e1"))
        assertTrue(telemetry.records.any { it.name == "eventbus.registry.rejected" })
    }

    @Test
    fun `a malformed payload never reaches storage`() {
        assertTrue(!store.append(sensorEvent("e1", mapOf("sensor" to "s1"))).isSuccess)
        assertEquals(null, delegate.find("e1"))
        assertEquals(0, delegate.stats().total)
    }

    @Test
    fun `an event from a newer schema version never reaches storage`() {
        val envelope = RegistryFixtures.envelope(
            eventId = "e1",
            type = RegistryFixtures.SENSOR_READING,
            schemaVersion = 4,
            payload = mapOf("sensor" to "s1", "reading" to "10"),
        )
        assertTrue(!store.append(envelope).isSuccess)
        assertEquals(0, delegate.stats().total)
    }

    @Test
    fun `claimed events arrive at the current schema version`() {
        // Written directly to the delegate: this is what an older release left behind.
        val legacy = RegistryFixtures.envelope(
            eventId = "e1",
            type = RegistryFixtures.WIDGET_OBSERVED,
            schemaVersion = 1,
            payload = mapOf("source" to "rpc", "value_raw" to "42"),
            producer = ModuleId.BlockchainDataPlane,
        )
        delegate.append(legacy)

        val claimed = requireNotNull(store.claimPending(10, 1_700_000_100_000, 30_000).getOrNull())
        assertEquals(1, claimed.size)
        assertEquals(3, claimed.single().envelope.schemaVersion)
        assertEquals(
            mapOf("source" to "rpc", "value" to "42", "unit" to "wei"),
            claimed.single().envelope.payload,
        )
    }

    @Test
    fun `the stored row is not rewritten by a read, so a downgrade still finds its own bytes`() {
        val legacy = RegistryFixtures.envelope(
            eventId = "e1",
            type = RegistryFixtures.WIDGET_OBSERVED,
            schemaVersion = 1,
            payload = mapOf("source" to "rpc", "value_raw" to "42"),
            producer = ModuleId.BlockchainDataPlane,
        )
        delegate.append(legacy)
        store.claimPending(10, 1_700_000_100_000, 30_000)

        val raw = requireNotNull(delegate.find("e1"))
        assertEquals(1, raw.envelope.schemaVersion)
        assertEquals(mapOf("source" to "rpc", "value_raw" to "42"), raw.envelope.payload)
    }

    @Test
    fun `an unreadable stored event is dead-lettered instead of being delivered or dropped`() {
        // A row whose payload never matched its declared shape: corruption, a hand-edited database,
        // or a producer that bypassed the registry. It must not reach a handler, and it must not
        // vanish.
        val corrupt = RegistryFixtures.envelope(
            eventId = "e1",
            type = RegistryFixtures.WIDGET_OBSERVED,
            schemaVersion = 1,
            payload = mapOf("source" to "rpc"),
            producer = ModuleId.BlockchainDataPlane,
        )
        delegate.append(corrupt)

        val claimed = requireNotNull(store.claimPending(10, 1_700_000_100_000, 30_000).getOrNull())
        assertTrue(claimed.isEmpty(), "an unreadable event must not be delivered")

        val stored = requireNotNull(delegate.find("e1"))
        assertEquals(EventStatus.DeadLettered, stored.status)
        assertTrue(stored.lastError!!.startsWith("schema:"))
        assertEquals(1, delegate.stats().deadLettered)
        assertTrue(telemetry.records.any { it.name == "eventbus.registry.unreadable" })
    }

    @Test
    fun `diagnostic reads show an unreadable row as it is stored`() {
        val corrupt = RegistryFixtures.envelope(
            eventId = "e1",
            type = RegistryFixtures.WIDGET_OBSERVED,
            schemaVersion = 1,
            payload = mapOf("source" to "rpc"),
            producer = ModuleId.BlockchainDataPlane,
        )
        delegate.append(corrupt)

        val found = requireNotNull(store.find("e1"))
        assertEquals(1, found.envelope.schemaVersion)
        assertEquals(mapOf("source" to "rpc"), found.envelope.payload)
    }

    @Test
    fun `dead letters are lifted when they can be read`() {
        val legacy = RegistryFixtures.envelope(
            eventId = "e1",
            type = RegistryFixtures.LEDGER_ENTRY,
            schemaVersion = 1,
            payload = mapOf("amount" to "1.25"),
            producer = ModuleId.Treasury,
        )
        delegate.append(legacy)
        delegate.markDeadLettered("e1", "budget exhausted", 1_700_000_100_000)

        val letters = store.deadLetters(10)
        assertEquals(1, letters.size)
        assertEquals(2, letters.single().envelope.schemaVersion)
        assertEquals(mapOf("amount" to "1.25", "memo" to ""), letters.single().envelope.payload)
    }

    @Test
    fun `lifecycle calls pass straight through to the delegate`() {
        store.append(sensorEvent("e1", mapOf("sensor" to "s1", "reading" to "10")))
        store.claimPending(10, 1_700_000_100_000, 30_000)
        assertTrue(store.markProcessed("e1", 1_700_000_200_000).isSuccess)
        assertEquals(1, store.stats().processed)
    }
}
