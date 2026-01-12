package com.davidstudioz.david.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidstudioz.david.models.AIModel
import com.davidstudioz.david.models.ModelManager
import kotlinx.coroutines.launch

/**
 * ModelManagementUI - REAL Model Management with Accurate Names
 * ✅ FIXED: Model names match actual ModelManager download links
 * ✅ FIXED: Model sizes match real files from HuggingFace/MediaPipe
 * ✅ FIXED: Device tier recommendations match RAM requirements
 * 
 * Connected to: ModelManager.kt (real download links)
 * Models from: HuggingFace, Google MediaPipe, ONNX Model Zoo
 */

data class ModelInfo(
    val displayName: String,
    val description: String,
    val icon: String,
    val sizeText: String,
    val sizeMB: Float,
    val internalName: String,  // Matches ModelManager AIModel.name
    val tier: String  // Pro, Standard, Light, Mini, Shared
)

/**
 * Get recommended models based on device RAM
 * ✅ MATCHES ModelManager.getEssentialModels() logic
 */
fun getRecommendedModels(context: Context): List<ModelInfo> {
    val modelManager = ModelManager(context)
    val deviceRam = modelManager.getDeviceRamGB()
    
    return when {
        // High-end devices (4GB+ RAM) - Pro models
        deviceRam >= 4 -> listOf(
            // Voice: Whisper Small (466MB)
            ModelInfo(
                "D.A.V.I.D Voice Pro",
                "High-accuracy speech recognition (Whisper Small)",
                "🎤",
                "466 MB",
                466f,
                "D.A.V.I.D Voice Pro",  // Matches ModelManager
                "Pro"
            ),
            // Chat: Phi-2 (1.6GB)
            ModelInfo(
                "D.A.V.I.D Chat Pro",
                "Advanced AI conversations (Phi-2 2.7B)",
                "💬",
                "1.6 GB",
                1600f,
                "D.A.V.I.D Chat Pro",  // Matches ModelManager
                "Pro"
            ),
            // Vision: ResNet50 (98MB)
            ModelInfo(
                "D.A.V.I.D Vision Standard",
                "Advanced image recognition (ResNet50)",
                "👁️",
                "98 MB",
                98f,
                "D.A.V.I.D Vision Standard",  // Matches ModelManager
                "Pro"
            ),
            // Multilingual (120MB)
            ModelInfo(
                "D.A.V.I.D Multilingual",
                "15 languages: EN, HI, TA, TE, BN, MR, GU, KN, ML, PA, OR, UR, SA, KS, AS",
                "🌐",
                "120 MB",
                120f,
                "D.A.V.I.D Multilingual",  // Matches ModelManager
                "Shared"
            ),
            // Gesture Hand (25MB)
            ModelInfo(
                "D.A.V.I.D Gesture Hand",
                "Hand detection & 21-point tracking (MediaPipe)",
                "✋",
                "25 MB",
                25f,
                "D.A.V.I.D Gesture Hand",  // Matches ModelManager
                "Shared"
            ),
            // Gesture Recognition (31MB)
            ModelInfo(
                "D.A.V.I.D Gesture Recognition",
                "Gesture classification: thumbs up, peace, OK, etc. (MediaPipe)",
                "👆",
                "31 MB",
                31f,
                "D.A.V.I.D Gesture Recognition",  // Matches ModelManager
                "Shared"
            )
        )
        
        // Mid-range devices (3GB RAM) - Standard models
        deviceRam >= 3 -> listOf(
            // Voice: Whisper Base (142MB)
            ModelInfo(
                "D.A.V.I.D Voice Base",
                "Balanced speech recognition (Whisper Base)",
                "🎤",
                "142 MB",
                142f,
                "D.A.V.I.D Voice Base",  // Matches ModelManager
                "Standard"
            ),
            // Chat: Qwen 1.8B (1.1GB)
            ModelInfo(
                "D.A.V.I.D Chat Standard",
                "Smart AI conversations (Qwen 1.5-1.8B)",
                "💬",
                "1.1 GB",
                1100f,
                "D.A.V.I.D Chat Standard",  // Matches ModelManager
                "Standard"
            ),
            // Vision: ResNet50 (98MB)
            ModelInfo(
                "D.A.V.I.D Vision Standard",
                "Standard image recognition (ResNet50)",
                "👁️",
                "98 MB",
                98f,
                "D.A.V.I.D Vision Standard",  // Matches ModelManager
                "Standard"
            ),
            // Multilingual (120MB)
            ModelInfo(
                "D.A.V.I.D Multilingual",
                "15 languages: EN, HI, TA, TE, BN, MR, GU, KN, ML, PA, OR, UR, SA, KS, AS",
                "🌐",
                "120 MB",
                120f,
                "D.A.V.I.D Multilingual",  // Matches ModelManager
                "Shared"
            ),
            // Gesture Hand (25MB)
            ModelInfo(
                "D.A.V.I.D Gesture Hand",
                "Hand detection & 21-point tracking (MediaPipe)",
                "✋",
                "25 MB",
                25f,
                "D.A.V.I.D Gesture Hand",  // Matches ModelManager
                "Shared"
            ),
            // Gesture Recognition (31MB)
            ModelInfo(
                "D.A.V.I.D Gesture Recognition",
                "Gesture classification: thumbs up, peace, OK, etc. (MediaPipe)",
                "👆",
                "31 MB",
                31f,
                "D.A.V.I.D Gesture Recognition",  // Matches ModelManager
                "Shared"
            )
        )
        
        // Budget devices (2GB RAM) - Light models
        deviceRam >= 2 -> listOf(
            // Voice: Whisper Base (142MB)
            ModelInfo(
                "D.A.V.I.D Voice Base",
                "Fast speech recognition (Whisper Base)",
                "🎤",
                "142 MB",
                142f,
                "D.A.V.I.D Voice Base",  // Matches ModelManager
                "Light"
            ),
            // Chat: TinyLlama (669MB)
            ModelInfo(
                "D.A.V.I.D Chat Light",
                "Efficient AI chat (TinyLlama 1.1B)",
                "💬",
                "669 MB",
                669f,
                "D.A.V.I.D Chat Light",  // Matches ModelManager
                "Light"
            ),
            // Vision: MobileNetV2 (14MB)
            ModelInfo(
                "D.A.V.I.D Vision Lite",
                "Fast object detection (MobileNetV2)",
                "👁️",
                "14 MB",
                14f,
                "D.A.V.I.D Vision Lite",  // Matches ModelManager
                "Light"
            ),
            // Multilingual (120MB)
            ModelInfo(
                "D.A.V.I.D Multilingual",
                "15 languages: EN, HI, TA, TE, BN, MR, GU, KN, ML, PA, OR, UR, SA, KS, AS",
                "🌐",
                "120 MB",
                120f,
                "D.A.V.I.D Multilingual",  // Matches ModelManager
                "Shared"
            ),
            // Gesture Hand (25MB)
            ModelInfo(
                "D.A.V.I.D Gesture Hand",
                "Hand detection & 21-point tracking (MediaPipe)",
                "✋",
                "25 MB",
                25f,
                "D.A.V.I.D Gesture Hand",  // Matches ModelManager
                "Shared"
            ),
            // Gesture Recognition (31MB)
            ModelInfo(
                "D.A.V.I.D Gesture Recognition",
                "Gesture classification: thumbs up, peace, OK, etc. (MediaPipe)",
                "👆",
                "31 MB",
                31f,
                "D.A.V.I.D Gesture Recognition",  // Matches ModelManager
                "Shared"
            )
        )
        
        // Very low-end devices (1GB RAM) - Ultra-light models
        else -> listOf(
            // Voice: Whisper Tiny (75MB)
            ModelInfo(
                "D.A.V.I.D Voice Tiny",
                "Ultra-fast speech (Whisper Tiny)",
                "🎤",
                "75 MB",
                75f,
                "D.A.V.I.D Voice Tiny",  // Matches ModelManager
                "Mini"
            ),
            // Chat: TinyLlama (669MB)
            ModelInfo(
                "D.A.V.I.D Chat Light",
                "Basic AI chat (TinyLlama 1.1B)",
                "💬",
                "669 MB",
                669f,
                "D.A.V.I.D Chat Light",  // Matches ModelManager
                "Mini"
            ),
            // Vision: MobileNetV2 (14MB)
            ModelInfo(
                "D.A.V.I.D Vision Lite",
                "Basic object detection (MobileNetV2)",
                "👁️",
                "14 MB",
                14f,
                "D.A.V.I.D Vision Lite",  // Matches ModelManager
                "Mini"
            ),
            // Multilingual (120MB)
            ModelInfo(
                "D.A.V.I.D Multilingual",
                "15 languages: EN, HI, TA, TE, BN, MR, GU, KN, ML, PA, OR, UR, SA, KS, AS",
                "🌐",
                "120 MB",
                120f,
                "D.A.V.I.D Multilingual",  // Matches ModelManager
                "Shared"
            ),
            // Gesture Hand (25MB)
            ModelInfo(
                "D.A.V.I.D Gesture Hand",
                "Hand detection & 21-point tracking (MediaPipe)",
                "✋",
                "25 MB",
                25f,
                "D.A.V.I.D Gesture Hand",  // Matches ModelManager
                "Shared"
            ),
            // Gesture Recognition (31MB)
            ModelInfo(
                "D.A.V.I.D Gesture Recognition",
                "Gesture classification: thumbs up, peace, OK, etc. (MediaPipe)",
                "👆",
                "31 MB",
                31f,
                "D.A.V.I.D Gesture Recognition",  // Matches ModelManager
                "Shared"
            )
        )
    }
}

