package com.lennit.cryptolyzer.core.events

import com.lennit.cryptolyzer.data.local.DomainEventDao
import com.lennit.cryptolyzer.data.local.DomainEventEntity
import java.util.UUID

class DomainEventBus(private val dao: DomainEventDao) {
    suspend fun publish(type: String, aggregateId: String? = null, payloadJson: String = "{}") {
        dao.insert(
            DomainEventEntity(
                id = UUID.randomUUID().toString(),
                type = type,
                aggregateId = aggregateId,
                payloadJson = payloadJson,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )
    }
}
