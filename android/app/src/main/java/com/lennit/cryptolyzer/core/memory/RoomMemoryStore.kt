package com.lennit.cryptolyzer.core.memory

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.lennit.cryptolyzer.core.agents.m16.MemoryStore

@Entity(tableName = "agent_memory", primaryKeys = ["namespace", "key"])
data class AgentMemoryEntity(
    val namespace: String,
    val key: String,
    val valueJson: String,
    val updatedAtEpochMs: Long
)

@Dao
interface AgentMemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(value: AgentMemoryEntity)

    @Query("SELECT valueJson FROM agent_memory WHERE namespace = :namespace AND `key` = :key")
    suspend fun get(namespace: String, key: String): String?
}

class RoomMemoryStore(private val dao: AgentMemoryDao) : MemoryStore {
    override suspend fun put(namespace: String, key: String, valueJson: String) {
        dao.upsert(AgentMemoryEntity(namespace, key, valueJson, System.currentTimeMillis()))
    }

    override suspend fun get(namespace: String, key: String): String? = dao.get(namespace, key)
}
