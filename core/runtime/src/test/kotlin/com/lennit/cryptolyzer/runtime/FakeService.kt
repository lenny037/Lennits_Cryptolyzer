package com.lennit.cryptolyzer.runtime

import com.lennit.cryptolyzer.contracts.HealthReport
import com.lennit.cryptolyzer.contracts.HealthState
import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.contracts.PlatformError
import com.lennit.cryptolyzer.contracts.RuntimeService
import com.lennit.cryptolyzer.contracts.ServiceId

/** A controllable service used to drive every branch of the supervisor's lifecycle. */
internal class FakeService(
    name: String,
    dependsOnNames: Set<String> = emptySet(),
    private val startBehaviour: Behaviour = Behaviour.Succeed,
    private val stopBehaviour: Behaviour = Behaviour.Succeed,
    private val healthState: HealthState = HealthState.Healthy,
    private val healthThrows: Boolean = false,
    private val log: MutableList<String>? = null,
) : RuntimeService {

    internal enum class Behaviour { Succeed, Fail, Throw }

    override val id: ServiceId = ServiceId(name)
    override val dependsOn: Set<ServiceId> = dependsOnNames.map(::ServiceId).toSet()

    var startCount: Int = 0
        private set
    var stopCount: Int = 0
        private set

    override suspend fun start(): Outcome<Unit> {
        startCount++
        log?.add("start:${id.value}")
        return when (startBehaviour) {
            Behaviour.Succeed -> Outcome.success(Unit)
            Behaviour.Fail -> Outcome.failure(PlatformError.Storage("${id.value} could not open its store"))
            Behaviour.Throw -> error("${id.value} exploded")
        }
    }

    override suspend fun stop(): Outcome<Unit> {
        stopCount++
        log?.add("stop:${id.value}")
        return when (stopBehaviour) {
            Behaviour.Succeed -> Outcome.success(Unit)
            Behaviour.Fail -> Outcome.failure(PlatformError.Storage("${id.value} failed to flush"))
            Behaviour.Throw -> error("${id.value} exploded on stop")
        }
    }

    override suspend fun health(): HealthReport {
        if (healthThrows) error("${id.value} health probe exploded")
        return HealthReport(
            serviceId = id,
            state = healthState,
            checkedAtEpochMillis = 1_000,
        )
    }
}
