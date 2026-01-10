package com.davidstudioz.david.ui

import android.content.Intent
import android.os.Bundle
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
 * - Downloads AI models in background
 * - Shows progress
 */
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start model download worker
        startModelDownloadWorker()

        setContent {
            SplashScreen()
        }
    }

    @Composable
    private fun SplashScreen() {
        var progress by remember { mutableStateOf(0f) }
        var statusText by remember { mutableStateOf("Initializing DAVID AI...") }

        LaunchedEffect(Unit) {
            // Simulate initialization
            for (i in 0..100) {
                progress = i / 100f
                when (i) {
                    20 -> statusText = "Checking device capabilities..."
                    40 -> statusText = "Downloading AI models..."
                    60 -> statusText = "Setting up voice recognition..."
                    80 -> statusText = "Finalizing setup..."
                    100 -> statusText = "Ready!"
                }
                delay(30)
            }
            
            // Navigate to MainActivity
            delay(500)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1F2937)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
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
            }
        }
    }

    /**
     * Start background worker to download AI models
     */
    private fun startModelDownloadWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val downloadWork = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            "model_download",
            ExistingWorkPolicy.KEEP,
            downloadWork
        )
    }
}
