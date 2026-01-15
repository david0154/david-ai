package com.davidstudioz.david.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.hilt.work.HiltWorker
import com.davidstudioz.david.chat.ChatManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class HistoryCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val chatManager: ChatManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            chatManager.clearHistory()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
