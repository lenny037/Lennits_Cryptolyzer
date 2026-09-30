package com.lennit.cryptolyzer.persistence

import com.lennit.cryptolyzer.domain.events.DomainEvent
import com.lennit.cryptolyzer.domain.events.EventRepository
import java.util.concurrent.ConcurrentHashMap

/** Temporary process-safe repository boundary until the Room adapter is wired. */
class LocalEventStore : EventRepository {
    private val events = ConcurrentHashMap<String, DomainEvent>()
    private val processed = ConcurrentHashMap.newKeySet<String>()

    override suspend fun append(event: DomainEvent): Boolean {
        if (events.values.any { it.idempotencyKey == event.idempotencyKey }) return false
        return events.putIfAbsent(event.eventId, event) == null
    }

    override suspend fun claimPending(limit: Int, nowEpochMs: Long): List<DomainEvent> =
        events.values.asSequence()
            .filter { it.eventId !in processed }
            .sortedBy { it.occurredAtEpochMs }
            .take(limit.coerceAtLeast(0))
            .toList()

    override suspend fun markProcessed(eventId: String, processedAtEpochMs: Long): Boolean =
        events.containsKey(eventId) && processed.add(eventId)

    override suspend fun recordFailure(eventId: String, availableAtEpochMs: Long, error: String): Boolean =
        events.containsKey(eventId)
}
