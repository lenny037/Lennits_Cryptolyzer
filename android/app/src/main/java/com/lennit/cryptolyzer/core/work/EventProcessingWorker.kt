package com.lennit.cryptolyzer.core.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lennit.cryptolyzer.core.events.DomainEventProcessor
import com.lennit.cryptolyzer.data.local.DatabaseProvider

class EventProcessingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val dao = DatabaseProvider.get(applicationContext).domainEventDao()
        // No event type is acknowledged here until a concrete handler is wired.
        // This worker currently acts as the durable scheduling boundary.
        DomainEventProcessor(dao, emptyMap()).processBatch(BATCH_SIZE)
        return Result.success()
    }

    companion object {
        const val UNIQUE_NAME = "cryptolyzer-event-processing"
        const val BATCH_SIZE = 50
    }
}
