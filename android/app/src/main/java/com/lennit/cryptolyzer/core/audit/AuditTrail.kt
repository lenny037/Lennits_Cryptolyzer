package com.lennit.cryptolyzer.core.audit

import com.lennit.cryptolyzer.core.events.DomainEventBus

class AuditTrail(private val events: DomainEventBus) {
    suspend fun record(actor: String, action: String, outcome: String, detailsJson: String = "{}") {
        events.publish(
            type = "audit.$action",
            payloadJson = "{\"actor\":\"${escape(actor)}\",\"outcome\":\"${escape(outcome)}\",\"details\":$detailsJson}"
        )
    }

    private fun escape(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
}
