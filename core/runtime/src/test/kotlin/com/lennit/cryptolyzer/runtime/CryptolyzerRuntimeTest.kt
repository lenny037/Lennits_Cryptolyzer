package com.lennit.cryptolyzer.runtime

import com.lennit.cryptolyzer.contracts.HealthState
import com.lennit.cryptolyzer.contracts.MutableClock
import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.telemetry.RecordingTelemetry
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CryptolyzerRuntimeTest {

    private val clock = MutableClock(1_000)
    private val telemetry = RecordingTelemetry()

    private fun runtimeOf(vararg services: FakeService): CryptolyzerRuntime {
        val graph = when (val result = RuntimeGraph.of(services.toList())) {
            is Outcome.Success -> result.value
            is Outcome.Failure -> throw AssertionError(result.error.message)
        }
        return CryptolyzerRuntime(graph, clock, telemetry)
    }

    @Test
    fun `starts every service in dependency order`() = runTest {
        val log = mutableListOf<String>()
        val runtime = runtimeOf(
            FakeService("api", dependsOnNames = setOf("store"), log = log),
            FakeService("store", log = log),
        )
        assertTrue(runtime.start() is Outcome.Success)
        assertEquals(listOf("start:store", "start:api"), log)
        assertEquals(CryptolyzerRuntime.State.Running, runtime.state)
    }

    @Test
    fun `start is idempotent, because Android recreates the process routinely`() = runTest {
        val store = FakeService("store")
        val runtime = runtimeOf(store)
        runtime.start()
        runtime.start()
        runtime.start()
        assertEquals(1, store.startCount)
        assertEquals(CryptolyzerRuntime.State.Running, runtime.state)
    }

    @Test
    fun `a failed start rolls back the services already started`() = runTest {
        val log = mutableListOf<String>()
        val store = FakeService("store", log = log)
        val broken = FakeService(
            "api",
            dependsOnNames = setOf("store"),
            startBehaviour = FakeService.Behaviour.Fail,
            log = log,
        )
        val runtime = runtimeOf(store, broken)

        val result = runtime.start()
        assertTrue(result is Outcome.Failure)
        assertEquals(CryptolyzerRuntime.State.Faulted, runtime.state)
        assertEquals(listOf("start:store", "start:api", "stop:store"), log)
        assertTrue(runtime.startedServices.isEmpty(), "a half-started runtime must not be the resting state")
    }

    @Test
    fun `a service that throws during start is treated as a failed start, not a crash`() = runTest {
        val runtime = runtimeOf(
            FakeService("store"),
            FakeService("api", dependsOnNames = setOf("store"), startBehaviour = FakeService.Behaviour.Throw),
        )
        val result = runtime.start()
        assertTrue(result is Outcome.Failure)
        assertTrue((result as Outcome.Failure).error.message.contains("threw during start"), result.error.message)
        assertEquals(CryptolyzerRuntime.State.Faulted, runtime.state)
    }

    @Test
    fun `services downstream of a failure are never started`() = runTest {
        val downstream = FakeService("ui", dependsOnNames = setOf("api"))
        val runtime = runtimeOf(
            FakeService("store"),
            FakeService("api", dependsOnNames = setOf("store"), startBehaviour = FakeService.Behaviour.Fail),
            downstream,
        )
        runtime.start()
        assertEquals(0, downstream.startCount)
    }

    @Test
    fun `stop runs in reverse order`() = runTest {
        val log = mutableListOf<String>()
        val runtime = runtimeOf(
            FakeService("api", dependsOnNames = setOf("store"), log = log),
            FakeService("store", log = log),
        )
        runtime.start()
        log.clear()
        runtime.stop()
        assertEquals(listOf("stop:api", "stop:store"), log)
        assertEquals(CryptolyzerRuntime.State.Stopped, runtime.state)
    }

    @Test
    fun `stop gives every service its chance to flush even when one fails`() = runTest {
        val flushed = FakeService("store")
        val runtime = runtimeOf(
            FakeService("api", dependsOnNames = setOf("store"), stopBehaviour = FakeService.Behaviour.Fail),
            flushed,
        )
        runtime.start()
        val result = runtime.stop()

        assertTrue(result is Outcome.Success, "a noisy shutdown is still a completed shutdown")
        assertEquals(1, flushed.stopCount, "a failure upstream must not skip the remaining services")
        assertTrue(telemetry.records.any { it.name == "runtime.stop_failed" }, "the failure must be reported")
    }

    @Test
    fun `a service that throws during stop does not prevent shutdown`() = runTest {
        val flushed = FakeService("store")
        val runtime = runtimeOf(
            FakeService("api", dependsOnNames = setOf("store"), stopBehaviour = FakeService.Behaviour.Throw),
            flushed,
        )
        runtime.start()
        assertTrue(runtime.stop() is Outcome.Success)
        assertEquals(CryptolyzerRuntime.State.Stopped, runtime.state)
        assertEquals(1, flushed.stopCount)
    }

    @Test
    fun `stopping a runtime that never started is a no-op`() = runTest {
        val store = FakeService("store")
        val runtime = runtimeOf(store)
        assertTrue(runtime.stop() is Outcome.Success)
        assertEquals(0, store.stopCount)
    }

    @Test
    fun `restart brings the runtime back to running`() = runTest {
        val store = FakeService("store")
        val runtime = runtimeOf(store)
        runtime.start()
        assertTrue(runtime.restart() is Outcome.Success)
        assertEquals(CryptolyzerRuntime.State.Running, runtime.state)
        assertEquals(2, store.startCount)
        assertEquals(1, store.stopCount)
    }

    @Test
    fun `health aggregates every service and stays usable while degraded`() = runTest {
        val runtime = runtimeOf(
            FakeService("store"),
            FakeService("api", dependsOnNames = setOf("store"), healthState = HealthState.Degraded),
        )
        runtime.start()
        val health = runtime.health()

        assertEquals(2, health.services.size)
        assertEquals(1, health.degraded.size)
        assertTrue(health.unhealthy.isEmpty())
        assertTrue(health.isUsable, "offline with a warm cache is a supported mode, not an outage")
    }

    @Test
    fun `an unhealthy service makes the runtime unusable`() = runTest {
        val runtime = runtimeOf(FakeService("store", healthState = HealthState.Unhealthy))
        runtime.start()
        assertTrue(!runtime.health().isUsable)
    }

    @Test
    fun `a health probe that throws is reported as unhealthy rather than failing the probe`() = runTest {
        val runtime = runtimeOf(FakeService("store", healthThrows = true))
        runtime.start()
        val health = runtime.health()
        assertEquals(1, health.unhealthy.size)
        assertTrue(health.unhealthy.first().detail?.contains("health probe threw") == true)
    }

    @Test
    fun `health is answerable before start and reports the idle state`() = runTest {
        val runtime = runtimeOf(FakeService("store"))
        val health = runtime.health()
        assertEquals(CryptolyzerRuntime.State.Idle, health.runtimeState)
        assertTrue(!health.isUsable)
    }
}
