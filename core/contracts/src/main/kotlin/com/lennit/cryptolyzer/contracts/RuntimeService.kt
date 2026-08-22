package com.lennit.cryptolyzer.contracts

/**
 * Lifecycle contract every long-lived subsystem obeys.
 *
 * On Android, process death and background execution limits are normal operating conditions,
 * not exceptional ones. Therefore [start] must be idempotent and safe to call after an abrupt
 * kill, and [stop] must be able to run during a constrained shutdown window.
 */
public interface RuntimeService {

    public val id: ServiceId

    /** Services that must be running before this one starts. Used to compute start order. */
    public val dependsOn: Set<ServiceId> get() = emptySet()

    /** Idempotent. Must not block indefinitely. */
    public suspend fun start(): Outcome<Unit>

    /** Idempotent, best-effort, must not throw. */
    public suspend fun stop(): Outcome<Unit>

    /** Cheap, side-effect-free probe. */
    public suspend fun health(): HealthReport
}

@JvmInline
public value class ServiceId(public val value: String) {
    init {
        require(value.isNotBlank()) { "ServiceId cannot be blank" }
    }

    override fun toString(): String = value
}

public data class HealthReport(
    val serviceId: ServiceId,
    val state: HealthState,
    val detail: String? = null,
    val checkedAtEpochMillis: Long,
) {
    public val isUsable: Boolean get() = state == HealthState.Healthy || state == HealthState.Degraded
}

public enum class HealthState {
    /** Fully operational. */
    Healthy,

    /** Operating with reduced capability, for example offline with a warm local cache. */
    Degraded,

    /** Not operational. Dependent services must not assume its output. */
    Unhealthy,

    /** Not started yet. Distinct from Unhealthy so that startup order bugs are visible. */
    Stopped,
}
