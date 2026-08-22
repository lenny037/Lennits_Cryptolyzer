package com.lennit.cryptolyzer.eventbus

import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.telemetry.Telemetry

/**
 * Enforces the [EventRegistry] contract around any [EventStore].
 *
 * Composition rather than inheritance, for two reasons. The storage implementations stay ignorant
 * of schema policy, so `SqliteEventStore` remains a dumb durable log that the contract suite can
 * verify on its own terms; and the enforcement point is explicit in the runtime wiring, so it is
 * visible in review whether a given store is registry-checked or raw.
 *
 * Write path: an unregistered type, an unknown version, or a payload that does not match the
 * declared shape is refused before anything is persisted. Refusing early is the cheap option — a
 * malformed row on a phone cannot be fixed by a server-side backfill.
 *
 * Read path: payloads are lifted to the current declared version by the registry's upcasts. The
 * stored row is never rewritten (ADR-0015), so an application downgrade still finds the bytes it
 * originally wrote.
 *
 * An event that cannot be lifted is dead-lettered rather than dropped or replayed forever. That is
 * the one honest disposition: it is retained for inspection, it stops consuming retry budget, and
 * it never reaches a handler in a shape the handler cannot read.
 */
public class RegisteredEventStore(
    private val delegate: EventStore,
    private val registry: EventRegistry = EventRegistry.RELEASED,
    private val telemetry: Telemetry = Telemetry.noop(),
) : EventStore {

    override fun append(envelope: EventEnvelope): Outcome<AppendResult> =
        when (val validated = registry.validate(envelope)) {
            is Outcome.Failure -> {
                telemetry.failure(
                    "eventbus.registry.rejected",
                    validated.error,
                    mapOf("type" to envelope.type.name, "schema_version" to envelope.schemaVersion.toString()),
                )
                validated
            }
            is Outcome.Success -> delegate.append(envelope)
        }

    override fun claimPending(
        limit: Int,
        nowEpochMillis: Long,
        leaseMillis: Long,
    ): Outcome<List<StoredEvent>> =
        when (val claimed = delegate.claimPending(limit, nowEpochMillis, leaseMillis)) {
            is Outcome.Failure -> claimed
            is Outcome.Success -> Outcome.success(
                claimed.value.mapNotNull { stored -> liftForDelivery(stored, nowEpochMillis) },
            )
        }

    override fun markProcessed(eventId: String, nowEpochMillis: Long): Outcome<Unit> =
        delegate.markProcessed(eventId, nowEpochMillis)

    override fun markForRetry(
        eventId: String,
        nextAttemptAtEpochMillis: Long,
        error: String,
    ): Outcome<Unit> = delegate.markForRetry(eventId, nextAttemptAtEpochMillis, error)

    override fun markDeadLettered(
        eventId: String,
        reason: String,
        nowEpochMillis: Long,
    ): Outcome<Unit> = delegate.markDeadLettered(eventId, reason, nowEpochMillis)

    override fun releaseLease(eventId: String, nextAttemptAtEpochMillis: Long): Outcome<Unit> =
        delegate.releaseLease(eventId, nextAttemptAtEpochMillis)

    /**
     * Diagnostic read. Upcasts when possible and returns the row **unchanged** when not: an
     * inspection surface must show what is actually stored, not hide the row that needs attention.
     */
    override fun find(eventId: String): StoredEvent? =
        delegate.find(eventId)?.let { stored -> liftOrRaw(stored) }

    override fun stats(): EventStoreStats = delegate.stats()

    override fun deadLetters(limit: Int): List<StoredEvent> =
        delegate.deadLetters(limit).map { stored -> liftOrRaw(stored) }

    // ------------------------------------------------------------------ internals

    private fun liftForDelivery(stored: StoredEvent, nowEpochMillis: Long): StoredEvent? =
        when (val lifted = registry.upcast(stored.envelope)) {
            is Outcome.Success -> stored.copy(envelope = lifted.value)
            is Outcome.Failure -> {
                telemetry.failure(
                    "eventbus.registry.unreadable",
                    lifted.error,
                    mapOf(
                        "event_id" to stored.eventId,
                        "type" to stored.envelope.type.name,
                        "schema_version" to stored.envelope.schemaVersion.toString(),
                    ),
                )
                // Retained, terminal, inspectable. Never delivered in a shape a handler cannot read.
                delegate.markDeadLettered(
                    stored.eventId,
                    "schema: ${lifted.error.message}",
                    nowEpochMillis,
                )
                null
            }
        }

    private fun liftOrRaw(stored: StoredEvent): StoredEvent =
        when (val lifted = registry.upcast(stored.envelope)) {
            is Outcome.Success -> stored.copy(envelope = lifted.value)
            is Outcome.Failure -> stored
        }
}
