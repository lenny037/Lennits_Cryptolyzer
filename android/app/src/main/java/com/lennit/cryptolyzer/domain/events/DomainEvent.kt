package com.lennit.cryptolyzer.domain.events

data class DomainEvent(
    val eventId: String,
    val eventType: String,
    val aggregateId: String?,
    val occurredAtEpochMs: Long,
    val payloadJson: String,
    val idempotencyKey: String
)
