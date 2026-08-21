package com.lennit.cryptolyzer.core.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lennit.cryptolyzer.core.events.CoreEventHandlers
import com.lennit.cryptolyzer.core.events.DomainEventProcessor
import com.lennit.cryptolyzer.data.local.DatabaseProvider

class EventProcessingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val dao = DatabaseProvider.get(applicationContext).domainEventDao()
        return runCatching {
            DomainEventProcessor(dao, CoreEventHandlers.create()).processBatch(BATCH_SIZE)
            Result.success()
        }.getOrElse { Result.retry() }
    }

    companion object {
        const val UNIQUE_NAME = "cryptolyzer-event-processing"
        const val BATCH_SIZE = 50
    }
}
