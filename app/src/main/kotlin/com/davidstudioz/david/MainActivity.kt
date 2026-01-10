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
import androidx.compose.runtime.*
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
import com.davidstudioz.david.ui.JarvisMainScreen
import com.davidstudioz.david.utils.DeviceResourceManager
import com.davidstudioz.david.voice.HotWordDetector
import com.davidstudioz.david.voice.TextToSpeechEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * DAVID AI - Main Activity with Jarvis-Style UI
 * Voice-First Android AI Assistant with Smart Resource Management
 * Features: 
 * - Futuristic Jarvis/Google Assistant UI
 * - Real-time RAM/Storage/CPU monitoring
 * - Smart model selection (50-60% resource limit)
 * - Voice Recognition with animated orb
 * - Device Control & Gesture Recognition
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
    private var statusMessage by mutableStateOf("Initializing D.A.V.I.D...")
    private var resourceStatus by mutableStateOf<DeviceResourceManager.ResourceStatus?>(null)

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.forEach { (permission, granted) ->
            Log.d("MainActivity", "$permission: $granted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize components
        initializeComponents()

        // Set up Jarvis-style UI
        setContent {
            JarvisUI()
        }

        // Start resource monitoring
        startResourceMonitoring()
    }

    /**
     * Initialize all DAVID AI components
     */
    private fun initializeComponents() {
        try {
            // Resource Manager - Monitor RAM/Storage/CPU
            resourceManager = DeviceResourceManager(this)
            val initialStatus = resourceManager.getResourceStatus()
            resourceStatus = initialStatus
            
            Log.d(TAG, "Device Resources:")
            Log.d(TAG, "RAM: ${initialStatus.usedRamMB / 1024}GB / ${initialStatus.totalRamMB / 1024}GB (${initialStatus.ramUsagePercent.toInt()}%)")
            Log.d(TAG, "Storage: ${initialStatus.usedStorageGB}GB / ${initialStatus.totalStorageGB}GB (${initialStatus.storageUsagePercent.toInt()}%)")
            Log.d(TAG, "CPU: ${initialStatus.cpuCores} cores (${initialStatus.cpuUsagePercent.toInt()}% usage)")
            Log.d(TAG, "Recommended Model: ${initialStatus.canUseForAI.recommendedModel.name}")
            Log.d(TAG, "Can Download: ${initialStatus.canUseForAI.canDownloadModel}")
            
            statusMessage = "System initialized. Model: ${initialStatus.canUseForAI.recommendedModel.name}"

            // Device Access Manager
            deviceAccess = DeviceAccessManager(this)

            // User Profile
            userProfile = UserProfile(this)
            if (userProfile.isFirstLaunch) {
                userProfile.nickname = "Sir"
                userProfile.isFirstLaunch = false
            }

            // Permission Manager
            permissionManager = PermissionManager(this)
            requestRequiredPermissions()

            // Text-to-Speech Engine
            textToSpeechEngine = TextToSpeechEngine(this) {
                statusMessage = "Voice systems online"
                textToSpeechEngine.speak(
                    "Welcome ${userProfile.nickname}. D.A.V.I.D. systems are online.",
                    TextToSpeechEngine.SupportedLanguage.ENGLISH
                )
            }

            // Device Controller
            deviceController = DeviceController(this)

            // Chat Manager
            chatManager = ChatManager(this)

            // Weather & Time Provider
            weatherTimeProvider = WeatherTimeProvider(this)

            // Device Lock Manager
            deviceLockManager = DeviceLockManager(this)

            // Pointer Controller
            pointerController = PointerController(this)

            // Gesture Controller
            gestureController = GestureController(this)

            // Hot Word Detector
            hotWordDetector = HotWordDetector(this)
            hotWordDetector.startListening(
                hotWords = listOf("hey david", "ok david", "jarvis"),
                callback = { word ->
                    statusMessage = "Wake word detected: $word"
                    activateListeningMode()
                    textToSpeechEngine.speak(
                        "Yes, Sir?",
                        TextToSpeechEngine.SupportedLanguage.ENGLISH
                    )
                }
            )

            Log.d(TAG, "All systems operational")
        } catch (e: Exception) {
            statusMessage = "Error: ${e.message}"
            Log.e(TAG, "Initialization error", e)
        }
    }

    /**
     * Start monitoring device resources every 2 seconds
     */
    private fun startResourceMonitoring() {
        lifecycleScope.launch {
            while (true) {
                try {
                    resourceStatus = resourceManager.getResourceStatus()
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating resources", e)
                }
                delay(2000) // Update every 2 seconds
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
     * Activate listening mode
     */
    private fun activateListeningMode() {
        isListening = true
        statusMessage = "Listening for command..."
        
        // Auto-stop after 5 seconds
        lifecycleScope.launch {
            delay(5000)
            isListening = false
            statusMessage = "Ready"
        }
    }

    /**
     * Jarvis-Style UI
     */
    @Composable
    private fun JarvisUI() {
        val currentResourceStatus = resourceStatus
        
        if (currentResourceStatus != null) {
            JarvisMainScreen(
                resourceStatus = currentResourceStatus,
                isListening = isListening,
                statusMessage = statusMessage,
                onVoiceClick = {
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
                onSettingsClick = {
                    statusMessage = "Settings"
                }
            )
        } else {
            // Loading screen
            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = androidx.compose.ui.graphics.Color(0xFF00E5FF)
                )
            }
        }
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
