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
 * Runs on background thread with proper network checks and memory management
 * 
 * ALL FIXES APPLIED:
 * ✅ Network availability check
 * ✅ WiFi connection check
 * ✅ Memory availability check
 * ✅ Better error handling with error codes
 * ✅ Fixed all unresolved references
 * ✅ Fixed AIModel property access (name instead of id)
 * ✅ Fixed Result<File> handling
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

            // Check memory availability for large downloads
            if (!isMemoryAvailable()) {
                Log.e(TAG, "Insufficient memory for download")
                return@withContext Result.failure(
                    workDataOf(
                        "error" to "Low memory - please free up space",
                        "error_code" to "LOW_MEMORY"
                    )
                )
            }

            // Check if WiFi is available for large downloads
            if (!isWifiConnected(context)) {
                Log.w(TAG, "Not on WiFi - may use mobile data")
            }

            // Get essential models list
            val essentialModels = modelManager.getEssentialModels()
            if (essentialModels.isEmpty()) {
                Log.e(TAG, "No suitable models found for device")
                return@withContext Result.failure(
                    workDataOf(
                        "error" to "No suitable AI models for your device",
                        "error_code" to "NO_MODEL"
                    )
                )
            }

            // Download the first essential model (or you can download all)
            val modelToDownload = essentialModels.first()
            Log.d(TAG, "Downloading model: ${modelToDownload.name}")

            // Download with progress
            var lastProgress = 0
            val downloadResult = modelManager.downloadModel(
                model = modelToDownload,
                onProgress = { progress ->
                    if (progress - lastProgress >= 10) {
                        Log.d(TAG, "Download progress: $progress%")
                        lastProgress = progress
                        // Update progress in WorkManager
                        setProgressAsync(
                            workDataOf(
                                "progress" to progress,
                                "model_name" to modelToDownload.name
                            )
                        )
                    }
                }
            )

            // Check if download was successful
            downloadResult.fold(
                onSuccess = { modelFile ->
                    Log.d(TAG, "Model downloaded successfully: ${modelFile.absolutePath}")
                    
                    // Save download info
                    val prefs = context.getSharedPreferences("david_prefs", Context.MODE_PRIVATE)
                    prefs.edit().apply {
                        putBoolean("model_downloaded", true)
                        putString("downloaded_model", modelToDownload.name)
                        putString("model_path", modelFile.absolutePath)
                        putLong("download_timestamp", System.currentTimeMillis())
                        apply()
                    }
                    
                    return@withContext Result.success(
                        workDataOf(
                            "model_path" to modelFile.absolutePath,
                            "model_name" to modelToDownload.name,
                            "success" to true
                        )
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "Model download failed: ${error.message}")
                    return@withContext Result.failure(
                        workDataOf(
                            "error" to (error.message ?: "Download failed"),
                            "error_code" to "DOWNLOAD_FAILED"
                        )
                    )
                }
            )

            // This should never be reached due to returns above
            Result.failure(
                workDataOf(
                    "error" to "Unknown error",
                    "error_code" to "UNKNOWN"
                )
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

    /**
     * Check if sufficient memory is available for download
     * Ensures at least 200MB free memory to prevent OOM errors
     */
    private fun isMemoryAvailable(): Boolean {
        return try {
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            val maxMemory = runtime.maxMemory()
            val availableMemory = maxMemory - usedMemory
            
            val minRequiredMemory = 200 * 1024 * 1024 // 200MB minimum
            val hasEnoughMemory = availableMemory > minRequiredMemory
            
            Log.d(TAG, "Memory check: Available=${availableMemory / 1024 / 1024}MB, " +
                      "Required=${minRequiredMemory / 1024 / 1024}MB, " +
                      "Sufficient=$hasEnoughMemory")
            
            hasEnoughMemory
        } catch (e: Exception) {
            Log.e(TAG, "Error checking memory", e)
            true // Continue anyway if check fails
        }
    }

    companion object {
        private const val TAG = "ModelDownloadWorker"
        const val WORK_NAME = "model_download"
    }
}