/**
 * Convert ModelInfo to AIModel for download
 */
fun ModelInfo.toAIModel(modelManager: ModelManager): AIModel? {
    // Map UI display names to ModelManager's AIModel instances
    return when (internalName) {
        "D.A.V.I.D Voice Tiny" -> modelManager.getVoiceModel("tiny")
        "D.A.V.I.D Voice Base" -> modelManager.getVoiceModel("base")
        "D.A.V.I.D Voice Pro" -> modelManager.getVoiceModel("small")
        "D.A.V.I.D Chat Light" -> modelManager.getLLMModel("light")
        "D.A.V.I.D Chat Standard" -> modelManager.getLLMModel("standard")
        "D.A.V.I.D Chat Pro" -> modelManager.getLLMModel("pro")
        "D.A.V.I.D Vision Lite" -> modelManager.getVisionModel("lite")
        "D.A.V.I.D Vision Standard" -> modelManager.getVisionModel("standard")
        "D.A.V.I.D Multilingual" -> modelManager.getMultilingualModel()
        "D.A.V.I.D Gesture Hand", "D.A.V.I.D Gesture Recognition" -> {
            modelManager.getGestureModels().find { it.name == internalName }
        }
        else -> null
    }
}

/**
 * UI Component for Model Management
 */
@Composable
fun ModelManagementCard(
    context: Context,
    modelManager: ModelManager,
    onModelsUpdated: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val recommendedModels = remember { getRecommendedModels(context) }
    val deviceRam = remember { modelManager.getDeviceRamGB() }
    val downloadedModels = remember { mutableStateOf(modelManager.getDownloadedModels()) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1F3A)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🤖 AI Models",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Device: ${deviceRam}GB RAM",
                        fontSize = 12.sp,
                        color = Color(0xFF00E5FF)
                    )
                }
                
                Text(
                    text = "${downloadedModels.value.size}/${recommendedModels.size}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFF374151))
            Spacer(modifier = Modifier.height(16.dp))
            
            // Recommended Models List
            Text(
                text = "Recommended for your device:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF9CA3AF)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recommendedModels) { model ->
                    ModelItem(
                        modelInfo = model,
                        isDownloaded = downloadedModels.value.any { 
                            it.name.contains(model.internalName.replace("D.A.V.I.D ", "").lowercase())
                        },
                        onDownload = {
                            scope.launch {
                                val aiModel = model.toAIModel(modelManager)
                                if (aiModel != null) {
                                    modelManager.downloadModel(aiModel) { progress ->
                                        // Progress handled by callback
                                    }
                                    downloadedModels.value = modelManager.getDownloadedModels()
                                    onModelsUpdated()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ModelItem(
    modelInfo: ModelInfo,
    isDownloaded: Boolean,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0A0E27)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Text(
                text = modelInfo.icon,
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            
            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = modelInfo.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Surface(
                        color = when (modelInfo.tier) {
                            "Pro" -> Color(0xFFFFD700)
                            "Standard" -> Color(0xFF00E5FF)
                            "Light" -> Color(0xFF10B981)
                            "Mini" -> Color(0xFF9CA3AF)
                            else -> Color(0xFF6B7280)
                        }.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = modelInfo.tier,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (modelInfo.tier) {
                                "Pro" -> Color(0xFFFFD700)
                                "Standard" -> Color(0xFF00E5FF)
                                "Light" -> Color(0xFF10B981)
                                "Mini" -> Color(0xFF9CA3AF)
                                else -> Color(0xFF6B7280)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Text(
                    text = modelInfo.description,
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF),
                    lineHeight = 14.sp
                )
                
                Text(
                    text = modelInfo.sizeText,
                    fontSize = 10.sp,
                    color = Color(0xFF6B7280)
                )
            }
            
            // Download/Status
            if (isDownloaded) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Downloaded",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                IconButton(
                    onClick = onDownload,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = "Download",
                        tint = Color(0xFF00E5FF)
                    )
                }
            }
        }
    }
}
