package com.lennit.cryptolyzer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DomainEventDao {
    @Insert
    suspend fun insert(event: DomainEventEntity)

    @Query("SELECT * FROM domain_events ORDER BY createdAtEpochMs DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<DomainEventEntity>>

    @Query("UPDATE domain_events SET processed = 1 WHERE id = :id")
    suspend fun markProcessed(id: String)
}
