package com.lennit.cryptolyzer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DomainEventEntity::class], version = 1, exportSchema = true)
abstract class CryptolyzerDatabase : RoomDatabase() {
    abstract fun domainEventDao(): DomainEventDao
}
