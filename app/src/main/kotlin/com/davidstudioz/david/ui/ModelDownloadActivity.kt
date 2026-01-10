package com.davidstudioz.david.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.davidstudioz.david.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Model Download Activity
 * Shows AI model download progress with beautiful Jarvis-style UI
 */
class ModelDownloadActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if model already downloaded
        val prefs = getSharedPreferences("david_prefs", MODE_PRIVATE)
        val modelDownloaded = prefs.getBoolean("model_downloaded", false)

        if (modelDownloaded) {
            navigateToMain()
            return
        }

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF00E5FF),
                    secondary = Color(0xFF9CA3AF),
                    background = Color(0xFF0A0E27)
                )
            ) {
                ModelDownloadScreen()
            }
        }
    }

    @Composable
    private fun ModelDownloadScreen() {
        var downloadProgress by remember { mutableStateOf(0f) }
        var downloadStatus by remember { mutableStateOf("Preparing download...") }
        var isComplete by remember { mutableStateOf(false) }

        // Animated progress
        val infiniteTransition = rememberInfiniteTransition(label = "download_anim")
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow"
        )

        LaunchedEffect(Unit) {
            try {
                // Simulate model download with realistic progress
                val steps = listOf(
                    0.1f to "Connecting to server...",
                    0.2f to "Checking device compatibility...",
                    0.3f to "Downloading AI model (Gemma 2B)...",
                    0.5f to "Downloading voice models...",
                    0.7f to "Downloading language data...",
                    0.85f to "Optimizing for your device...",
                    0.95f to "Finalizing installation...",
                    1.0f to "Download complete!"
                )

                steps.forEach { (targetProgress, status) ->
                    downloadStatus = status
                    val startProgress = downloadProgress
                    val duration = 1000L
                    val startTime = System.currentTimeMillis()

                    while (downloadProgress < targetProgress) {
                        val elapsed = System.currentTimeMillis() - startTime
                        downloadProgress = (startProgress + (targetProgress - startProgress) * 
                                          (elapsed.toFloat() / duration)).coerceAtMost(targetProgress)
                        delay(16)
                    }
                    delay(300)
                }

                isComplete = true

                // Save model downloaded flag
                val prefs = getSharedPreferences("david_prefs", MODE_PRIVATE)
                prefs.edit().putBoolean("model_downloaded", true).apply()

                delay(1000)
                navigateToMain()
            } catch (e: Exception) {
                Log.e(TAG, "Download error", e)
                downloadStatus = "Error: ${e.message}"
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0E27),
                            Color(0xFF1A1F3A),
                            Color(0xFF0A0E27)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                // Animated Circle
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer glow
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF00E5FF).copy(alpha = glowAlpha * 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Progress circle
                    CircularProgressIndicator(
                        progress = downloadProgress,
                        modifier = Modifier.size(160.dp),
                        color = Color(0xFF00E5FF),
                        strokeWidth = 8.dp,
                        trackColor = Color(0xFF1E293B)
                    )

                    // Percentage text
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(downloadProgress * 100).toInt()}%",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                        if (isComplete) {
                            Text(
                                text = "✔",
                                fontSize = 24.sp,
                                color = Color(0xFF00FF88)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Title
                Text(
                    text = "Setting Up Your AI",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Status message
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E88E5).copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    Text(
                        text = downloadStatus,
                        fontSize = 14.sp,
                        color = Color(0xFF64B5F6),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Info cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoCard(
                        icon = "🧠",
                        title = "AI Model",
                        subtitle = "Gemma 2B",
                        modifier = Modifier.weight(1f)
                    )
                    InfoCard(
                        icon = "📦",
                        title = "Size",
                        subtitle = "~2.8 GB",
                        modifier = Modifier.weight(1f)
                    )
                    InfoCard(
                        icon = "⚡",
                        title = "Speed",
                        subtitle = "Fast",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Skip button (for testing)
                TextButton(
                    onClick = {
                        val prefs = getSharedPreferences("david_prefs", MODE_PRIVATE)
                        prefs.edit().putBoolean("model_downloaded", true).apply()
                        navigateToMain()
                    }
                ) {
                    Text(
                        text = "Skip for now",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }
    }

    @Composable
    private fun InfoCard(
        icon: String,
        title: String,
        subtitle: String,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E88E5).copy(alpha = 0.1f))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = icon,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    fontSize = 10.sp,
                    color = Color(0xFF9CA3AF),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    private fun navigateToMain() {
        try {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Navigation error", e)
        }
    }

    companion object {
        private const val TAG = "ModelDownloadActivity"
    }
}
