package com.davidstudioz.david.workers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.davidstudioz.david.models.ModelManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background worker for downloading AI models
 * Runs on background thread with proper network checks
 * 
 * FIXED: Added network availability check
 * FIXED: Better error handling
 */
class ModelDownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val modelManager = ModelManager(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "ModelDownloadWorker started")
            
            // Check network availability first
            if (!isNetworkAvailable(context)) {
                Log.e(TAG, "No network available")
                return@withContext Result.failure(
                    workDataOf(
                        "error" to "No internet connection",
                        "error_code" to "NETWORK_UNAVAILABLE"
                    )
                )
            }

            // Check if WiFi is available for large downloads
            if (!isWifiConnected(context)) {
                Log.w(TAG, "Not on WiFi - may use mobile data")
            }

            // Get recommended model
            val recommendedModel = modelManager.getRecommendedLLM()
            if (recommendedModel == null) {
                Log.e(TAG, "No suitable model found for device")
                return@withContext Result.failure(
                    workDataOf(
                        "error" to "No suitable AI model for your device",
                        "error_code" to "NO_MODEL"
                    )
                )
            }

            Log.d(TAG, "Downloading model: ${recommendedModel.name}")

            // Download with progress
            var lastProgress = 0
            val downloadResult = modelManager.downloadModel(recommendedModel) { progress ->
                if (progress - lastProgress >= 10) {
                    Log.d(TAG, "Download progress: $progress%")
                    lastProgress = progress
                    // Update progress in WorkManager
                    setProgressAsync(
                        workDataOf(
                            "progress" to progress,
                            "model_name" to recommendedModel.name
                        )
                    )
                }
            }

            downloadResult.fold(
                onSuccess = { modelPath ->
                    Log.d(TAG, "Model downloaded successfully: $modelPath")
                    
                    // Save download info
                    val prefs = context.getSharedPreferences("david_prefs", Context.MODE_PRIVATE)
                    prefs.edit().apply {
                        putBoolean("model_downloaded", true)
                        putString("downloaded_model", recommendedModel.name)
                        putString("model_path", modelPath)
                        putLong("download_timestamp", System.currentTimeMillis())
                        apply()
                    }
                    
                    Result.success(
                        workDataOf(
                            "model_path" to modelPath,
                            "model_name" to recommendedModel.name,
                            "success" to true
                        )
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "Model download failed", error)
                    Result.failure(
                        workDataOf(
                            "error" to (error.message ?: "Download failed"),
                            "error_code" to "DOWNLOAD_FAILED"
                        )
                    )
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Worker exception", e)
            Result.failure(
                workDataOf(
                    "error" to (e.message ?: "Unknown error"),
                    "error_code" to "EXCEPTION"
                )
            )
        }
    }

    /**
     * Check if network is available
     */
    private fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
                as? ConnectivityManager ?: return false
            
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network", e)
            false
        }
    }

    /**
     * Check if connected to WiFi
     */
    private fun isWifiConnected(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
                as? ConnectivityManager ?: return false
            
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking WiFi", e)
            false
        }
    }

    companion object {
        private const val TAG = "ModelDownloadWorker"
        const val WORK_NAME = "model_download"
    }
}
