package com.davidstudioz.david.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.hilt.work.HiltWorker
import com.davidstudioz.david.health.HealthTracker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class HealthTrackingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val healthTracker: HealthTracker
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            healthTracker.track()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
