package com.lennit.cryptolyzer.core.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lennit.cryptolyzer.data.local.DatabaseProvider

class HeartbeatWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val db = DatabaseProvider.get(applicationContext)
        db.domainEventDao().insert(
            com.lennit.cryptolyzer.data.local.DomainEventEntity(
                id = "heartbeat-${System.currentTimeMillis()}",
                type = "system.heartbeat",
                aggregateId = null,
                payloadJson = "{}",
                createdAtEpochMs = System.currentTimeMillis()
            )
        )
        return Result.success()
    }
}
