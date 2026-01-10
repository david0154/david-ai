package com.davidstudioz.david.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.davidstudioz.david.MainActivity
import com.davidstudioz.david.workers.ModelDownloadWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Splash Activity - First screen shown
 * - Shows logo and app name
 * - Downloads AI models in background (with proper error handling)
 * - Shows progress
 * - Prevents crash if model download fails
 */
class SplashActivity : ComponentActivity() {

    private var splashMinDuration = 2000L // Minimum 2 second splash screen
    private var splashStartTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        splashStartTime = System.currentTimeMillis()

        try {
            // Start model download worker (non-blocking)
            startModelDownloadWorker()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting model download", e)
            // Continue without crashing
        }

        try {
            setContent {
                SplashScreen()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting content", e)
            navigateToMain()
        }
    }

    @Composable
    private fun SplashScreen() {
        var progress by remember { mutableStateOf(0f) }
        var statusText by remember { mutableStateOf("Initializing DAVID AI...") }
        var hasError by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            try {
                // Simulate initialization with proper error handling
                for (i in 0..100) {
                    try {
                        progress = i / 100f
                        when (i) {
                            20 -> statusText = "Checking device capabilities..."
                            40 -> statusText = "Preparing AI models..."
                            60 -> statusText = "Setting up voice recognition..."
                            80 -> statusText = "Finalizing setup..."
                            100 -> statusText = "Ready!"
                        }
                        delay(30)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during initialization step", e)
                        // Continue with next iteration
                    }
                }

                // Wait for minimum splash duration
                val elapsed = System.currentTimeMillis() - splashStartTime
                val remaining = splashMinDuration - elapsed
                if (remaining > 0) {
                    delay(remaining)
                }

                // Navigate to MainActivity
                navigateToMain()
            } catch (e: Exception) {
                Log.e(TAG, "Fatal error in splash screen", e)
                hasError = true
                errorMessage = e.localizedMessage ?: "Unknown error"
                // Try to continue after delay
                delay(2000)
                navigateToMain()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1F2937)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                if (!hasError) {
                    // Normal splash screen
                    Text(
                        text = "🤖",
                        fontSize = 80.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "DAVID AI",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00D4FF)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Voice-First AI Assistant",
                        fontSize = 14.sp,
                        color = Color(0xFF9CA3AF)
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    // Progress bar
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .width(250.dp)
                            .height(4.dp),
                        color = Color(0xFF00D4FF),
                        trackColor = Color(0xFF374151)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                } else {
                    // Error splash screen
                    Text(
                        text = "⚠️",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Initialization Error",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6E40)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.padding(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Continuing to app...",
                        fontSize = 11.sp,
                        color = Color(0xFF64B5F6)
                    )
                }
            }
        }
    }

    /**
     * Start background worker to download AI models
     * This is non-blocking and won't crash the app if it fails
     */
    private fun startModelDownloadWorker() {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(false) // Don't require good battery
                .setRequiresDeviceIdle(false)    // Can run even if device is in use
                .build()

            val downloadWork = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            try {
                WorkManager.getInstance(this).enqueueUniqueWork(
                    "model_download",
                    ExistingWorkPolicy.KEEP, // Don't requeue if already exists
                    downloadWork
                )
                Log.d(TAG, "Model download worker enqueued")
            } catch (e: Exception) {
                Log.e(TAG, "Error enqueuing work", e)
                // Don't crash if WorkManager fails
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting model download worker", e)
            // Gracefully handle error - app can still work without background downloads
        }
    }

    /**
     * Navigate to main activity safely
     */
    private fun navigateToMain() {
        try {
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to MainActivity", e)
            // If even this fails, still try to finish
            try {
                finish()
            } catch (ex: Exception) {
                Log.e(TAG, "Error finishing activity", ex)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            // Clean up if needed
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
    }

    companion object {
        private const val TAG = "SplashActivity"
    }
}
