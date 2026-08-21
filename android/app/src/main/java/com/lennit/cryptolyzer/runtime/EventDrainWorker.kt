package com.lennit.cryptolyzer.runtime

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class EventDrainWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val app = applicationContext as CryptolyzerApplication
        if (!app.runtimeLifecycle.permitsDomainWork()) return Result.retry()

        // Handler registration is intentionally injected at the runtime-composition
        // boundary. The worker itself owns scheduling semantics, not domain policy.
        return Result.success()
    }
}
