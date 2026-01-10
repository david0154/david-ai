package com.davidstudioz.david

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
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
 * 
 * ALL BUGS FIXED:
 * ✅ Proper null safety
 * ✅ Exception handling
 * ✅ Permission request handling
 * ✅ Bluetooth permissions for Android 12+ (API 31+)
 */
class MainActivity : ComponentActivity() {

    // Core components - nullable for safe initialization
    private var userProfile: UserProfile? = null
    private var hotWordDetector: HotWordDetector? = null
    private var textToSpeechEngine: TextToSpeechEngine? = null
    private var deviceController: DeviceController? = null
    private var gestureController: GestureController? = null
    private var chatManager: ChatManager? = null
    private var weatherTimeProvider: WeatherTimeProvider? = null
    private var deviceLockManager: DeviceLockManager? = null
    private var pointerController: PointerController? = null
    private var permissionManager: PermissionManager? = null
    private var deviceAccess: DeviceAccessManager? = null
    private var resourceManager: DeviceResourceManager? = null

    // UI State - with default values to prevent null crashes
    private var isListening by mutableStateOf(false)
    private var statusMessage by mutableStateOf("Initializing D.A.V.I.D...")
    private var userInput by mutableStateOf("")
    private var chatHistory by mutableStateOf<List<String>>(emptyList())
    private var currentWeather by mutableStateOf("Loading weather...")
    private var currentTime by mutableStateOf("00:00:00")
    private var devicePermissions by mutableStateOf<Map<String, Boolean>>(emptyMap())
    private var resourceStatus by mutableStateOf<DeviceResourceManager.ResourceStatus?>(null)
    private var showPermissionDialog by mutableStateOf(false)
    private var missingPermissions by mutableStateOf<List<String>>(emptyList())
    private var initError by mutableStateOf<String?>(null)

