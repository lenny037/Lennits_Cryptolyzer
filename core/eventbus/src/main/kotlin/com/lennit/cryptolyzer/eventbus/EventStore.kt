package com.lennit.cryptolyzer.eventbus

import com.lennit.cryptolyzer.contracts.Outcome

/**
 * Durable event log port.
 *
 * The contract, not the implementation, is what guarantees the Phase 3 gate that no event
 * silently disappears:
 *  1. [append] is idempotent on [EventEnvelope.idempotencyKey].
 *  2. [claimPending] hands out time-bounded leases; an unfinished lease returns to Pending once
 *     it expires, so a killed process cannot strand work.
 *  3. There is no delete operation. Terminal states are Processed and DeadLettered, both retained.
 */
public interface EventStore {

    public fun append(envelope: EventEnvelope): Outcome<AppendResult>

    /**
     * Atomically leases up to [limit] events that are due at [nowEpochMillis], including events
     * whose previous lease has expired.
     */
    public fun claimPending(limit: Int, nowEpochMillis: Long, leaseMillis: Long): Outcome<List<StoredEvent>>

    public fun markProcessed(eventId: String, nowEpochMillis: Long): Outcome<Unit>

    /** Returns the event to Pending with a computed next attempt time. */
    public fun markForRetry(
        eventId: String,
        nextAttemptAtEpochMillis: Long,
        error: String,
    ): Outcome<Unit>

    /** Terminal, but retained. Used when the retry budget is exhausted or the payload is invalid. */
    public fun markDeadLettered(eventId: String, reason: String, nowEpochMillis: Long): Outcome<Unit>

    /** Releases a lease without consuming an attempt. Used when no handler is registered yet. */
    public fun releaseLease(eventId: String, nextAttemptAtEpochMillis: Long): Outcome<Unit>

    public fun find(eventId: String): StoredEvent?

    public fun stats(): EventStoreStats

    /** Dead letters, newest first, for the diagnostics surface and manual replay. */
    public fun deadLetters(limit: Int): List<StoredEvent>
}
