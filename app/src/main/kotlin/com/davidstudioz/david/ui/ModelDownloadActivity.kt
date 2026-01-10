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
import com.davidstudioz.david.models.AIModel
import com.davidstudioz.david.models.ModelManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Model Download Activity
 * Shows AI model download progress with real downloads from Hugging Face
 * Uses ModelManager to download actual AI models
 */
@AndroidEntryPoint
class ModelDownloadActivity : ComponentActivity() {

    @Inject
    lateinit var modelManager: ModelManager

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
        var hasError by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }
        var selectedModel by remember { mutableStateOf<AIModel?>(null) }
        var isDownloading by remember { mutableStateOf(false) }

        // Get recommended model
        LaunchedEffect(Unit) {
            try {
                val recommendedLLM = modelManager.getRecommendedLLM()
                selectedModel = recommendedLLM
                Log.d(TAG, "Recommended model: ${recommendedLLM?.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Error getting recommended model", e)
                selectedModel = null
            }
        }

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
                        color = if (hasError) Color(0xFFFF6E40) else Color(0xFF00E5FF),
                        strokeWidth = 8.dp,
                        trackColor = Color(0xFF1E293B)
                    )

                    // Percentage text
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (hasError) {
                            Text(
                                text = "⚠️",
                                fontSize = 36.sp
                            )
                        } else if (isComplete) {
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

                // Title
                Text(
                    text = if (hasError) "Download Failed" else "Setting Up Your AI",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasError) Color(0xFFFF6E40) else Color.White
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
                        text = if (hasError) errorMessage else downloadStatus,
                        fontSize = 14.sp,
                        color = if (hasError) Color(0xFFFF6E40) else Color(0xFF64B5F6),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Model info cards
                selectedModel?.let { model ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InfoCard(
                            icon = "🧠",
                            title = "AI Model",
                            subtitle = model.name,
                            modifier = Modifier.weight(1f)
                        )
                        InfoCard(
                            icon = "📦",
                            title = "Size",
                            subtitle = model.size,
                            modifier = Modifier.weight(1f)
                        )
                        InfoCard(
                            icon = "⚡",
                            title = "Type",
                            subtitle = model.type.uppercase(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Download button
                    if (!isDownloading && !isComplete) {
                        Button(
                            onClick = {
                                selectedModel?.let { model ->
                                    isDownloading = true
                                    hasError = false
                                    lifecycleScope.launch {
                                        try {
                                            downloadStatus = "Connecting to Hugging Face..."
                                            delay(500)
                                            
                                            downloadStatus = "Downloading ${model.name}..."
                                            
                                            val result = modelManager.downloadModel(model) { progress ->
                                                downloadProgress = progress / 100f
                                                downloadStatus = "Downloading ${model.name}... ${progress}%"
                                            }
                                            
                                            result.onSuccess { path ->
                                                downloadProgress = 1f
                                                downloadStatus = "Download complete!"
                                                isComplete = true
                                                
                                                // Save download state
                                                val prefs = getSharedPreferences("david_prefs", MODE_PRIVATE)
                                                prefs.edit().apply {
                                                    putBoolean("model_downloaded", true)
                                                    putString("downloaded_model", model.name)
                                                    putString("model_path", path)
                                                    apply()
                                                }
                                                
                                                delay(1500)
                                                navigateToMain()
                                            }
                                            
                                            result.onFailure { error ->
                                                hasError = true
                                                errorMessage = "Download failed: ${error.message}"
                                                isDownloading = false
                                                Log.e(TAG, "Download error", error)
                                            }
                                            
                                        } catch (e: Exception) {
                                            hasError = true
                                            errorMessage = "Error: ${e.message}"
                                            isDownloading = false
                                            Log.e(TAG, "Download exception", e)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E5FF)
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text(
                                text = "📥 Download",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                    
                    // Skip button
                    OutlinedButton(
                        onClick = {
                            val prefs = getSharedPreferences("david_prefs", MODE_PRIVATE)
                            prefs.edit().putBoolean("model_downloaded", true).apply()
                            navigateToMain()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF9CA3AF)
                        ),
                        shape = RoundedCornerShape(28.dp),
                        enabled = !isDownloading
                    ) {
                        Text(
                            text = "Skip for now",
                            fontSize = 14.sp
                        )
                    }
                }

                // Retry button if error
                if (hasError) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = {
                            hasError = false
                            downloadProgress = 0f
                        }
                    ) {
                        Text(
                            text = "🔄 Retry",
                            fontSize = 14.sp,
                            color = Color(0xFF00E5FF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info text
                Text(
                    text = if (isDownloading) {
                        "Please keep the app open while downloading"
                    } else {
                        "Download AI model for offline voice assistant"
                    },
                    fontSize = 11.sp,
                    color = Color(0xFF4B5563),
                    textAlign = TextAlign.Center
                )
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
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
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
