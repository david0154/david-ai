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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.davidstudioz.david.voice.HotWordDetector
import com.davidstudioz.david.voice.TextToSpeechEngine
import kotlinx.coroutines.launch

/**
 * DAVID AI - Main Activity
 * Voice-First Android AI Assistant with Gesture Control
 * Features: Voice Recognition, Weather (Open-Meteo), Device Control, Gesture Recognition, AI Chat
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

    // UI State
    private var isListening by mutableStateOf(false)
    private var statusMessage by mutableStateOf("Initializing DAVID AI...")
    private var userInput by mutableStateOf("")
    private var chatHistory by mutableStateOf<List<String>>(emptyList())
    private var currentWeather by mutableStateOf("Loading weather...")
    private var currentTime by mutableStateOf("00:00:00")
    private var devicePermissions by mutableStateOf<Map<String, Boolean>>(emptyMap())

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

        // Set up UI
        setContent {
            DavidAITheme {
                DavidAIScreen()
            }
        }
    }

    /**
     * Initialize all DAVID AI components
     */
    private fun initializeComponents() {
        try {
            // Device Access Manager
            deviceAccess = DeviceAccessManager(this)
            updatePermissions()

            // User Profile
            userProfile = UserProfile(this)
            if (userProfile.isFirstLaunch) {
                userProfile.nickname = "Friend"
                userProfile.isFirstLaunch = false
            }
            statusMessage = "Hi ${userProfile.nickname}, I'm ready!"

            // Permission Manager
            permissionManager = PermissionManager(this)
            requestRequiredPermissions()

            // Text-to-Speech Engine
            textToSpeechEngine = TextToSpeechEngine(this) {
                statusMessage = "TTS Engine Ready"
                textToSpeechEngine.speak(
                    "Hello ${userProfile.nickname}, I'm ready to help!",
                    TextToSpeechEngine.SupportedLanguage.ENGLISH
                )
            }

            // Device Controller (20+ commands)
            deviceController = DeviceController(this)

            // Chat Manager (with AI & SMS)
            chatManager = ChatManager(this)

            // Weather & Time Provider (Open-Meteo API)
            weatherTimeProvider = WeatherTimeProvider(this)
            initializeWeather()

            // Device Lock Manager (voice lock/unlock)
            deviceLockManager = DeviceLockManager(this)

            // Pointer Controller (mouse cursor)
            pointerController = PointerController(this)
            pointerController.setOnClickListener { x, y ->
                statusMessage = "Clicked at ($x, $y)"
            }

            // FIXED: Gesture Controller - only needs Context
            gestureController = GestureController(this)

            // FIXED: Hot Word Detector - only needs Context, use new signature
            hotWordDetector = HotWordDetector(this)
            hotWordDetector.startListening(
                hotWords = listOf("hey david", "ok david"),
                callback = { word ->
                    statusMessage = "Wake word detected: $word"
                    activateListeningMode()
                    textToSpeechEngine.speak(
                        "I'm listening...",
                        TextToSpeechEngine.SupportedLanguage.ENGLISH
                    )
                }
            )

            Log.d("MainActivity", "All components initialized successfully")
        } catch (e: Exception) {
            statusMessage = "Error: ${e.message}"
            Log.e("MainActivity", "Initialization error", e)
        }
    }

    /**
     * Initialize weather with Open-Meteo API
     */
    private fun initializeWeather() {
        lifecycleScope.launch {
            try {
                val weather = weatherTimeProvider.getWeatherVoiceReport()
                currentWeather = weather
                Log.d("MainActivity", "Weather: $weather")
            } catch (e: Exception) {
                Log.e("MainActivity", "Weather error", e)
                currentWeather = "Weather unavailable"
            }
        }
    }

    /**
     * Request required permissions
     */
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

    /**
     * Update permissions status
     */
    private fun updatePermissions() {
        devicePermissions = deviceAccess.getAccessStatus()
    }

    /**
     * Activate listening mode
     */
    private fun activateListeningMode() {
        isListening = true
        statusMessage = "Listening for command..."
    }

    /**
     * Handle gesture input
     * FIXED: Removed GestureType enum references
     */
    private fun handleGesture(gesture: String, details: String) {
        statusMessage = "Gesture: $gesture - $details"
        when (gesture) {
            "swipe_left" -> statusMessage = "Previous"
            "swipe_right" -> statusMessage = "Next"
            "single_tap" -> statusMessage = "Tap"
            "double_tap" -> statusMessage = "Double tap"
            "long_press" -> statusMessage = "Long press"
            else -> {}
        }
    }

    /**
     * Add message to chat history
     */
    private fun addChatMessage(message: String) {
        chatHistory = chatHistory + message
    }

    /**
     * Refresh time every second
     */
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
     * Main UI Screen
     */
    @Composable
    private fun DavidAIScreen() {
        TimeUpdater()  // Update time every second

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1F2937))
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🤖 DAVID AI",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00D4FF)
                )
                Text(
                    text = currentTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9CA3AF)
                )
            }

            // Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF374151))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9CA3AF)
                    )
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Weather Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF374151))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🌤 Weather",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9CA3AF)
                    )
                    Text(
                        text = currentWeather,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // User Info
            Text(
                text = "User: ${userProfile.nickname}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Quick Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        lifecycleScope.launch {
                            currentWeather = weatherTimeProvider.getWeatherVoiceReport()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4FF))
                ) {
                    Text("🌤 Weather", fontSize = 11.sp)
                }
                Button(
                    onClick = {
                        lifecycleScope.launch {
                            val forecast = weatherTimeProvider.getForecastVoiceReport(3)
                            textToSpeechEngine.speak(forecast, TextToSpeechEngine.SupportedLanguage.ENGLISH)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4FF))
                ) {
                    Text("📅 Forecast", fontSize = 11.sp)
                }
                Button(
                    onClick = { deviceLockManager.lockDevice() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
                ) {
                    Text("🔒 Lock", fontSize = 11.sp)
                }
            }

            // Chat History
            Text(
                text = "Chat History",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF111827), shape = MaterialTheme.shapes.small)
                    .padding(8.dp)
            ) {
                items(chatHistory.size) { index ->
                    Text(
                        text = chatHistory[index],
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        // FIXED: Proper signature
                        hotWordDetector.startListening(
                            hotWords = listOf("hey david", "ok david"),
                            callback = { word ->
                                statusMessage = "Detected: $word"
                            }
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4FF))
                ) {
                    Text("🎤 Listen", fontSize = 11.sp)
                }
                Button(
                    onClick = { pointerController.showPointer() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4FF))
                ) {
                    Text("🖱 Pointer", fontSize = 11.sp)
                }
            }
        }
    }

    @Composable
    private fun DavidAITheme(content: @Composable () -> Unit) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFF00D4FF),
                secondary = Color(0xFF9CA3AF)
            ),
            content = content
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        hotWordDetector.stopListening()  // FIXED: Use stopListening() instead of release()
        textToSpeechEngine.release()
        pointerController.release()
    }
}
