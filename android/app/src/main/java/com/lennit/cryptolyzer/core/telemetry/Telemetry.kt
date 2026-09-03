package com.lennit.cryptolyzer.core.telemetry

import com.lennit.cryptolyzer.data.local.DomainEventDao
import com.lennit.cryptolyzer.data.local.DomainEventEntity
import java.util.UUID

interface Telemetry {
    suspend fun record(eventType: String, attributesJson: String = "{}")
}

class LocalTelemetry(private val dao: DomainEventDao) : Telemetry {
    override suspend fun record(eventType: String, attributesJson: String) {
        dao.insert(
            DomainEventEntity(
                id = UUID.randomUUID().toString(),
                type = "telemetry.$eventType",
                aggregateId = null,
                payloadJson = attributesJson,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )
    }
}
