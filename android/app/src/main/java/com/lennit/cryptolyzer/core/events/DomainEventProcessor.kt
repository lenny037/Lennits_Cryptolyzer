package com.lennit.cryptolyzer.core.events

import com.lennit.cryptolyzer.data.local.DomainEventDao
import com.lennit.cryptolyzer.data.local.DomainEventEntity

fun interface DomainEventHandler {
    suspend fun handle(event: DomainEventEntity)
}

class DomainEventProcessor(
    private val dao: DomainEventDao,
    private val handlers: Map<String, DomainEventHandler>
) {
    suspend fun processBatch(limit: Int = 50): Int {
        require(limit in 1..500)
        var processed = 0
        for (event in dao.pending(limit)) {
            val handler = handlers[event.type] ?: continue
            try {
                handler.handle(event)
                dao.markProcessed(event.id)
                processed++
            } catch (_: Exception) {
                // Leave failed events pending so a later retry can process them.
            }
        }
        return processed
    }
}
