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
import com.davidstudioz.david.SafeMainActivity

/**
 * Model Download Activity
 * Simple standalone version without Hilt
 * Shows simulated download progress
 */
class ModelDownloadActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "ModelDownloadActivity started")
            
            // Check if model already downloaded
            val prefs = getSharedPreferences("david_prefs", MODE_PRIVATE)
            val modelDownloaded = prefs.getBoolean("model_downloaded", false)

            if (modelDownloaded) {
                Log.d(TAG, "Model already downloaded, navigating to main")
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
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            // Skip to main if error
            navigateToMain()
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

        // Simulate download
        LaunchedEffect(Unit) {
            val steps = listOf(
                0.2f to "Connecting to server...",
                0.4f to "Downloading D.A.V.I.D AI 2B...",
                0.6f to "Downloading voice models...",
                0.8f to "Optimizing for your device...",
                1.0f to "Download complete!"
            )

            steps.forEach { (target, status) ->
                downloadStatus = status
                val start = downloadProgress
                val duration = 800L
                val startTime = System.currentTimeMillis()

                while (downloadProgress < target) {
                    val elapsed = System.currentTimeMillis() - startTime
                    downloadProgress = (start + (target - start) * (elapsed.toFloat() / duration))
                        .coerceAtMost(target)
                    kotlinx.coroutines.delay(16)
                }
                kotlinx.coroutines.delay(200)
            }

            isComplete = true
            val prefs = getSharedPreferences("david_prefs", MODE_PRIVATE)
            prefs.edit().putBoolean("model_downloaded", true).apply()
            kotlinx.coroutines.delay(1000)
            navigateToMain()
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

                    CircularProgressIndicator(
                        progress = downloadProgress,
                        modifier = Modifier.size(160.dp),
                        color = Color(0xFF00E5FF),
                        strokeWidth = 8.dp,
                        trackColor = Color(0xFF1E293B)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isComplete) {
                            Text(
                                text = "✔",
                                fontSize = 36.sp,
                                color = Color(0xFF00FF88)
                            )
                        } else {
                            Text(
                                text = "${(downloadProgress * 100).toInt()}%",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "Setting Up Your AI",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoCard(
                        icon = "🧠",
                        title = "AI Model",
                        subtitle = "D.A.V.I.D AI 2B",
                        modifier = Modifier.weight(1f)
                    )
                    InfoCard(
                        icon = "📦",
                        title = "Size",
                        subtitle = "1.3 GB",
                        modifier = Modifier.weight(1f)
                    )
                    InfoCard(
                        icon = "⚡",
                        title = "Type",
                        subtitle = "LLM",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

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
                Text(text = icon, fontSize = 24.sp)
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
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    private fun navigateToMain() {
        try {
            Log.d(TAG, "Navigating to SafeMainActivity")
            val intent = Intent(this, SafeMainActivity::class.java)
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
