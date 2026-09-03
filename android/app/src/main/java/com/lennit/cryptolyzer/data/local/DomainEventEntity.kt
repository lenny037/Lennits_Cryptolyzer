package com.lennit.cryptolyzer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "domain_events")
data class DomainEventEntity(
    @PrimaryKey val id: String,
    val type: String,
    val aggregateId: String?,
    val payloadJson: String,
    val createdAtEpochMs: Long,
    val processed: Boolean = false
)
