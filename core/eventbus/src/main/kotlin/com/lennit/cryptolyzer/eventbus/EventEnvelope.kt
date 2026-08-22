package com.lennit.cryptolyzer.eventbus

import com.lennit.cryptolyzer.contracts.ModuleId

/**
 * The single transport unit of the event fabric.
 *
 * Design constraints that come from running on a phone rather than a server:
 *  - [occurredAt] and [recordedAt] are separate, because a device can observe an event while
 *    offline and record it minutes later. Collapsing them destroys ordering analysis.
 *  - [idempotencyKey] is mandatory. At-least-once delivery is the only honest guarantee across
 *    process death, so every consumer must be able to recognise a repeat.
 *  - [schemaVersion] is mandatory. Events outlive the code that wrote them; an unversioned
 *    payload is an unreadable payload after the first migration.
 *  - the payload is a flat string map, so it survives serialization round-trips and schema
 *    evolution without a reflective codec in the hot path.
 */
public data class EventEnvelope(
    val eventId: String,
    val type: EventType,
    val schemaVersion: Int,
    val producer: ModuleId,
    val occurredAtEpochMillis: Long,
    val recordedAtEpochMillis: Long,
    val idempotencyKey: String,
    val payload: Map<String, String>,
    val traceId: String? = null,
) {
    init {
        require(eventId.isNotBlank()) { "eventId cannot be blank" }
        require(idempotencyKey.isNotBlank()) { "idempotencyKey cannot be blank" }
        require(schemaVersion >= 1) { "schemaVersion starts at 1" }
        require(occurredAtEpochMillis >= 0) { "occurredAt cannot be negative" }
        require(recordedAtEpochMillis >= occurredAtEpochMillis) {
            "recordedAt cannot precede occurredAt"
        }
    }
}

/**
 * Namespaced event type. The dotted form doubles as the routing key, and the leading segment is
 * always the owning domain, which keeps the topic space navigable as modules are added.
 */
@JvmInline
public value class EventType(public val name: String) {
    init {
        require(name.matches(TYPE_PATTERN)) {
            "Event type must be lowercase dotted segments, got '$name'"
        }
    }

    override fun toString(): String = name

    public companion object {
        private val TYPE_PATTERN = Regex("^[a-z][a-z0-9]*(\\.[a-z][a-z0-9_]*)+$")

        public val CHAIN_BLOCK_OBSERVED: EventType = EventType("chain.block_observed")
        public val CHAIN_BALANCE_OBSERVED: EventType = EventType("chain.balance_observed")
        public val SIGNAL_PRODUCED: EventType = EventType("intelligence.signal_produced")
        public val PREDICTION_PRODUCED: EventType = EventType("prediction.produced")
        public val PREDICTION_EVALUATED: EventType = EventType("prediction.evaluated")
        public val TREASURY_SNAPSHOT_TAKEN: EventType = EventType("treasury.snapshot_taken")
        public val POLICY_DECISION_RECORDED: EventType = EventType("policy.decision_recorded")
        public val RUNTIME_STATE_CHANGED: EventType = EventType("runtime.state_changed")
    }
}

/** Persisted event plus its processing metadata. */
public data class StoredEvent(
    val envelope: EventEnvelope,
    val status: EventStatus,
    val attempt: Int,
    val nextAttemptAtEpochMillis: Long,
    val leaseExpiresAtEpochMillis: Long?,
    val lastError: String?,
) {
    val eventId: String get() = envelope.eventId
}

public enum class EventStatus {
    /** Durable, waiting for a handler. The default resting state. */
    Pending,

    /** Leased by a worker. A crash here does not lose the event: the lease expires. */
    InFlight,

    /** Handled successfully and acknowledged. */
    Processed,

    /** Exhausted its retry budget. Retained for inspection and manual replay, never dropped. */
    DeadLettered,
}

/** Result of appending. Duplicates are a normal, expected, non-error outcome. */
public sealed interface AppendResult {
    public data class Appended(val eventId: String) : AppendResult
    public data class Duplicate(val existingEventId: String) : AppendResult
}

public data class EventStoreStats(
    val pending: Int,
    val inFlight: Int,
    val processed: Int,
    val deadLettered: Int,
) {
    public val total: Int get() = pending + inFlight + processed + deadLettered
}
