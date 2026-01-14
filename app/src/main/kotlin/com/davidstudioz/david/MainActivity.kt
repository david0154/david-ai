package com.davidstudioz.david

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.davidstudioz.david.chat.ChatManager
import com.davidstudioz.david.ui.ChatScreen
import com.davidstudioz.david.ui.JarvisMainScreen
import com.davidstudioz.david.ui.SettingsActivity
import com.davidstudioz.david.ui.theme.DAVIDTheme
import com.davidstudioz.david.utils.DeviceResourceManager
import com.davidstudioz.david.voice.TextToSpeechEngine
import kotlinx.coroutines.launch

/**
 * MainActivity - WITH CHAT SCREEN & COPY/PASTE
 * ✅ Chat UI with message list
 * ✅ Copy/paste functionality
 * ✅ Voice input
 * ✅ Text input
 * ✅ Settings
 */
class MainActivity : ComponentActivity() {

    private lateinit var chatManager: ChatManager
    private lateinit var ttsEngine: TextToSpeechEngine
    private lateinit var resourceManager: DeviceResourceManager
    private var speechRecognizer: SpeechRecognizer? = null

    private var isListening by mutableStateOf(false)
    private var statusMessage by mutableStateOf("Ready")
    private var resourceStatus by mutableStateOf(
        DeviceResourceManager.ResourceStatus(
            totalRamMB = 0,
            usedRamMB = 0,
            ramUsagePercent = 0f,
            totalStorageGB = 0,
            usedStorageGB = 0,
            storageUsagePercent = 0f,
            cpuUsagePercent = 0f,
            cpuCores = 0,
            canUseForAI = DeviceResourceManager.AIRecommendation(
                canRun = false,
                recommendedModel = DeviceResourceManager.ModelType.TINY,
                reason = "Checking..."
            )
        )
    )
    private var messages by mutableStateOf<List<com.davidstudioz.david.chat.ChatMessage>>(emptyList())
    private var showChatScreen by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceRecognition()
        } else {
            Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize components
        chatManager = ChatManager(this)
        ttsEngine = TextToSpeechEngine(this)
        resourceManager = DeviceResourceManager(this)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        // Load chat history
        chatManager.loadChatHistory()
        messages = chatManager.getMessages()

        // Monitor resources
        lifecycleScope.launch {
            resourceStatus = resourceManager.getResourceStatus()
        }

        setContent {
            DAVIDTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0A0E27)
                ) {
                    if (showChatScreen) {
                        ChatScreenWrapper()
                    } else {
                        MainScreenWrapper()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreenWrapper() {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("D.A.V.I.D") },
                    actions = {
                        IconButton(onClick = { showChatScreen = true }) {
                            Icon(Icons.Default.Chat, "Chat")
                        }
                        IconButton(onClick = { openSettings() }) {
                            Icon(Icons.Default.Settings, "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1A1F3A),
                        titleContentColor = Color(0xFF00E5FF)
                    )
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                JarvisMainScreen(
                    resourceStatus = resourceStatus,
                    isListening = isListening,
                    statusMessage = statusMessage,
                    onVoiceClick = { handleVoiceInput() }
                )
            }
        }
    }

    @Composable
    fun ChatScreenWrapper() {
        ChatScreen(
            messages = messages,
            onSendMessage = { message ->
                sendMessage(message)
            },
            onVoiceInput = { handleVoiceInput() },
            onClearChat = {
                chatManager.clearHistory()
                messages = emptyList()
            }
        )
    }

    private fun handleVoiceInput() {
        if (checkMicrophonePermission()) {
            if (isListening) {
                stopVoiceRecognition()
            } else {
                startVoiceRecognition()
            }
        } else {
            requestMicrophonePermission()
        }
    }

    private fun sendMessage(text: String) {
        lifecycleScope.launch {
            try {
                statusMessage = "Thinking..."
                val response = chatManager.sendMessage(text)
                messages = chatManager.getMessages()
                
                // Speak response
                ttsEngine.speak(response.text)
                statusMessage = "Ready"
            } catch (e: Exception) {
                statusMessage = "Error: ${e.message}"
                Log.e(TAG, "Error sending message", e)
            }
        }
    }

    private fun checkMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestMicrophonePermission() {
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun startVoiceRecognition() {
        isListening = true
        statusMessage = "Listening..."

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
            }

            override fun onError(error: Int) {
                isListening = false
                statusMessage = "Voice error"
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    sendMessage(text)
                }
                isListening = false
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    private fun stopVoiceRecognition() {
        speechRecognizer?.stopListening()
        isListening = false
        statusMessage = "Ready"
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
        ttsEngine.shutdown()
        chatManager.release()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}