package com.davidstudioz.david

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.davidstudioz.david.chat.ChatManager
import com.davidstudioz.david.device.DeviceAccessManager
import com.davidstudioz.david.device.DeviceController
import com.davidstudioz.david.features.WeatherTimeProvider
import com.davidstudioz.david.gesture.GestureController
import com.davidstudioz.david.permissions.PermissionManager
import com.davidstudioz.david.pointer.PointerController
import com.davidstudioz.david.profile.UserProfile
import com.davidstudioz.david.security.DeviceLockManager
import com.davidstudioz.david.ui.JarvisComponents
import com.davidstudioz.david.utils.DeviceResourceManager
import com.davidstudioz.david.voice.HotWordDetector
import com.davidstudioz.david.voice.TextToSpeechEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * DAVID AI - Unified Jarvis-Style Interface with Logo
 * ALL features in ONE beautiful sci-fi UI
 * Features: Logo + Jarvis Orb + Weather + Chat + Voice + Resource Monitoring + All Controls
 */
class MainActivity : ComponentActivity() {

    // Core components
    private lateinit var userProfile: UserProfile
    private lateinit var hotWordDetector: HotWordDetector
    private lateinit var textToSpeechEngine: TextToSpeechEngine
    private lateinit var deviceController: DeviceController
    private lateinit var gestureController: GestureController
    private lateinit var chatManager: ChatManager
    private lateinit var weatherTimeProvider: WeatherTimeProvider
    private lateinit var deviceLockManager: DeviceLockManager
    private lateinit var pointerController: PointerController
    private lateinit var permissionManager: PermissionManager
    private lateinit var deviceAccess: DeviceAccessManager
    private lateinit var resourceManager: DeviceResourceManager

    // UI State
    private var isListening by mutableStateOf(false)
    private var statusMessage by mutableStateOf("Initializing DAVID AI...")
    private var userInput by mutableStateOf("")
    private var chatHistory by mutableStateOf<List<String>>(emptyList())
    private var currentWeather by mutableStateOf("Loading weather...")
    private var currentTime by mutableStateOf("00:00:00")
    private var devicePermissions by mutableStateOf<Map<String, Boolean>>(emptyMap())
    private var resourceStatus by mutableStateOf<DeviceResourceManager.ResourceStatus?>(null)

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.forEach { (permission, granted) ->
            Log.d("MainActivity", "$permission: $granted")
        }
        initializeWeather()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize all components
        initializeComponents()

        // Set up unified UI
        setContent {
            DavidAITheme {
                UnifiedDavidAIScreen()
            }
        }

