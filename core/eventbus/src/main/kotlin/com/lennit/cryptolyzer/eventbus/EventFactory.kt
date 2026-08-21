package com.lennit.cryptolyzer.eventbus

import com.lennit.cryptolyzer.contracts.Clock
import com.lennit.cryptolyzer.contracts.IdGenerator
import com.lennit.cryptolyzer.contracts.ModuleId

/**
 * Builds envelopes so that no producer has to remember the invariants.
 *
 * Producers supply what only they know: type, payload, when it happened, and a natural
 * idempotency key. Everything mechanical, including ids and record time, is filled in here.
 */
public class EventFactory(
    private val clock: Clock,
    private val ids: IdGenerator,
    private val producer: ModuleId,
) {
    public fun create(
        type: EventType,
        payload: Map<String, String>,
        idempotencyKey: String,
        occurredAtEpochMillis: Long = clock.nowEpochMillis(),
        schemaVersion: Int = 1,
        traceId: String? = null,
    ): EventEnvelope {
        val recordedAt = clock.nowEpochMillis().coerceAtLeast(occurredAtEpochMillis)
        return EventEnvelope(
            eventId = ids.newId(),
            type = type,
            schemaVersion = schemaVersion,
            producer = producer,
            occurredAtEpochMillis = occurredAtEpochMillis,
            recordedAtEpochMillis = recordedAt,
            // Namespacing the key by type and schema prevents a collision between two producers
            // that happen to use the same natural key for different facts.
            idempotencyKey = "${type.name}:v$schemaVersion:$idempotencyKey",
            payload = payload,
            traceId = traceId,
        )
    }
}
