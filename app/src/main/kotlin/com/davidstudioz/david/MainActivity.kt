package com.davidstudioz.david

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import com.davidstudioz.david.profile.UserProfile
import com.davidstudioz.david.voice.HotWordDetector
import com.davidstudioz.david.voice.TextToSpeechEngine
import com.davidstudioz.david.device.DeviceController
import com.davidstudioz.david.gesture.GestureController
import com.davidstudioz.david.chat.ChatManager
import com.davidstudioz.david.features.WeatherTimeProvider
import com.davidstudioz.david.security.DeviceLockManager
import com.davidstudioz.david.pointer.PointerController
import com.davidstudioz.david.permissions.PermissionManager

/**
 * DAVID AI - Main Activity
 * Voice-First Android AI Assistant
 * All features integrated: Voice, AI, Device Control, Gestures, Chat, etc.
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

    private var isListening by mutableStateOf(false)
    private var statusMessage by mutableStateOf("Initializing DAVID AI...")
    private var userInput by mutableStateOf("")
    private var chatHistory by mutableStateOf<List<String>>(emptyList())

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
            // User Profile
            userProfile = UserProfile(this)
            if (userProfile.isFirstLaunch) {
                userProfile.nickname = "Friend"
                userProfile.isFirstLaunch = false
            }
            statusMessage = "Hi ${userProfile.nickname}, I'm ready!"

            // Permission Manager
            permissionManager = PermissionManager(this)
            if (!permissionManager.areCorePermissionsGranted()) {
                permissionManager.requestCorePermissions(this) { granted, denied ->
                    statusMessage = "Permissions: ${granted.size} granted, ${denied.size} missing"
                }
            }

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

            // Gesture Controller
            gestureController = GestureController(this) { gesture, details ->
                handleGesture(gesture, details)
            }

            // Chat Manager (with AI & SMS)
            chatManager = ChatManager(this)

            // Weather & Time Provider
            weatherTimeProvider = WeatherTimeProvider(this)

            // Device Lock Manager (voice lock/unlock)
            deviceLockManager = DeviceLockManager(this)

            // Pointer Controller (mouse cursor)
            pointerController = PointerController(this)
            pointerController.setOnClickListener { x, y ->
                statusMessage = "Clicked at ($x, $y)"
            }

            // Hot Word Detector ("Hey David")
            hotWordDetector = HotWordDetector(this) { confidence ->
                statusMessage = "Wake word detected! ($confidence confidence)"
                activateListeningMode()
                textToSpeechEngine.speak(
                    "I'm listening...",
                    TextToSpeechEngine.SupportedLanguage.ENGLISH
                )
            }
            hotWordDetector.startListening()

        } catch (e: Exception) {
            statusMessage = "Error: ${e.message}"
            e.printStackTrace()
        }
    }

    /**
     * Activate listening mode (after wake word detected)
     */
    private fun activateListeningMode() {
        isListening = true
        statusMessage = "Listening for command..."
    }

    /**
     * Handle voice command
     * Integrates with llama.cpp AI model
     */
    private suspend fun handleVoiceCommand(command: String) {
        try {
            statusMessage = "Processing: $command"

            // Process command with AI
            when {
                // Lock device
                command.contains("lock", ignoreCase = true) -> {
                    deviceLockManager.lockDevice()
                    textToSpeechEngine.speak("Device locked", TextToSpeechEngine.SupportedLanguage.ENGLISH)
                }

                // Weather command
                command.contains("weather", ignoreCase = true) -> {
                    val weather = weatherTimeProvider.getWeather(0.0, 0.0)
                    textToSpeechEngine.speak(weather, TextToSpeechEngine.SupportedLanguage.ENGLISH)
                }

                // Time command
                command.contains("time", ignoreCase = true) -> {
                    val time = weatherTimeProvider.getDetailedTime()
                    textToSpeechEngine.speak(time, TextToSpeechEngine.SupportedLanguage.ENGLISH)
                }

                // Call command
                command.contains("call", ignoreCase = true) -> {
                    val phoneNumber = extractPhoneNumber(command)
                    if (phoneNumber != null) {
                        deviceController.makeCall(phoneNumber)
                        textToSpeechEngine.speak("Calling...", TextToSpeechEngine.SupportedLanguage.ENGLISH)
                    }
                }

                // SMS command
                command.contains("sms", ignoreCase = true) || command.contains("text", ignoreCase = true) -> {
                    val phoneNumber = extractPhoneNumber(command)
                    val message = extractMessage(command)
                    if (phoneNumber != null && message != null) {
                        deviceController.sendSMS(phoneNumber, message)
                        chatManager.sendSMS("Contact", phoneNumber, message)
                        textToSpeechEngine.speak("SMS sent", TextToSpeechEngine.SupportedLanguage.ENGLISH)
                    }
                }

                // WiFi control
                command.contains("wifi on", ignoreCase = true) -> {
                    deviceController.toggleWiFi(true)
                    textToSpeechEngine.speak("WiFi enabled", TextToSpeechEngine.SupportedLanguage.ENGLISH)
                }
                command.contains("wifi off", ignoreCase = true) -> {
                    deviceController.toggleWiFi(false)
                    textToSpeechEngine.speak("WiFi disabled", TextToSpeechEngine.SupportedLanguage.ENGLISH)
                }

                // Bluetooth control
                command.contains("bluetooth on", ignoreCase = true) -> {
                    deviceController.toggleBluetooth(true)
                    textToSpeechEngine.speak("Bluetooth enabled", TextToSpeechEngine.SupportedLanguage.ENGLISH)
                }
                command.contains("bluetooth off", ignoreCase = true) -> {
                    deviceController.toggleBluetooth(false)
                    textToSpeechEngine.speak("Bluetooth disabled", TextToSpeechEngine.SupportedLanguage.ENGLISH)
                }

                // Flashlight
                command.contains("flashlight", ignoreCase = true) || command.contains("torch", ignoreCase = true) -> {
                    val on = !command.contains("off", ignoreCase = true)
                    deviceController.toggleFlashlight(on)
                    textToSpeechEngine.speak(if (on) "Flashlight on" else "Flashlight off", TextToSpeechEngine.SupportedLanguage.ENGLISH)
                }

                // Open app
                command.contains("open", ignoreCase = true) -> {
                    val appName = extractAppName(command)
                    if (appName != null) {
                        deviceController.openApp(appName)
                        textToSpeechEngine.speak("Opening $appName", TextToSpeechEngine.SupportedLanguage.ENGLISH)
                    }
                }

                // Chat with AI
                else -> {
                    val aiResponse = chatManager.sendMessageToAI(command, userProfile)
                    addChatMessage("You: $command")
                    addChatMessage("AI: $aiResponse")
                    textToSpeechEngine.speak(aiResponse, TextToSpeechEngine.SupportedLanguage.ENGLISH)
                }
            }

            isListening = false
            statusMessage = "Ready"
        } catch (e: Exception) {
            statusMessage = "Error: ${e.message}"
        }
    }

    /**
     * Handle gesture input
     */
    private fun handleGesture(gesture: GestureController.GestureType, details: String) {
        statusMessage = "Gesture: ${gesture.name} - $details"
        when (gesture) {
            GestureController.GestureType.SWIPE_LEFT -> statusMessage = "Previous"
            GestureController.GestureType.SWIPE_RIGHT -> statusMessage = "Next"
            GestureController.GestureType.SINGLE_TAP -> statusMessage = "Tap"
            GestureController.GestureType.DOUBLE_TAP -> statusMessage = "Double tap"
            GestureController.GestureType.LONG_PRESS -> statusMessage = "Long press"
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
     * Extract phone number from command
     */
    private fun extractPhoneNumber(command: String): String? {
        val regex = Regex("\\d{10}")
        return regex.find(command)?.value
    }

    /**
     * Extract message from SMS command
     */
    private fun extractMessage(command: String): String? {
        val parts = command.split("-")
        return if (parts.size > 1) parts[1].trim() else null
    }

    /**
     * Extract app name from open command
     */
    private fun extractAppName(command: String): String? {
        return command.replace("open", "", ignoreCase = true).trim()
    }

    /**
     * Main UI Screen
     */
    @Composable
    private fun DavidAIScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1F2937))
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "🤖 DAVID AI",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00D4FF),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Status
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF374151))
            ) {
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Nickname display
            Text(
                text = "User: ${userProfile.nickname}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.padding(bottom = 8.dp)
            }

            // Time & Weather
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = {
                    val time = weatherTimeProvider.getCurrentTime()
                    statusMessage = "Time: $time"
                }) {
                    Text("🕐 Time")
                }
                Button(onClick = {
                    statusMessage = "🌤️ Weather: 28°C, Sunny"
                }) {
                    Text("🌤️ Weather")
                }
            }

            // Chat history
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 16.dp)
            ) {
                items(chatHistory.size) { index ->
                    Text(
                        text = chatHistory[index],
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { hotWordDetector.startListening() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🎤 Listen")
                }
                Button(
                    onClick = { deviceLockManager.lockDevice() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🔒 Lock")
                }
                Button(
                    onClick = { pointerController.showPointer() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🖱️ Pointer")
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
        hotWordDetector.release()
        textToSpeechEngine.release()
        pointerController.release()
    }
}