        // Start resource monitoring
        startResourceMonitoring()
    }

    private fun initializeComponents() {
        try {
            resourceManager = DeviceResourceManager(this)
            resourceStatus = resourceManager.getResourceStatus()
            
            deviceAccess = DeviceAccessManager(this)
            updatePermissions()

            userProfile = UserProfile(this)
            if (userProfile.isFirstLaunch) {
                userProfile.nickname = "Friend"
                userProfile.isFirstLaunch = false
            }
            statusMessage = "Hi ${userProfile.nickname}, I'm ready!"

            permissionManager = PermissionManager(this)
            requestRequiredPermissions()

            textToSpeechEngine = TextToSpeechEngine(this) {
                statusMessage = "Voice systems online"
                textToSpeechEngine.speak(
                    "Hello ${userProfile.nickname}, DAVID systems are online!",
                    TextToSpeechEngine.SupportedLanguage.ENGLISH
                )
            }

            deviceController = DeviceController(this)
            chatManager = ChatManager(this)
            weatherTimeProvider = WeatherTimeProvider(this)
            deviceLockManager = DeviceLockManager(this)
            
            pointerController = PointerController(this)
            pointerController.setOnClickListener { x, y ->
                statusMessage = "Clicked at ($x, $y)"
            }

            gestureController = GestureController(this)

            hotWordDetector = HotWordDetector(this)
            hotWordDetector.startListening(
                hotWords = listOf("hey david", "ok david", "jarvis"),
                callback = { word ->
                    statusMessage = "Wake word detected: $word"
                    activateListeningMode()
                    textToSpeechEngine.speak(
                        "Yes, I'm listening...",
                        TextToSpeechEngine.SupportedLanguage.ENGLISH
                    )
                }
            )

            initializeWeather()
            Log.d(TAG, "All systems operational")
        } catch (e: Exception) {
            statusMessage = "Error: ${e.message}"
            Log.e(TAG, "Initialization error", e)
        }
    }

    private fun startResourceMonitoring() {
        lifecycleScope.launch {
            while (true) {
                try {
                    resourceStatus = resourceManager.getResourceStatus()
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating resources", e)
                }
                delay(2000)
            }
        }
    }

    private fun initializeWeather() {
        lifecycleScope.launch {
            try {
                currentWeather = weatherTimeProvider.getWeatherVoiceReport()
            } catch (e: Exception) {
                currentWeather = "Weather unavailable"
            }
        }
    }

    private fun requestRequiredPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.INTERNET
        )
        permissionLauncher.launch(permissions)
    }

    private fun updatePermissions() {
        devicePermissions = deviceAccess.getAccessStatus()
    }

    private fun activateListeningMode() {
        isListening = true
        statusMessage = "Listening for command..."
        lifecycleScope.launch {
            delay(5000)
            isListening = false
            statusMessage = "Ready"
        }
    }

    @Composable
    private fun TimeUpdater() {
        LaunchedEffect(Unit) {
            while (true) {
                currentTime = weatherTimeProvider.getCurrentTime()
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    /**
     * UNIFIED JARVIS UI - ALL Features Combined with Logo!
     */
    @Composable
    private fun UnifiedDavidAIScreen() {
        TimeUpdater()
        val currentResourceStatus = resourceStatus

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
            // Animated background grid
            JarvisComponents.AnimatedGrid()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with LOGO and time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // LOGO HERE!
                        LogoImage(
                            modifier = Modifier.size(40.dp),
                            tint = Color(0xFF00E5FF)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "D.A.V.I.D",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF),
                                letterSpacing = 6.sp
                            )
                            Text(
                                text = "Digital Assistant",
                                fontSize = 9.sp,
                                color = Color(0xFF64B5F6),
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = currentTime,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                        Text(
                            text = "User: ${userProfile.nickname}",
                            fontSize = 10.sp,
                            color = Color(0xFF64B5F6)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // AI Orb with LOGO inside
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    JarvisComponents.AIOrb(
                        isListening = isListening,
                        centerContent = {
                            // LOGO INSIDE ORB!
                            LogoImage(
                                modifier = Modifier.size(50.dp),
                                tint = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status message
                GlassCard {
                    Text(
                        text = statusMessage,
                        fontSize = 12.sp,
                        color = Color(0xFF00E5FF),
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Resource Status Rings
                currentResourceStatus?.let { status ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        JarvisComponents.ResourceRing(
                            label = "RAM",
                            usage = status.ramUsagePercent,
                            value = "${status.usedRamMB / 1024}/${status.totalRamMB / 1024}GB",
                            color = Color(0xFF00E5FF)
                        )
                        JarvisComponents.ResourceRing(
                            label = "STORAGE",
                            usage = status.storageUsagePercent,
                            value = "${status.usedStorageGB}/${status.totalStorageGB}GB",
                            color = Color(0xFF00FF88)
                        )
                        JarvisComponents.ResourceRing(
                            label = "CPU",
                            usage = status.cpuUsagePercent,
                            value = "${status.cpuCores} cores",
                            color = Color(0xFFFF6E40)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // AI Model info
                    GlassCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "AI MODEL: ${status.canUseForAI.recommendedModel.name}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                            Text(
                                text = if (status.canUseForAI.canDownloadModel) "✓ Ready" else "⚠ Limited",
                                fontSize = 10.sp,
                                color = if (status.canUseForAI.canDownloadModel) Color(0xFF00FF88) else Color(0xFFFF6E40)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Weather Card
                GlassCard {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🌤 WEATHER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64B5F6),
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentWeather,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassButton(
                        text = "🌤",
                        onClick = {
                            lifecycleScope.launch {
                                currentWeather = weatherTimeProvider.getWeatherVoiceReport()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "📅",
                        onClick = {
                            lifecycleScope.launch {
                                val forecast = weatherTimeProvider.getForecastVoiceReport(3)
                                textToSpeechEngine.speak(forecast, TextToSpeechEngine.SupportedLanguage.ENGLISH)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "🔒",
                        onClick = { deviceLockManager.lockDevice() },
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFFF6E40)
                    )
                    GlassButton(
                        text = "🖱",
                        onClick = { pointerController.showPointer() },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Chat History
                Text(
                    text = "CHAT HISTORY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64B5F6),
                    letterSpacing = 2.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                GlassCard {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(8.dp)
                    ) {
                        items(chatHistory.size) { index ->
                            Text(
                                text = "→ ${chatHistory[index]}",
                                fontSize = 10.sp,
                                color = Color(0xFF9CA3AF),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        if (chatHistory.isEmpty()) {
                            item {
                                Text(
                                    text = "No messages yet...",
                                    fontSize = 10.sp,
                                    color = Color(0xFF4B5563)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Voice Button
                FloatingActionButton(
                    onClick = {
                        if (isListening) {
                            isListening = false
                            statusMessage = "Listening stopped"
                        } else {
                            activateListeningMode()
                            textToSpeechEngine.speak(
                                "I'm listening",
                                TextToSpeechEngine.SupportedLanguage.ENGLISH
                            )
                        }
                    },
                    modifier = Modifier.size(64.dp),
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color.Black
                ) {
                    Text(
                        text = if (isListening) "🔊" else "🎤",
                        fontSize = 28.sp
                    )
                }
            }
        }
    }

    /**
     * Logo Image Component
     * Displays logo.png from drawable folder
     * Falls back to robot emoji if not found
     */
    @Composable
    private fun LogoImage(
        modifier: Modifier = Modifier,
        tint: Color? = null
    ) {
        // Try to load logo.png from drawable
        val logoResourceId = remember {
            try {
                resources.getIdentifier("logo", "drawable", packageName)
            } catch (e: Exception) {
                0
            }
        }
        
        if (logoResourceId != 0) {
            Image(
                painter = painterResource(id = logoResourceId),
                contentDescription = "DAVID AI Logo",
                modifier = modifier.clip(CircleShape),
                contentScale = ContentScale.Fit,
                colorFilter = tint?.let { ColorFilter.tint(it) }
            )
        } else {
            // Fallback: Show robot emoji
            Box(
                modifier = modifier
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                tint ?: Color(0xFF00E5FF),
                                (tint ?: Color(0xFF00E5FF)).copy(alpha = 0.5f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🤖",
                    fontSize = 24.sp
                )
            }
        }
    }

    @Composable
    private fun GlassCard(content: @Composable () -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Color(0xFF1E88E5).copy(alpha = 0.1f)
                )
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = 0.05f),
                            Color.Transparent,
                            Color(0xFF00E5FF).copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            content()
        }
    }

    @Composable
    private fun GlassButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        color: Color = Color(0xFF00E5FF)
    ) {
        Button(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = color.copy(alpha = 0.2f),
                contentColor = color
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text, fontSize = 20.sp)
        }
    }

    @Composable
    private fun DavidAITheme(content: @Composable () -> Unit) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFF00E5FF),
                secondary = Color(0xFF9CA3AF)
            ),
            content = content
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        hotWordDetector.stopListening()
        textToSpeechEngine.release()
        pointerController.release()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
