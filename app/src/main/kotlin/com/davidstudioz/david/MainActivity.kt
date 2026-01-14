package com.davidstudioz.david

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import com.davidstudioz.david.chat.ScriptureDownloadManager
import com.davidstudioz.david.device.DeviceAccessManager
import com.davidstudioz.david.device.DeviceController
import com.davidstudioz.david.features.WeatherTimeProvider
import com.davidstudioz.david.gesture.GestureController
import com.davidstudioz.david.permissions.PermissionManager
import com.davidstudioz.david.pointer.PointerController
import com.davidstudioz.david.profile.UserProfile
import com.davidstudioz.david.security.DeviceLockManager
import com.davidstudioz.david.services.ServiceManager
import com.davidstudioz.david.ui.*
import com.davidstudioz.david.utils.DeviceResourceManager
import com.davidstudioz.david.voice.HotWordDetectionService
import com.davidstudioz.david.voice.HotWordDetector
import com.davidstudioz.david.voice.TextToSpeechEngine
import com.davidstudioz.david.voice.VoiceController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * DAVID AI - COMPLETE INTEGRATION + CHAT + SCRIPTURE DOWNLOAD
 *
 * ALL FEATURES:
 * ✅ Complete voice & device control
 * ✅ Chat with copy/paste functionality
 * ✅ Auto-download 850+ scripture verses
 * ✅ Background services & hot word detection
 * ✅ Gesture control
 * ✅ Weather & time integration
 * ✅ Resource monitoring
 * ✅ All permissions handled
 */
class MainActivity : ComponentActivity() {

    // Core components
    private var userProfile: UserProfile? = null
    private var hotWordDetector: HotWordDetector? = null
    private var textToSpeechEngine: TextToSpeechEngine? = null
    private var deviceController: DeviceController? = null
    private var voiceController: VoiceController? = null
    private var gestureController: GestureController? = null
    private var chatManager: ChatManager? = null
    private var weatherTimeProvider: WeatherTimeProvider? = null
    private var deviceLockManager: DeviceLockManager? = null
    private var pointerController: PointerController? = null
    private var permissionManager: PermissionManager? = null
    private var deviceAccess: DeviceAccessManager? = null
    private var resourceManager: DeviceResourceManager? = null
    private var serviceManager: ServiceManager? = null
    private var hotWordReceiver: BroadcastReceiver? = null
    
    // NEW: Scripture download manager
    private var scriptureDownloadManager: ScriptureDownloadManager? = null

    // UI State
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
    
