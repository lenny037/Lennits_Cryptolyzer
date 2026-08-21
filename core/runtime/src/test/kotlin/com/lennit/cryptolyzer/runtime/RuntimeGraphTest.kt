package com.lennit.cryptolyzer.runtime

import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.contracts.PlatformError
import com.lennit.cryptolyzer.contracts.ServiceId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RuntimeGraphTest {

    private fun graphOf(vararg services: FakeService): Outcome<RuntimeGraph> =
        RuntimeGraph.of(services.toList())

    private fun expectGraph(vararg services: FakeService): RuntimeGraph =
        when (val result = graphOf(*services)) {
            is Outcome.Success -> result.value
            is Outcome.Failure -> throw AssertionError("expected a graph: ${result.error.message}")
        }

    private fun invariantOf(result: Outcome<*>): String {
        assertTrue(result is Outcome.Failure, "expected a refusal, got $result")
        return ((result as Outcome.Failure).error as PlatformError.InvariantViolation).invariant
    }

    @Test
    fun `orders dependencies before their dependents`() {
        val graph = expectGraph(
            FakeService("api", dependsOnNames = setOf("store")),
            FakeService("store"),
            FakeService("ui", dependsOnNames = setOf("api")),
        )
        assertEquals(listOf("store", "api", "ui"), graph.startOrder.map { it.value })
    }

    @Test
    fun `resolves a diamond dependency exactly once each`() {
        val graph = expectGraph(
            FakeService("clock"),
            FakeService("left", dependsOnNames = setOf("clock")),
            FakeService("right", dependsOnNames = setOf("clock")),
            FakeService("top", dependsOnNames = setOf("left", "right")),
        )
        val order = graph.startOrder.map { it.value }
        assertEquals(4, order.size)
        assertEquals(4, order.toSet().size)
        assertTrue(order.indexOf("clock") < order.indexOf("left"))
        assertTrue(order.indexOf("left") < order.indexOf("top"))
        assertTrue(order.indexOf("right") < order.indexOf("top"))
    }

    @Test
    fun `start order is deterministic for independent services`() {
        val first = expectGraph(FakeService("c"), FakeService("a"), FakeService("b")).startOrder
        val second = expectGraph(FakeService("b"), FakeService("c"), FakeService("a")).startOrder
        assertEquals(first, second)
        assertEquals(listOf("a", "b", "c"), first.map { it.value })
    }

    @Test
    fun `stop order is the exact reverse of start order`() {
        val graph = expectGraph(
            FakeService("api", dependsOnNames = setOf("store")),
            FakeService("store"),
        )
        assertEquals(
            graph.servicesInStartOrder().map { it.id }.reversed(),
            graph.servicesInStopOrder().map { it.id },
        )
    }

    @Test
    fun `detects a direct two-node cycle and names the participants`() {
        val result = graphOf(
            FakeService("a", dependsOnNames = setOf("b")),
            FakeService("b", dependsOnNames = setOf("a")),
        )
        assertEquals("runtime.acyclic-dependencies", invariantOf(result))
        assertTrue((result as Outcome.Failure).error.message.contains("a depends on b"), result.error.message)
    }

    @Test
    fun `detects a longer indirect cycle`() {
        val result = graphOf(
            FakeService("a", dependsOnNames = setOf("c")),
            FakeService("b", dependsOnNames = setOf("a")),
            FakeService("c", dependsOnNames = setOf("b")),
        )
        assertEquals("runtime.acyclic-dependencies", invariantOf(result))
    }

    @Test
    fun `detects a self-dependency`() {
        val result = graphOf(FakeService("a", dependsOnNames = setOf("a")))
        assertEquals("runtime.acyclic-dependencies", invariantOf(result))
    }

    @Test
    fun `refuses an unregistered dependency rather than starting a partial system`() {
        val result = graphOf(FakeService("api", dependsOnNames = setOf("store")))
        assertEquals("runtime.resolvable-dependencies", invariantOf(result))
        assertTrue((result as Outcome.Failure).error.message.contains("api -> store"), result.error.message)
    }

    @Test
    fun `refuses duplicate service ids, which would make lookup ambiguous`() {
        assertEquals("runtime.unique-service-id", invariantOf(graphOf(FakeService("a"), FakeService("a"))))
    }

    @Test
    fun `an empty graph is valid and starts nothing`() {
        val graph = expectGraph()
        assertEquals(0, graph.size)
        assertEquals(emptyList<com.lennit.cryptolyzer.contracts.ServiceId>(), graph.startOrder)
    }

    @Test
    fun `services are retrievable by id and absent ids return null`() {
        val graph = expectGraph(FakeService("a"))
        assertTrue(graph.service(ServiceId("a")) != null)
        assertTrue(graph.service(ServiceId("nope")) == null)
    }

    @Test
    fun `a cycle isolated from a healthy subgraph is still refused`() {
        val result = graphOf(
            FakeService("standalone"),
            FakeService("a", dependsOnNames = setOf("b")),
            FakeService("b", dependsOnNames = setOf("a")),
        )
        assertEquals("runtime.acyclic-dependencies", invariantOf(result))
    }
}
