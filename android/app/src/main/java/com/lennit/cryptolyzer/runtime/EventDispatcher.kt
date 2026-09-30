package com.lennit.cryptolyzer.runtime

import com.lennit.cryptolyzer.domain.events.DomainEvent
import com.lennit.cryptolyzer.domain.events.EventRepository

interface EventHandler {
    val eventType: String
    suspend fun handle(event: DomainEvent)
}

class EventDispatcher(
    private val repository: EventRepository,
    handlers: List<EventHandler>
) {
    private val handlersByType = handlers.associateBy(EventHandler::eventType)

    suspend fun dispatchBatch(limit: Int, nowEpochMs: Long): Int {
        var processed = 0
        for (event in repository.claimPending(limit, nowEpochMs)) {
            val handler = handlersByType[event.eventType] ?: continue
            try {
                handler.handle(event)
                if (repository.markProcessed(event.eventId, nowEpochMs)) processed++
            } catch (_: Throwable) {
                repository.recordFailure(event.eventId, nowEpochMs + 30_000L, "handler failure")
            }
        }
        return processed
    }
}