    // NEW: Chat & Download UI state
    private var showChatScreen by mutableStateOf(false)
    private var showDownloadDialog by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        try {
            permissions.forEach { (permission, granted) ->
                Log.d(TAG, "$permission: $granted")
            }
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
            initializeComponents()
            startBackgroundServices()
            
            // NEW: Check scripture download status
            scriptureDownloadManager = ScriptureDownloadManager(this)
            if (!scriptureDownloadManager!!.isDownloaded()) {
                showDownloadDialog = true
            }

            setContent {
                DavidAITheme {
                    when {
                        initError != null -> ErrorScreen(initError!!)
                        showPermissionDialog -> PermissionDenialDialog(missingPermissions) {
                            showPermissionDialog = false
                        }
                        showChatScreen -> ChatScreenWrapper()
                        else -> UnifiedDavidAIScreen()
                    }
                    
                    // NEW: Scripture download dialog
                    if (showDownloadDialog) {
                        ScriptureDownloadDialog(
                            onDismiss = { showDownloadDialog = false },
                            onComplete = {
                                statusMessage = "✅ Scriptures downloaded! 850+ verses available"
                                // Scripture files saved to internal storage
                                // ChatManager already has BhagavadGitaQuotes built-in
                            }
                        )
                    }
                }
            }

            startResourceMonitoring()
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error in onCreate", e)
            initError = e.message ?: "Unknown error"
            statusMessage = "Fatal initialization error: ${e.message}"
            setContent {
                DavidAITheme {
                    ErrorScreen(initError ?: "Unknown error")
                }
            }
        }
    }

    private fun initializeComponents() {
        try {
            resourceManager = DeviceResourceManager(this)
            resourceStatus = resourceManager?.getResourceStatus()

            deviceAccess = DeviceAccessManager(this)
            updatePermissions()

            userProfile = UserProfile(this).apply {
                if (isFirstLaunch) {
                    nickname = "Friend"
                    isFirstLaunch = false
                }
                statusMessage = "Hi $nickname, I'm initializing..."
            }

            permissionManager = PermissionManager(this)
            requestRequiredPermissions()

            textToSpeechEngine = TextToSpeechEngine(this)

            lifecycleScope.launch {
                delay(1000)
                try {
                    statusMessage = "Voice systems online"
                    textToSpeechEngine?.speak(
                        "Hello ${userProfile?.nickname}, D.A.V.I.D systems are online!"
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "TTS error", e)
                }
            }

            deviceController = DeviceController(this)
            Log.d(TAG, "Device controller initialized")

            chatManager = ChatManager(this)
            Log.d(TAG, "Chat manager initialized")

            voiceController = VoiceController(this, deviceController!!, chatManager)

            voiceController?.setOnCommandProcessed { command, response ->
                lifecycleScope.launch {
                    chatHistory = chatHistory + "You: $command" + "DAVID: $response"
                    statusMessage = response
                    Log.d(TAG, "Command processed: $command -> $response")
                }
            }
            Log.d(TAG, "Voice controller initialized with device controller AND chat manager")

            weatherTimeProvider = WeatherTimeProvider(this)
            deviceLockManager = DeviceLockManager(this)
            pointerController = PointerController(this)
            pointerController?.setOnClickListener { x, y ->
                statusMessage = "Clicked at ($x, $y)"
            }

            gestureController = GestureController(this)
            gestureController?.initialize { gesture ->
                lifecycleScope.launch {
                    try {
                        statusMessage = "Gesture detected: $gesture"
                        Log.d(TAG, "Gesture: $gesture")
                        when (gesture) {
                            GestureController.GESTURE_OPEN_PALM -> {
                                pointerController?.showPointer()
                                textToSpeechEngine?.speak("Pointer shown")
                            }
                            GestureController.GESTURE_CLOSED_FIST -> {
                                pointerController?.hidePointer()
                                textToSpeechEngine?.speak("Pointer hidden")
                            }
                            GestureController.GESTURE_VICTORY -> {
                                gestureController?.performClick()
                                textToSpeechEngine?.speak("Click performed")
                            }
                            GestureController.GESTURE_POINTING -> {
                                statusMessage = "Pointing gesture detected"
                            }
                            else -> {
                                Log.d(TAG, "Unhandled gesture: $gesture")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error handling gesture", e)
                    }
                }
            }
            Log.d(TAG, "Gesture controller initialized with callbacks")

            hotWordDetector = HotWordDetector(this)
            hotWordDetector?.startListening(
                hotWords = listOf("hey david", "ok david", "jarvis"),
                callback = { word ->
                    try {
                        statusMessage = "Wake word detected: $word"
                        activateListeningMode()
                        voiceController?.startListening()
                        textToSpeechEngine?.speak("Yes, I'm listening...")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in hotword callback", e)
                    }
                }
            )

            initializeWeather()
            statusMessage = "D.A.V.I.D systems ready! 100% complete!"
            Log.d(TAG, "ALL SYSTEMS OPERATIONAL - COMPLETE INTEGRATION SUCCESSFUL")
        } catch (e: Exception) {
            Log.e(TAG, "Initialization error", e)
            statusMessage = "Error: ${e.localizedMessage ?: e.message ?: "Unknown error"}"
        }
    }

    private fun startBackgroundServices() {
        try {
            Log.d(TAG, "Starting background services...")
            serviceManager = ServiceManager(this)
            serviceManager?.requestBatteryOptimizationBypass()

            if (serviceManager?.isHotWordServiceEnabled() == true) {
                HotWordDetectionService.start(this)
                Log.d(TAG, "✅ Hot word detection service started")
                statusMessage = "Always-on voice assistant active"
            }

            hotWordReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val hotWord = intent?.getStringExtra(HotWordDetectionService.EXTRA_HOTWORD)
                    if (hotWord != null) {
                        Log.d(TAG, "Hot word broadcast received: $hotWord")
                        lifecycleScope.launch {
                            try {
                                statusMessage = "Wake word detected: $hotWord"
                                chatHistory = chatHistory + "System: Wake word '$hotWord' detected"
                                activateListeningMode()
                                voiceController?.startListening()
                                textToSpeechEngine?.speak("Yes, I'm listening...")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error handling hot word", e)
                            }
                        }
                    }
                }
            }

            val filter = IntentFilter(HotWordDetectionService.ACTION_HOTWORD_DETECTED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(hotWordReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(hotWordReceiver, filter)
            }

            Log.d(TAG, "✅ Hot word broadcast receiver registered")
            Log.d(TAG, "✅ Background services initialization complete")

        } catch (e: Exception) {
            Log.e(TAG, "Error starting background services", e)
            statusMessage = "Background services unavailable: ${e.message}"
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
                Log.d(TAG, "Added Bluetooth permissions for Android 12+")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                Log.d(TAG, "Added notification permission for Android 13+")
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
    
    /**
     * NEW: Chat Screen Wrapper
     */
    @Composable
    private fun ChatScreenWrapper() {
        ChatScreen(
            messages = chatManager?.getMessages() ?: emptyList(),
            onSendMessage = { message ->
                lifecycleScope.launch {
                    try {
                        val response = chatManager?.sendMessage(message)
                        if (response != null) {
                            textToSpeechEngine?.speak(response.text)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error sending message", e)
                    }
                }
            },
            onVoiceInput = {
                try {
                    activateListeningMode()
                    voiceController?.startListening()
                } catch (e: Exception) {
                    Log.e(TAG, "Voice input error", e)
                }
            },
            onClearChat = {
                chatManager?.clearHistory()
                chatHistory = emptyList()
            }
        )
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
            JarvisComponents.AnimatedGrid()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Logo, Title, Time, Chat, and Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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

                    // Time, Chat, and Settings
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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

                        // NEW: Chat Button
                        IconButton(
                            onClick = { showChatScreen = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Chat",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Settings Button
                        IconButton(
                            onClick = {
                                try {
                                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error opening settings", e)
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // NEW: Download Button (if not downloaded)
                        if (scriptureDownloadManager?.isDownloaded() == false) {
                            IconButton(
                                onClick = { showDownloadDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download Scriptures",
                                    tint = Color(0xFFFF6E40),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    JarvisComponents.AIOrb(
                        isListening = isListening,
                        centerContent = {
                            LogoImage(
                                modifier = Modifier.size(50.dp),
                                tint = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                GlassCard {
                    Text(
                        text = statusMessage,
                        fontSize = 12.sp,
                        color = Color(0xFF00E5FF),
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                                    textToSpeechEngine?.speak(forecast)
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

                FloatingActionButton(
                    onClick = {
                        try {
                            if (isListening) {
                                isListening = false
                                voiceController?.stopListening()
                                statusMessage = "Listening stopped"
                            } else {
                                activateListeningMode()
                                voiceController?.startListening()
                                textToSpeechEngine?.speak("I'm listening")
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

        var showFallback by remember { mutableStateOf(logoResourceId == 0) }

        if (!showFallback && logoResourceId != 0) {
            Image(
                painter = painterResource(id = logoResourceId),
                contentDescription = "D.A.V.I.D Logo",
                modifier = modifier.clip(CircleShape),
                contentScale = ContentScale.Fit,
                colorFilter = tint?.let { ColorFilter.tint(it) }
            )
        } else {
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
            hotWordReceiver?.let {
                try {
                    unregisterReceiver(it)
                    Log.d(TAG, "Hot word receiver unregistered")
                } catch (e: Exception) {
                    Log.e(TAG, "Error unregistering receiver", e)
                }
                hotWordReceiver = null
            }

            hotWordDetector?.stopListening()
            pointerController?.release()
            gestureController?.release()
            voiceController?.cleanup()
            voiceController = null

            Log.d(TAG, "All resources cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up resources", e)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}