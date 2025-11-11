package com.example.voidlauncher

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * WorkManager worker for performing daily usage statistics reset
 */
class DailyResetWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        UsageTrackingManager.checkAndPerformDailyReset(applicationContext)
        return Result.success()
    }
}
