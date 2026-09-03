package com.lennit.cryptolyzer.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile private var instance: CryptolyzerDatabase? = null

    fun get(context: Context): CryptolyzerDatabase =
        instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                CryptolyzerDatabase::class.java,
                "cryptolyzer.db"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
                .also { instance = it }
        }
}
