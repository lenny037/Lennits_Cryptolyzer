package com.lennit.cryptolyzer

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.lennit.cryptolyzer.core.work.EventProcessingWorker
import com.lennit.cryptolyzer.core.work.HeartbeatWorker
import java.util.concurrent.TimeUnit

class CryptolyzerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val workManager = WorkManager.getInstance(this)

        workManager.enqueueUniquePeriodicWork(
            "cryptolyzer-heartbeat",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<HeartbeatWorker>(15, TimeUnit.MINUTES).build()
        )

        val processingConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        workManager.enqueueUniquePeriodicWork(
            EventProcessingWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<EventProcessingWorker>(15, TimeUnit.MINUTES)
                .setConstraints(processingConstraints)
                .build()
        )
    }
}
