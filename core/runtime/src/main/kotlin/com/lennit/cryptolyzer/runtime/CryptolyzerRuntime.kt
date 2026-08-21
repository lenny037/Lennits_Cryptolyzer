package com.lennit.cryptolyzer.runtime

import com.lennit.cryptolyzer.contracts.Clock
import com.lennit.cryptolyzer.contracts.HealthReport
import com.lennit.cryptolyzer.contracts.HealthState
import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.contracts.PlatformError
import com.lennit.cryptolyzer.contracts.ServiceId
import com.lennit.cryptolyzer.telemetry.Telemetry
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The application runtime: the phone's equivalent of a service supervisor.
 *
 * Behaviour that Android forces on the design:
 *  - [start] is idempotent, because the process is killed and recreated routinely.
 *  - a failed start rolls back the services already started, so a half-initialised runtime never
 *    becomes the resting state.
 *  - [stop] never throws and never aborts early on a failure, because a shutdown window can be
 *    a few hundred milliseconds and every service still deserves its chance to flush.
 */
public class CryptolyzerRuntime(
    private val graph: RuntimeGraph,
    private val clock: Clock,
    private val telemetry: Telemetry = Telemetry.noop(),
) {
    public enum class State { Idle, Starting, Running, Stopping, Stopped, Faulted }

    private val mutex = Mutex()
    private var currentState: State = State.Idle
    private val started = linkedSetOf<ServiceId>()

    public val state: State get() = currentState

    public val startedServices: Set<ServiceId> get() = started.toSet()

    public suspend fun start(): Outcome<Unit> = mutex.withLock {
        if (currentState == State.Running) {
            return Outcome.success(Unit) // idempotent
        }
        currentState = State.Starting
        telemetry.event("runtime.starting", mapOf("services" to graph.size.toString()))

        for (service in graph.servicesInStartOrder()) {
            val outcome = runCatching { service.start() }
                .getOrElse { throwable ->
                    Outcome.failure(
                        PlatformError.Unknown(
                            "Service ${service.id} threw during start: ${throwable::class.simpleName}",
                            cause = throwable,
                        ),
                    )
                }
            when (outcome) {
                is Outcome.Success -> started += service.id
                is Outcome.Failure -> {
                    telemetry.failure("runtime.start_failed", outcome.error, mapOf("service" to service.id.value))
                    rollback()
                    currentState = State.Faulted
                    return Outcome.failure(outcome.error)
                }
            }
        }
        currentState = State.Running
        telemetry.event("runtime.running", mapOf("services" to started.size.toString()))
        Outcome.success(Unit)
    }

    public suspend fun stop(): Outcome<Unit> = mutex.withLock {
        if (currentState == State.Stopped || currentState == State.Idle) {
            return Outcome.success(Unit)
        }
        currentState = State.Stopping
        val failures = stopStartedServices()
        currentState = State.Stopped
        telemetry.event("runtime.stopped", mapOf("failures" to failures.size.toString()))
        // A noisy shutdown is reported but is not an error: the runtime is stopped either way.
        Outcome.success(Unit)
    }

    /** Stop then start. Used after an unrecoverable subsystem fault or a config change. */
    public suspend fun restart(): Outcome<Unit> {
        stop()
        return start()
    }

    public suspend fun health(): RuntimeHealth {
        val reports = graph.servicesInStartOrder().map { service ->
            runCatching { service.health() }.getOrElse {
                HealthReport(
                    serviceId = service.id,
                    state = HealthState.Unhealthy,
                    detail = "health probe threw ${it::class.simpleName}",
                    checkedAtEpochMillis = clock.nowEpochMillis(),
                )
            }
        }
        return RuntimeHealth(
            runtimeState = currentState,
            checkedAtEpochMillis = clock.nowEpochMillis(),
            services = reports,
        )
    }

    private suspend fun rollback() {
        stopStartedServices()
    }

    private suspend fun stopStartedServices(): List<PlatformError> {
        val failures = mutableListOf<PlatformError>()
        graph.servicesInStopOrder()
            .filter { it.id in started }
            .forEach { service ->
                val outcome = runCatching { service.stop() }.getOrElse { throwable ->
                    Outcome.failure(
                        PlatformError.Unknown(
                            "Service ${service.id} threw during stop: ${throwable::class.simpleName}",
                            cause = throwable,
                        ),
                    )
                }
                if (outcome is Outcome.Failure) {
                    failures += outcome.error
                    telemetry.failure("runtime.stop_failed", outcome.error, mapOf("service" to service.id.value))
                }
            }
        started.clear()
        return failures
    }
}

public data class RuntimeHealth(
    val runtimeState: CryptolyzerRuntime.State,
    val checkedAtEpochMillis: Long,
    val services: List<HealthReport>,
) {
    public val unhealthy: List<HealthReport> get() = services.filter { it.state == HealthState.Unhealthy }

    public val degraded: List<HealthReport> get() = services.filter { it.state == HealthState.Degraded }

    /**
     * The whole runtime is usable when nothing is outright unhealthy. Degraded is explicitly
     * acceptable: offline operation with a warm cache is a supported mode, not an outage.
     */
    public val isUsable: Boolean
        get() = runtimeState == CryptolyzerRuntime.State.Running && unhealthy.isEmpty()
}
