package com.davidstudioz.david.ui

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidstudioz.david.SafeMainActivity
import com.davidstudioz.david.models.ModelManager
import com.davidstudioz.david.models.AIModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

/**
 * D.A.V.I.D Model Download - REAL DOWNLOADS FROM HUGGINGFACE/MEDIAPIPE
 * ✅ Uses ModelManager.downloadModel() for actual HTTP downloads
 * ✅ Downloads from real URLs: HuggingFace, Google MediaPipe, ONNX
 * ✅ Detects device RAM capacity
 * ✅ Downloads optimal models for device (Mini/Light/Standard/Pro)
 * ✅ Progress tracking with real download speeds
 * ✅ Model IDs match ModelManager methods
 */
class ModelDownloadActivity : ComponentActivity() {

    private lateinit var modelManager: ModelManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "ModelDownloadActivity started")
            
            modelManager = ModelManager(this)
            
            val prefs = getSharedPreferences("david_prefs", MODE_PRIVATE)
            val modelDownloaded = prefs.getBoolean("model_downloaded", false)

            if (modelDownloaded) {
                Log.d(TAG, "Models already downloaded")
                navigateToMain()
                return
            }

            setContent {
                MaterialTheme(
                    colorScheme = darkColorScheme(
                        primary = Color(0xFF00E5FF),
                        background = Color(0xFF0A0E27)
                    )
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        DownloadScreen()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            finish()
        }
    }

    /**
     * Get optimal AIModels from ModelManager based on device RAM
     * ✅ Returns REAL AIModel objects with download URLs
     */
    private fun getOptimalModelsFromManager(): List<AIModel> {
        return modelManager.getEssentialModels()
    }

    @Composable
    private fun DownloadScreen() {
        var downloadProgress by remember { mutableStateOf(0f) }
        var downloadStatus by remember { mutableStateOf("Preparing...") }
        var isComplete by remember { mutableStateOf(false) }
        var hasError by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }
        var currentModelIndex by remember { mutableStateOf(0) }
        var currentModelProgress by remember { mutableStateOf(0) }
        var isDownloading by remember { mutableStateOf(false) }
        var downloadedModels by remember { mutableStateOf(setOf<Int>()) }
        var totalDownloadedMB by remember { mutableStateOf(0f) }
        var downloadSpeed by remember { mutableStateOf("") }
        var deviceRam by remember { mutableStateOf(0) }
        var deviceTier by remember { mutableStateOf("") }

        // Get optimal models for this device from ModelManager
        val models = remember { getOptimalModelsFromManager() }
        val totalSize = remember { 
            models.sumOf { 
                parseSizeMB(it.size).toDouble() 
            }.toFloat() 
        }

        val infiniteTransition = rememberInfiniteTransition(label = "download")
        
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow"
        )

        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        val rotationAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )

        LaunchedEffect(Unit) {
            if (!isDownloading && !isComplete) {
                isDownloading = true
                
                try {
                    // Detect device capacity
                    deviceRam = modelManager.getDeviceRamGB()
                    deviceTier = when {
                        deviceRam >= 4 -> "Pro"
                        deviceRam >= 3 -> "Standard"
                        deviceRam >= 2 -> "Light"
                        else -> "Mini"
                    }
                    
                    downloadStatus = "Detected ${deviceRam}GB RAM - Optimizing for $deviceTier tier..."
                    Log.d(TAG, "Device: ${deviceRam}GB RAM, Tier: $deviceTier, Models: ${models.size}")
                    Log.d(TAG, "Models to download: ${models.map { it.name }}")
                    delay(2000)
                    
                    models.forEachIndexed { index, aiModel ->
                        currentModelIndex = index
                        downloadStatus = "Downloading ${aiModel.name}..."
                        Log.d(TAG, "Starting download: ${aiModel.name} from ${aiModel.url}")

                        // ✅ REAL DOWNLOAD using ModelManager
                        val downloadResult = modelManager.downloadModel(
                            model = aiModel,
                            onProgress = { progress ->
                                // Update UI with real download progress
                                currentModelProgress = progress.progress
                                totalDownloadedMB = downloadedModels.sumOf { 
                                    parseSizeMB(models[it].size).toDouble() 
                                }.toFloat() + progress.downloadedMB
                                downloadProgress = totalDownloadedMB / totalSize
                                
                                val speed = if (progress.downloadedMB > 0) {
                                    "${String.format("%.1f", progress.downloadedMB)} / ${String.format("%.1f", progress.totalMB)} MB"
                                } else {
                                    "Connecting..."
                                }
                                downloadSpeed = speed
                                
                                downloadStatus = "${aiModel.name}... ${progress.progress}%"
                                
                                Log.d(TAG, "Progress: ${aiModel.name} - ${progress.progress}% (${progress.downloadedMB}/${progress.totalMB} MB)")
                            }
                        )

                        // Check if download succeeded
                        if (downloadResult.isSuccess) {
                            currentModelProgress = 100
                            downloadedModels = downloadedModels + index
                            val file = downloadResult.getOrNull()
                            Log.d(TAG, "✅ Downloaded: ${aiModel.name} to ${file?.absolutePath}")
                            delay(500)
                        } else {
                            val error = downloadResult.exceptionOrNull()
                            Log.e(TAG, "❌ Download failed: ${aiModel.name}", error)
                            throw error ?: Exception("Download failed for ${aiModel.name}")
                        }
                    }

                    downloadProgress = 1f
                    downloadStatus = "All D.A.V.I.D $deviceTier models ready!"
                    isComplete = true
                    
                    val prefs = getSharedPreferences("david_prefs", MODE_PRIVATE)
                    prefs.edit().apply {
                        putBoolean("model_downloaded", true)
                        putLong("download_timestamp", System.currentTimeMillis())
                        putFloat("total_size_mb", totalSize)
                        putInt("model_count", models.size)
                        putInt("device_ram_gb", deviceRam)
                        putString("device_tier", deviceTier)
                        apply()
                    }
                    
                    Log.d(TAG, "All ${models.size} D.A.V.I.D $deviceTier models downloaded! Total: ${totalSize.toInt()} MB")
                    delay(2000)
                    navigateToMain()
                    
                } catch (e: Exception) {
                    hasError = true
                    errorMessage = "Download failed: ${e.message}"
                    Log.e(TAG, "Download error", e)
                    isDownloading = false
                }
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
                )
        ) {
            if (isDownloading && !isComplete) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.1f),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .rotate(rotationAngle)
                    ) {
                        repeat(6) { index ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .offset(
                                        x = (150 * kotlin.math.cos(index * 60.0 * Math.PI / 180)).dp,
                                        y = (150 * kotlin.math.sin(index * 60.0 * Math.PI / 180)).dp
                                    )
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E5FF))
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "D.A.V.I.D Setup",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00E5FF),
                    letterSpacing = 4.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (deviceRam > 0) {
                    Text(
                        text = "${deviceRam}GB RAM • $deviceTier Tier Models",
                        fontSize = 12.sp,
                        color = Color(0xFF00FF88),
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = "Downloading from HuggingFace & MediaPipe",
                    fontSize = 14.sp,
                    color = Color(0xFF64B5F6),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(if (isComplete) 1.1f else pulseScale),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .alpha(if (isDownloading) glowAlpha * 0.4f else 0.3f)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF00E5FF).copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    CircularProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier.size(150.dp),
                        color = if (hasError) Color(0xFFFF6E40) else Color(0xFF00E5FF),
                        strokeWidth = 10.dp,
                        trackColor = Color(0xFF1E293B)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (hasError) {
                            Text(text = "⚠️", fontSize = 40.sp)
                        } else if (isComplete) {
                            Text(text = "✅", fontSize = 40.sp)
                        } else {
                            Text(
                                text = "${(downloadProgress * 100).toInt()}%",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                            Text(
                                text = downloadSpeed,
                                fontSize = 10.sp,
                                color = Color(0xFF64B5F6)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E88E5).copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (hasError) "Download Failed" else downloadStatus,
                            fontSize = 14.sp,
                            color = if (hasError) Color(0xFFFF6E40) else Color(0xFF64B5F6),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (isDownloading && !hasError) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${totalDownloadedMB.toInt()} / ${totalSize.toInt()} MB • ${models.size} models",
                                fontSize = 12.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }

                        if (hasError) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = errorMessage,
                                fontSize = 11.sp,
                                color = Color(0xFF9CA3AF),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1F3A).copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(models) { index, model ->
                            ModelItem(
                                model = model,
                                isDownloading = index == currentModelIndex && isDownloading,
                                isDownloaded = downloadedModels.contains(index),
                                progress = if (index == currentModelIndex) currentModelProgress else 0
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = hasError,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Button(
                        onClick = {
                            hasError = false
                            downloadProgress = 0f
                            currentModelIndex = 0
                            downloadedModels = emptySet()
                            isDownloading = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF)
                        ),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = "🔄 Retry Download",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                Text(
                    text = if (isComplete) {
                        "Setup complete! Launching D.A.V.I.D..."
                    } else {
                        "Real downloads: Whisper, Phi-2, TinyLlama, ResNet50, MediaPipe"
                    },
                    fontSize = 10.sp,
                    color = Color(0xFF4B5563),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }

    @Composable
    private fun ModelItem(
        model: AIModel,
        isDownloading: Boolean,
        isDownloaded: Boolean,
        progress: Int
    ) {
        val backgroundColor = when {
            isDownloaded -> Color(0xFF00FF88).copy(alpha = 0.1f)
            isDownloading -> Color(0xFF00E5FF).copy(alpha = 0.2f)
            else -> Color(0xFF1E293B).copy(alpha = 0.5f)
        }

        val borderColor = when {
            isDownloaded -> Color(0xFF00FF88)
            isDownloading -> Color(0xFF00E5FF)
            else -> Color.Transparent
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (borderColor != Color.Transparent)
                        Modifier.border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    else Modifier
                ),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = when (model.type) {
                            "Speech" -> "🎤"
                            "LLM" -> "💬"
                            "Vision" -> "👁️"
                            "Language" -> "🌐"
                            "Gesture" -> if (model.name.contains("Hand")) "✋" else "👆"
                            else -> "🤖"
                        },
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = model.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDownloaded) Color(0xFF00FF88) 
                                       else if (isDownloading) Color(0xFF00E5FF)
                                       else Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = model.description,
                            fontSize = 9.sp,
                            color = Color(0xFF9CA3AF),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isDownloading) {
                            Text(
                                text = "$progress% • ${model.size}",
                                fontSize = 8.sp,
                                color = Color(0xFF64B5F6)
                            )
                        } else {
                            Text(
                                text = "${model.size} • ${model.format}",
                                fontSize = 8.sp,
                                color = Color(0xFF64B5F6)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isDownloaded -> Color(0xFF00FF88).copy(alpha = 0.2f)
                                isDownloading -> Color(0xFF00E5FF).copy(alpha = 0.2f)
                                else -> Color(0xFF1E293B)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isDownloaded -> Text("✓", fontSize = 16.sp, color = Color(0xFF00FF88))
                        isDownloading -> CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF00E5FF),
                            strokeWidth = 2.dp
                        )
                        else -> Text("○", fontSize = 16.sp, color = Color(0xFF4B5563))
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        try {
            Log.d(TAG, "Navigating to main app")
            val intent = Intent(this, SafeMainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Navigation error", e)
        }
    }

    /**
     * Parse size string to MB
     */
    private fun parseSizeMB(size: String): Float {
        val num = size.replace("[^0-9.]".toRegex(), "").toFloatOrNull() ?: 0f
        return when {
            size.contains("GB", ignoreCase = true) -> num * 1024f
            size.contains("MB", ignoreCase = true) -> num
            else -> num
        }
    }

    companion object {
        private const val TAG = "ModelDownloadActivity"
    }
}
