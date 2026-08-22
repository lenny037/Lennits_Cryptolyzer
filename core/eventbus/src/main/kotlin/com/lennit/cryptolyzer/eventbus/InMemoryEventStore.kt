package com.lennit.cryptolyzer.eventbus

import com.lennit.cryptolyzer.contracts.Outcome
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Reference implementation of [EventStore].
 *
 * It exists for two reasons: fast unit tests, and as the executable specification that the
 * SQLite implementation in core:persistence is verified against by the shared contract test
 * suite. Two implementations of one contract, one test suite.
 */
public class InMemoryEventStore : EventStore {

    private val lock = ReentrantLock()
    private val byId = LinkedHashMap<String, StoredEvent>()
    private val idempotencyIndex = HashMap<String, String>()

    override fun append(envelope: EventEnvelope): Outcome<AppendResult> = lock.withLock {
        val existing = idempotencyIndex[envelope.idempotencyKey]
        if (existing != null) {
            return Outcome.success(AppendResult.Duplicate(existing))
        }
        byId[envelope.eventId] = StoredEvent(
            envelope = envelope,
            status = EventStatus.Pending,
            attempt = 0,
            nextAttemptAtEpochMillis = envelope.recordedAtEpochMillis,
            leaseExpiresAtEpochMillis = null,
            lastError = null,
        )
        idempotencyIndex[envelope.idempotencyKey] = envelope.eventId
        Outcome.success(AppendResult.Appended(envelope.eventId))
    }

    override fun claimPending(
        limit: Int,
        nowEpochMillis: Long,
        leaseMillis: Long,
    ): Outcome<List<StoredEvent>> = lock.withLock {
        require(limit > 0) { "limit must be positive" }
        val claimable = byId.values.asSequence()
            .filter { candidate ->
                when (candidate.status) {
                    EventStatus.Pending -> candidate.nextAttemptAtEpochMillis <= nowEpochMillis
                    // Lease expiry is what makes process death survivable.
                    EventStatus.InFlight ->
                        (candidate.leaseExpiresAtEpochMillis ?: Long.MAX_VALUE) <= nowEpochMillis
                    else -> false
                }
            }
            .sortedBy { it.envelope.occurredAtEpochMillis }
            .take(limit)
            .toList()

        val leased = claimable.map { event ->
            event.copy(
                status = EventStatus.InFlight,
                leaseExpiresAtEpochMillis = nowEpochMillis + leaseMillis,
            )
        }
        leased.forEach { byId[it.eventId] = it }
        Outcome.success(leased)
    }

    override fun markProcessed(eventId: String, nowEpochMillis: Long): Outcome<Unit> = mutate(eventId) {
        it.copy(status = EventStatus.Processed, leaseExpiresAtEpochMillis = null, lastError = null)
    }

    override fun markForRetry(
        eventId: String,
        nextAttemptAtEpochMillis: Long,
        error: String,
    ): Outcome<Unit> = mutate(eventId) {
        it.copy(
            status = EventStatus.Pending,
            attempt = it.attempt + 1,
            nextAttemptAtEpochMillis = nextAttemptAtEpochMillis,
            leaseExpiresAtEpochMillis = null,
            lastError = error,
        )
    }

    override fun markDeadLettered(eventId: String, reason: String, nowEpochMillis: Long): Outcome<Unit> =
        mutate(eventId) {
            it.copy(
                status = EventStatus.DeadLettered,
                leaseExpiresAtEpochMillis = null,
                lastError = reason,
            )
        }

    override fun releaseLease(eventId: String, nextAttemptAtEpochMillis: Long): Outcome<Unit> =
        mutate(eventId) {
            it.copy(
                status = EventStatus.Pending,
                nextAttemptAtEpochMillis = nextAttemptAtEpochMillis,
                leaseExpiresAtEpochMillis = null,
            )
        }

    override fun find(eventId: String): StoredEvent? = lock.withLock { byId[eventId] }

    override fun stats(): EventStoreStats = lock.withLock {
        EventStoreStats(
            pending = byId.values.count { it.status == EventStatus.Pending },
            inFlight = byId.values.count { it.status == EventStatus.InFlight },
            processed = byId.values.count { it.status == EventStatus.Processed },
            deadLettered = byId.values.count { it.status == EventStatus.DeadLettered },
        )
    }

    override fun deadLetters(limit: Int): List<StoredEvent> = lock.withLock {
        byId.values.filter { it.status == EventStatus.DeadLettered }
            .sortedByDescending { it.envelope.recordedAtEpochMillis }
            .take(limit)
    }

    private inline fun mutate(eventId: String, transform: (StoredEvent) -> StoredEvent): Outcome<Unit> =
        lock.withLock {
            val current = byId[eventId]
                ?: return Outcome.failure(
                    com.lennit.cryptolyzer.contracts.PlatformError.Storage("Unknown eventId: $eventId"),
                )
            byId[eventId] = transform(current)
            Outcome.success(Unit)
        }
}