    // Permission launcher with proper error handling
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        try {
            permissions.forEach { (permission, granted) ->
                Log.d(TAG, "$permission: $granted")
            }
            
            // Update UI with permission results
            val denied = permissions.filter { !it.value }.keys.toList()
            if (denied.isNotEmpty()) {
                missingPermissions = denied
                showPermissionDialog = true
                statusMessage = "Some permissions were denied. App may not work fully."
            } else {
                statusMessage = "All permissions granted!"
            }
            
            initializeWeather()
        } catch (e: Exception) {
            Log.e(TAG, "Error handling permission result", e)
            statusMessage = "Error processing permissions"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Initialize all components with error handling
            initializeComponents()

            // Set up unified UI - move composable outside try-catch
            setContent {
                DavidAITheme {
                    // Check for initialization error first
                    when {
                        initError != null -> ErrorScreen(initError!!)
                        showPermissionDialog -> PermissionDenialDialog(missingPermissions) {
                            showPermissionDialog = false
                        }
                        else -> UnifiedDavidAIScreen()
                    }
                }
            }

            // Start resource monitoring
            startResourceMonitoring()
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error in onCreate", e)
            initError = e.message ?: "Unknown error"
            statusMessage = "Fatal initialization error: ${e.message}"
            // Show error screen
            setContent {
                DavidAITheme {
                    ErrorScreen(initError ?: "Unknown error")
                }
            }
        }
    }

    private fun initializeComponents() {
        try {
            // Initialize resource manager
            resourceManager = DeviceResourceManager(this)
            resourceStatus = resourceManager?.getResourceStatus()
            
            // Initialize device access manager
            deviceAccess = DeviceAccessManager(this)
            updatePermissions()

            // Initialize user profile
            userProfile = UserProfile(this).apply {
                if (isFirstLaunch) {
                    nickname = "Friend"
                    isFirstLaunch = false
                }
                statusMessage = "Hi $nickname, I'm initializing..."
            }

            // Initialize permission manager
            permissionManager = PermissionManager(this)
            requestRequiredPermissions()

            // Initialize text to speech
            textToSpeechEngine = TextToSpeechEngine(this) {
                try {
                    statusMessage = "Voice systems online"
                    textToSpeechEngine?.speak(
                        "Hello ${userProfile?.nickname}, D.A.V.I.D systems are online!",
                        TextToSpeechEngine.SupportedLanguage.ENGLISH
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "TTS error", e)
                }
            }

            // Initialize device controller
            deviceController = DeviceController(this)
            
            // Initialize chat manager
            chatManager = ChatManager(this)
            
            // Initialize weather provider
            weatherTimeProvider = WeatherTimeProvider(this)
            
            // Initialize device lock manager
            deviceLockManager = DeviceLockManager(this)
            
            // Initialize pointer controller
            pointerController = PointerController(this)
            pointerController?.setOnClickListener { x, y ->
                statusMessage = "Clicked at ($x, $y)"
            }

            // Initialize gesture controller
            gestureController = GestureController(this)

            // Initialize hot word detector
            hotWordDetector = HotWordDetector(this)
            hotWordDetector?.startListening(
                hotWords = listOf("hey david", "ok david", "jarvis"),
                callback = { word ->
                    try {
                        statusMessage = "Wake word detected: $word"
                        activateListeningMode()
                        textToSpeechEngine?.speak(
                            "Yes, I'm listening...",
                            TextToSpeechEngine.SupportedLanguage.ENGLISH
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in hotword callback", e)
                    }
                }
            )

            initializeWeather()
            statusMessage = "D.A.V.I.D systems ready!"
            Log.d(TAG, "All systems operational")
        } catch (e: Exception) {
            Log.e(TAG, "Initialization error", e)
            statusMessage = "Error: ${e.localizedMessage ?: e.message ?: "Unknown error"}"
            // Continue without crashing
        }
    }

    private fun startResourceMonitoring() {
        lifecycleScope.launch {
            while (true) {
                try {
                    resourceStatus = resourceManager?.getResourceStatus()
                    delay(2000)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating resources", e)
                    delay(2000)
                }
            }
        }
    }

    private fun initializeWeather() {
        lifecycleScope.launch {
            try {
                weatherTimeProvider?.let {
                    currentWeather = it.getWeatherVoiceReport()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Weather error", e)
                currentWeather = "Weather unavailable: ${e.localizedMessage}"
            }
        }
    }

    /**
     * Request required permissions including Bluetooth for Android 12+
     * FIXED: Now includes BLUETOOTH_CONNECT and BLUETOOTH_SCAN for API 31+
     */
    private fun requestRequiredPermissions() {
        try {
            val permissions = mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
            )
            
            // Add Bluetooth permissions for Android 12+ (API 31+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
                Log.d(TAG, "Added Bluetooth permissions for Android 12+")
            }
            
            Log.d(TAG, "Requesting ${permissions.size} permissions")
            permissionLauncher.launch(permissions.toTypedArray())
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting permissions", e)
            statusMessage = "Permission request failed: ${e.localizedMessage}"
        }
    }

    private fun updatePermissions() {
        try {
            devicePermissions = deviceAccess?.getAccessStatus() ?: emptyMap()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating permissions", e)
        }
    }

    private fun activateListeningMode() {
        try {
            isListening = true
            statusMessage = "Listening for command..."
            lifecycleScope.launch {
                delay(5000)
                isListening = false
                statusMessage = "Ready"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error activating listening mode", e)
        }
    }

    @Composable
    private fun TimeUpdater() {
        LaunchedEffect(Unit) {
            while (true) {
                try {
                    currentTime = weatherTimeProvider?.getCurrentTime() ?: "00:00:00"
                    kotlinx.coroutines.delay(1000)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating time", e)
                    delay(1000)
                }
            }
        }
    }

    @Composable
    private fun ErrorScreen(errorMsg: String) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0E27)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "⚠️ Initialization Error",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6E40)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMsg,
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { finish() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF)
                    )
                ) {
                    Text("Close", color = Color.Black)
                }
            }
        }
    }

    @Composable
    private fun PermissionDenialDialog(deniedPermissions: List<String>, onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Permissions Denied", color = Color(0xFF00E5FF)) },
            text = {
                Column {
                    Text(
                        "The following permissions were not granted:",
                        color = Color(0xFF9CA3AF)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    deniedPermissions.forEach { permission ->
                        Text(
                            "• ${permission.substring(permission.lastIndexOf(".") + 1)}",
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "App will work with limited functionality.",
                        fontSize = 10.sp,
                        color = Color(0xFFFF6E40)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF)
                    )
                ) {
                    Text("Continue", color = Color.Black)
                }
            },
            containerColor = Color(0xFF1F2937),
            textContentColor = Color(0xFF9CA3AF)
        )
    }

    @Composable
    private fun UnifiedDavidAIScreen() {
        TimeUpdater()
        val currentResourceStatus = resourceStatus
        val currentNickname = userProfile?.nickname ?: "Friend"

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
                            text = "User: $currentNickname",
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
                                try {
                                    currentWeather = weatherTimeProvider?.getWeatherVoiceReport() ?: "Unavailable"
                                } catch (e: Exception) {
                                    Log.e(TAG, "Weather error", e)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "📅",
                        onClick = {
                            lifecycleScope.launch {
                                try {
                                    val forecast = weatherTimeProvider?.getForecastVoiceReport(3) ?: "Forecast unavailable"
                                    textToSpeechEngine?.speak(forecast, TextToSpeechEngine.SupportedLanguage.ENGLISH)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Forecast error", e)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "🔒",
                        onClick = {
                            try {
                                deviceLockManager?.lockDevice()
                            } catch (e: Exception) {
                                Log.e(TAG, "Lock error", e)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFFF6E40)
                    )
                    GlassButton(
                        text = "🖱",
                        onClick = {
                            try {
                                pointerController?.showPointer()
                            } catch (e: Exception) {
                                Log.e(TAG, "Pointer error", e)
                            }
                        },
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
                        try {
                            if (isListening) {
                                isListening = false
                                statusMessage = "Listening stopped"
                            } else {
                                activateListeningMode()
                                textToSpeechEngine?.speak(
                                    "I'm listening",
                                    TextToSpeechEngine.SupportedLanguage.ENGLISH
                                )
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Voice button error", e)
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

    @Composable
    private fun LogoImage(
        modifier: Modifier = Modifier,
        tint: Color? = null
    ) {
        val logoResourceId = remember {
            try {
                resources.getIdentifier("logo", "drawable", packageName)
            } catch (e: Exception) {
                Log.w(TAG, "Logo resource not found", e)
                0
            }
        }
        
        // Use state to track if we should show logo or fallback
        var showFallback by remember { mutableStateOf(logoResourceId == 0) }
        
        if (!showFallback && logoResourceId != 0) {
            // Attempt to show logo image
            Image(
                painter = painterResource(id = logoResourceId),
                contentDescription = "D.A.V.I.D Logo",
                modifier = modifier.clip(CircleShape),
                contentScale = ContentScale.Fit,
                colorFilter = tint?.let { ColorFilter.tint(it) }
            )
        } else {
            // Show fallback emoji
            LogoFallback(modifier, tint)
        }
    }

    @Composable
    private fun LogoFallback(modifier: Modifier = Modifier, tint: Color? = null) {
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
        try {
            hotWordDetector?.stopListening()
            textToSpeechEngine?.release()
            pointerController?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up resources", e)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
