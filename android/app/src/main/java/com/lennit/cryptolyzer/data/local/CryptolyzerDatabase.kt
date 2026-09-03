package com.lennit.cryptolyzer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lennit.cryptolyzer.core.memory.AgentMemoryDao
import com.lennit.cryptolyzer.core.memory.AgentMemoryEntity

@Database(
    entities = [DomainEventEntity::class, AgentMemoryEntity::class],
    version = 2,
    exportSchema = true
)
abstract class CryptolyzerDatabase : RoomDatabase() {
    abstract fun domainEventDao(): DomainEventDao
    abstract fun agentMemoryDao(): AgentMemoryDao
}
