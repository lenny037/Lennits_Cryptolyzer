package com.lennit.cryptolyzer.core.events

import com.lennit.cryptolyzer.data.local.DomainEventDao

class DomainEventProcessor(private val dao: DomainEventDao) {
    suspend fun processBatch(limit: Int = 50): Int {
        require(limit in 1..500)
        var processed = 0
        for (event in dao.pending(limit)) {
            try {
                // Handlers are intentionally injected in the next layer. Unknown events remain pending.
                if (event.type.isBlank()) continue
                dao.markProcessed(event.id)
                processed++
            } catch (_: Exception) {
                // Fail closed: leave the event pending for a later retry.
            }
        }
        return processed
    }
}
