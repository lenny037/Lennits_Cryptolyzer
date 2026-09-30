package com.lennit.cryptolyzer.domain.events

interface EventRepository {
    suspend fun append(event: DomainEvent): Boolean
    suspend fun claimPending(limit: Int, nowEpochMs: Long): List<DomainEvent>
    suspend fun markProcessed(eventId: String, processedAtEpochMs: Long): Boolean
    suspend fun recordFailure(eventId: String, availableAtEpochMs: Long, error: String): Boolean
}
