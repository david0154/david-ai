package com.davidstudioz.david.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.hilt.work.HiltWorker
import com.davidstudioz.david.models.ModelManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ModelDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val modelManager: ModelManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val modelUrl = inputData.getString("model_url") ?: return Result.failure()
        return try {
            modelManager.downloadModel(modelUrl)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
