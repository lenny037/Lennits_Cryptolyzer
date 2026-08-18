package com.lennit.cryptolyzer

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.lennit.cryptolyzer.core.work.HeartbeatWorker
import java.util.concurrent.TimeUnit

class CryptolyzerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val request = PeriodicWorkRequestBuilder<HeartbeatWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "cryptolyzer-heartbeat",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
