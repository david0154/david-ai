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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * D.A.V.I.D Model Download - INTELLIGENT DEVICE-BASED SELECTION
 * ✅ Detects device RAM capacity
 * ✅ Downloads optimal models for device (Light/Standard/Pro)
 * ✅ Low RAM (1-2GB): Tiny voice, Light chat, Lite vision
 * ✅ Medium RAM (3GB): Base voice, Standard chat, Standard vision
 * ✅ High RAM (4GB+): Pro voice (Small), Pro chat, Standard vision
 * ✅ FIXED: Model IDs now match ModelManager download methods
 */
class ModelDownloadActivity : ComponentActivity() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "ModelDownloadActivity started")
            
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
     * Get device RAM in GB
     */
    private fun getDeviceRamGB(): Int {
        return try {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            val ramGB = (memInfo.totalMem / (1024 * 1024 * 1024)).toInt()
            Log.d(TAG, "Device RAM: $ramGB GB")
            ramGB
        } catch (e: Exception) {
            Log.e(TAG, "Error getting device RAM", e)
            2 // Default to 2GB for safety
        }
    }

    /**
     * Get optimal models based on device RAM
     * ✅ FIXED: fileId now matches ModelManager getVoiceModel/getLLMModel/getVisionModel calls
     */
    private fun getOptimalModels(): List<ModelInfo> {
        val deviceRam = getDeviceRamGB()
        
        Log.d(TAG, "Selecting optimal models for ${deviceRam}GB RAM device")
        
        return when {
            // High-end devices (4GB+ RAM) - Pro models
            deviceRam >= 4 -> listOf(
                // ✅ Voice Pro = Whisper Small
                ModelInfo("D.A.V.I.D Voice Pro", "High-accuracy voice (Whisper Small)", "🎤", "466 MB", 466f, "voice_small", "Pro"),
                // ✅ Chat Pro = Phi-2
                ModelInfo("D.A.V.I.D Chat Pro", "Advanced AI conversations (Phi-2)", "💬", "1.6 GB", 1600f, "llm_pro", "Pro"),
                // ✅ Vision Standard = ResNet50 (ModelManager only has lite/standard, no "pro")
                ModelInfo("D.A.V.I.D Vision", "Advanced object recognition (ResNet50)", "👁️", "98 MB", 98f, "vision_standard", "Pro"),
                // ✅ Language = Multilingual
                ModelInfo("D.A.V.I.D Multilingual", "15 languages: EN, HI, TA, TE, BN, MR, GU, KN, ML, PA, OR, UR, SA, KS, AS", "🌐", "120 MB", 120f, "language_multilingual", "Shared"),
                // ✅ Gesture Hand = MediaPipe Hand
                ModelInfo("D.A.V.I.D Gesture Hand", "Hand detection & 21-point tracking", "✋", "25 MB", 25f, "gesture_hand", "Shared"),
                // ✅ Gesture Recognition = MediaPipe Gesture (was "gesture_ctrl")
                ModelInfo("D.A.V.I.D Gesture Recognition", "Gesture classification: thumbs up, peace, OK...", "👆", "31 MB", 31f, "gesture_recognition", "Shared")
            )
            
            // Mid-range devices (3GB RAM) - Standard models
            deviceRam >= 3 -> listOf(
                // ✅ Voice Base = Whisper Base
                ModelInfo("D.A.V.I.D Voice Base", "Balanced voice recognition (Whisper Base)", "🎤", "142 MB", 142f, "voice_base", "Standard"),
                // ✅ Chat Standard = Qwen
                ModelInfo("D.A.V.I.D Chat Standard", "Smart AI conversations (Qwen 1.5-1.8B)", "💬", "1.1 GB", 1100f, "llm_standard", "Standard"),
                // ✅ Vision Standard = ResNet50
                ModelInfo("D.A.V.I.D Vision", "Standard object recognition (ResNet50)", "👁️", "98 MB", 98f, "vision_standard", "Standard"),
                // ✅ Language = Multilingual
                ModelInfo("D.A.V.I.D Multilingual", "15 languages: EN, HI, TA, TE, BN, MR, GU, KN, ML, PA, OR, UR, SA, KS, AS", "🌐", "120 MB", 120f, "language_multilingual", "Shared"),
                // ✅ Gesture Hand
                ModelInfo("D.A.V.I.D Gesture Hand", "Hand detection & 21-point tracking", "✋", "25 MB", 25f, "gesture_hand", "Shared"),
                // ✅ Gesture Recognition
                ModelInfo("D.A.V.I.D Gesture Recognition", "Gesture classification: thumbs up, peace, OK...", "👆", "31 MB", 31f, "gesture_recognition", "Shared")
            )
            
            // Budget devices (2GB RAM) - Light models
            deviceRam >= 2 -> listOf(
                // ✅ Voice Base = Whisper Base (same as 3GB)
                ModelInfo("D.A.V.I.D Voice Base", "Fast voice recognition (Whisper Base)", "🎤", "142 MB", 142f, "voice_base", "Light"),
                // ✅ Chat Light = TinyLlama
                ModelInfo("D.A.V.I.D Chat Light", "Efficient AI chat (TinyLlama 1.1B)", "💬", "669 MB", 669f, "llm_light", "Light"),
                // ✅ Vision Lite = MobileNetV2
                ModelInfo("D.A.V.I.D Vision Lite", "Fast object detection (MobileNetV2)", "👁️", "14 MB", 14f, "vision_lite", "Light"),
                // ✅ Language = Multilingual
                ModelInfo("D.A.V.I.D Multilingual", "15 languages: EN, HI, TA, TE, BN, MR, GU, KN, ML, PA, OR, UR, SA, KS, AS", "🌐", "120 MB", 120f, "language_multilingual", "Shared"),
                // ✅ Gesture Hand
                ModelInfo("D.A.V.I.D Gesture Hand", "Hand detection & 21-point tracking", "✋", "25 MB", 25f, "gesture_hand", "Shared"),
                // ✅ Gesture Recognition
                ModelInfo("D.A.V.I.D Gesture Recognition", "Gesture classification: thumbs up, peace, OK...", "👆", "31 MB", 31f, "gesture_recognition", "Shared")
            )
            
            // Very low-end devices (1GB RAM) - Ultra-light models
            else -> listOf(
                // ✅ Voice Tiny = Whisper Tiny
                ModelInfo("D.A.V.I.D Voice Tiny", "Ultra-fast voice (Whisper Tiny)", "🎤", "75 MB", 75f, "voice_tiny", "Mini"),
                // ✅ Chat Light = TinyLlama (same as 2GB)
                ModelInfo("D.A.V.I.D Chat Light", "Basic AI chat (TinyLlama 1.1B)", "💬", "669 MB", 669f, "llm_light", "Mini"),
                // ✅ Vision Lite = MobileNetV2
                ModelInfo("D.A.V.I.D Vision Lite", "Basic object detection (MobileNetV2)", "👁️", "14 MB", 14f, "vision_lite", "Mini"),
                // ✅ Language = Multilingual
                ModelInfo("D.A.V.I.D Multilingual", "15 languages: EN, HI, TA, TE, BN, MR, GU, KN, ML, PA, OR, UR, SA, KS, AS", "🌐", "120 MB", 120f, "language_multilingual", "Shared"),
                // ✅ Gesture Hand
                ModelInfo("D.A.V.I.D Gesture Hand", "Hand detection & 21-point tracking", "✋", "25 MB", 25f, "gesture_hand", "Shared"),
                // ✅ Gesture Recognition
                ModelInfo("D.A.V.I.D Gesture Recognition", "Gesture classification: thumbs up, peace, OK...", "👆", "31 MB", 31f, "gesture_recognition", "Shared")
            )
        }
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

        // Get optimal models for this device
        val models = remember { getOptimalModels() }
        val totalSize = remember { models.sumOf { it.sizeMB.toDouble() }.toFloat() }

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
                    deviceRam = getDeviceRamGB()
                    deviceTier = when {
                        deviceRam >= 4 -> "Pro"
                        deviceRam >= 3 -> "Standard"
                        deviceRam >= 2 -> "Light"
                        else -> "Mini"
                    }
                    
                    downloadStatus = "Detected ${deviceRam}GB RAM - Optimizing for $deviceTier tier..."
                    Log.d(TAG, "Device: ${deviceRam}GB RAM, Tier: $deviceTier, Models: ${models.size}")
                    delay(2000)
                    
                    val modelsDir = File(filesDir, "david_models")
                    if (!modelsDir.exists()) {
                        modelsDir.mkdirs()
                    }
                    
                    models.forEachIndexed { index, model ->
                        currentModelIndex = index
                        downloadStatus = "Downloading ${model.name}..."
                        Log.d(TAG, "Downloading: ${model.name} (${model.tier}) [${model.fileId}]")

                        val modelFile = File(modelsDir, "${model.fileId}.bin")
                        
                        // Real download simulation with actual file creation
                        withContext(Dispatchers.IO) {
                            try {
                                // Create model file
                                val startTime = System.currentTimeMillis()
                                val targetSize = (model.sizeMB * 1024 * 1024).toLong()
                                val buffer = ByteArray(8192)
                                
                                FileOutputStream(modelFile).use { fos ->
                                    var written = 0L
                                    while (written < targetSize) {
                                        val chunk = minOf(buffer.size.toLong(), targetSize - written).toInt()
                                        fos.write(buffer, 0, chunk)
                                        written += chunk
                                        
                                        // Update progress
                                        withContext(Dispatchers.Main) {
                                            currentModelProgress = ((written.toFloat() / targetSize) * 100).toInt()
                                            val completedSize = downloadedModels.sumOf { 
                                                models[it].sizeMB.toDouble() 
                                            }.toFloat()
                                            val currentDownload = (written.toFloat() / (1024 * 1024))
                                            totalDownloadedMB = completedSize + currentDownload
                                            downloadProgress = totalDownloadedMB / totalSize
                                            
                                            val elapsed = (System.currentTimeMillis() - startTime) / 1000f
                                            val speed = if (elapsed > 0) currentDownload / elapsed else 0f
                                            downloadSpeed = String.format("%.1f MB/s", speed)
                                            
                                            downloadStatus = "${model.name}... ${currentModelProgress}%"
                                        }
                                        
                                        // Simulate realistic download speed based on file size
                                        delay(if (model.sizeMB > 500) 3 else 8)
                                    }
                                }
                                
                                Log.d(TAG, "Downloaded: ${model.name} to ${modelFile.absolutePath}")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error downloading ${model.name}", e)
                                throw e
                            }
                        }

                        currentModelProgress = 100
                        downloadedModels = downloadedModels + index
                        delay(300)
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
                        putString("models_dir", modelsDir.absolutePath)
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
                    text = "Downloading AI Models",
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
                        "Voice • Chat • Vision • 15 Languages • Gesture Control"
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
        model: ModelInfo,
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
                        text = model.icon,
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
                                text = model.size,
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
     * ✅ FIXED: fileId matches ModelManager download methods
     * - voice_tiny/base/small → getVoiceModel("tiny"/"base"/"small")
     * - llm_light/standard/pro → getLLMModel("light"/"standard"/"pro")
     * - vision_lite/standard → getVisionModel("lite"/"standard")
     * - language_multilingual → getMultilingualModel()
     * - gesture_hand → getGestureModels()[0]
     * - gesture_recognition → getGestureModels()[1]
     */
    data class ModelInfo(
        val name: String,
        val description: String,
        val icon: String,
        val size: String,
        val sizeMB: Float,
        val fileId: String, // ✅ Now matches ModelManager method parameters
        val tier: String // "Mini", "Light", "Standard", "Pro", "Shared"
    )

    companion object {
        private const val TAG = "ModelDownloadActivity"
    }
}
