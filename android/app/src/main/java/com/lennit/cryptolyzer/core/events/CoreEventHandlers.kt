package com.lennit.cryptolyzer.core.events

import com.lennit.cryptolyzer.data.local.DomainEventEntity

object CoreEventHandlers {
    fun create(): Map<String, DomainEventHandler> = mapOf(
        "system.heartbeat" to DomainEventHandler { event -> validateHeartbeat(event) }
    )

    private fun validateHeartbeat(event: DomainEventEntity) {
        require(event.type == "system.heartbeat")
        require(event.payloadJson.isNotBlank())
    }
}
