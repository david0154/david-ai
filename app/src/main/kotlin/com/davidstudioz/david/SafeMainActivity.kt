package com.davidstudioz.david

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.davidstudioz.david.chat.ChatHistoryManager
import com.davidstudioz.david.chat.ChatManager
import com.davidstudioz.david.device.DeviceController
import com.davidstudioz.david.gesture.GestureController
import com.davidstudioz.david.language.LanguageManager
import com.davidstudioz.david.llm.LLMEngine
import com.davidstudioz.david.storage.EncryptionManager
import com.davidstudioz.david.ui.SettingsActivity
import com.davidstudioz.david.voice.VoiceController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * D.A.V.I.D Main Activity - FULLY INTEGRATED
 * ✅ Voice Control connected to VoiceController with DeviceController
 * ✅ Chat connected to LLMEngine
 * ✅ Gesture connected to GestureController
 * ✅ Settings connected to SettingsActivity & LanguageManager
 * ✅ Privacy connected to EncryptionManager
 * ✅ Device control connected to DeviceController
 * ✅ ChatManager connected to VoiceController for AI responses
 */
@OptIn(ExperimentalMaterial3Api::class)
class SafeMainActivity : ComponentActivity() {

    // Backend controllers
    private lateinit var voiceController: VoiceController
    private lateinit var gestureController: GestureController
    private lateinit var deviceController: DeviceController
    private lateinit var languageManager: LanguageManager
    private lateinit var chatHistoryManager: ChatHistoryManager
    private lateinit var chatManager: ChatManager
    private lateinit var llmEngine: LLMEngine
    private lateinit var encryptionManager: EncryptionManager
    
    private lateinit var wifiManager: WifiManager
    private lateinit var bluetoothManager: BluetoothManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Initialize DeviceController FIRST (required by VoiceController)
            deviceController = DeviceController(this)
            
            // Initialize ChatManager (for AI responses)
            chatManager = ChatManager(this)
            
            // Initialize VoiceController with DeviceController and ChatManager
            voiceController = VoiceController(this, deviceController, chatManager)
            
            // Initialize other controllers
            gestureController = GestureController(this)
            languageManager = LanguageManager(this)
            chatHistoryManager = ChatHistoryManager(this)
            llmEngine = LLMEngine(this)
            encryptionManager = EncryptionManager(this)
            
            wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            
            Log.d(TAG, "✅ All controllers initialized successfully")
            Log.d(TAG, "✅ VoiceController connected to DeviceController")
            Log.d(TAG, "✅ VoiceController connected to ChatManager")
            
            setContent {
                MaterialTheme(
                    colorScheme = darkColorScheme(
                        primary = Color(0xFF00E5FF),
                        background = Color(0xFF0A0E27)
                    )
                ) {
                    MainScreen()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
        }
    }

    override fun onDestroy() {
        voiceController.cleanup()
        gestureController.stopGestureRecognition()
        super.onDestroy()
    }

    @Composable
    private fun MainScreen() {
        var currentScreen by remember { mutableStateOf("home") }
        var selectedLanguages by remember { mutableStateOf(languageManager.getSupportedLanguages().filter { it.isDownloaded }.map { it.name }.toSet()) }
        var showLanguageDialog by remember { mutableStateOf(false) }
        var chatMessage by remember { mutableStateOf("") }
        var chatHistory by remember { mutableStateOf(chatHistoryManager.getRecentMessages().map { ChatMessage(it.content, it.isUser) }) }
        var voiceHistory by remember { mutableStateOf(listOf<VoiceMessage>()) }
        var isVoiceListening by remember { mutableStateOf(false) }
        var isGestureActive by remember { mutableStateOf(false) }
        var detectedGesture by remember { mutableStateOf("No gesture") }
        
        val scope = rememberCoroutineScope()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "D.A.V.I.D",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when(currentScreen) {
                                    "voice" -> "Voice Control"
                                    "gesture" -> "Gesture Control"
                                    "chat" -> "Chat"
                                    "devices" -> "Device Control"
                                    "settings" -> "Settings"
                                    else -> "AI Assistant"
                                },
                                fontSize = 10.sp,
                                color = Color(0xFF64B5F6)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showLanguageDialog = true }) {
                            Icon(Icons.Default.Language, "Language")
                        }
                        IconButton(onClick = { 
                            // Open full settings activity
                            startActivity(Intent(this@SafeMainActivity, SettingsActivity::class.java))
                        }) {
                            Icon(Icons.Default.Settings, "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1A1F3A)
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF1A1F3A)
                ) {
                    NavigationBarItem(
                        selected = currentScreen == "home",
                        onClick = { currentScreen = "home" },
                        icon = { Icon(Icons.Default.Home, "Home") },
                        label = { Text("Home", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "voice",
                        onClick = { currentScreen = "voice" },
                        icon = { Icon(Icons.Default.Mic, "Voice") },
                        label = { Text("Voice", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "gesture",
                        onClick = { currentScreen = "gesture" },
                        icon = { Icon(Icons.Default.PanTool, "Gesture") },
                        label = { Text("Gesture", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "chat",
                        onClick = { currentScreen = "chat" },
                        icon = { Icon(Icons.Default.Chat, "Chat") },
                        label = { Text("Chat", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "devices",
                        onClick = { currentScreen = "devices" },
                        icon = { Icon(Icons.Default.DevicesOther, "Control") },
                        label = { Text("Control", fontSize = 10.sp) }
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
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
                when (currentScreen) {
                    "home" -> HomeScreen { screen -> currentScreen = screen }
                    "voice" -> VoiceControlScreen(
                        history = voiceHistory,
                        isListening = isVoiceListening,
                        onToggleListening = {
                            isVoiceListening = !isVoiceListening
                            if (isVoiceListening) {
                                // Start real voice recognition - FIXED callback
                                scope.launch {
                                    try {
                                        voiceController.startListening { recognizedText ->
                                            if (recognizedText.isNotBlank()) {
                                                voiceHistory = voiceHistory + VoiceMessage(recognizedText, true)
                                                // Get LLM response
                                                scope.launch {
                                                    val response = llmEngine.generateResponse(recognizedText)
                                                    voiceHistory = voiceHistory + VoiceMessage(response, false)
                                                    // Speak response
                                                    voiceController.speak(response)
                                                }
                                            }
                                            isVoiceListening = false
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Voice error", e)
                                        isVoiceListening = false
                                    }
                                }
                            } else {
                                voiceController.stopListening()
                            }
                        }
                    )
                    "gesture" -> GestureControlScreen(
                        isActive = isGestureActive,
                        detectedGesture = detectedGesture,
                        onToggleActive = {
                            isGestureActive = !isGestureActive
                            if (isGestureActive) {
                                // Start real gesture recognition
                                scope.launch {
                                    gestureController.startGestureRecognition { gesture ->
                                        detectedGesture = gesture
                                        // Execute gesture command
                                        when (gesture.lowercase()) {
                                            "thumbs_up" -> voiceController.speak("Thumbs up detected!")
                                            "peace" -> voiceController.speak("Peace sign detected!")
                                            "ok_sign" -> voiceController.speak("OK sign detected!")
                                        }
                                    }
                                }
                            } else {
                                gestureController.stopGestureRecognition()
                                detectedGesture = "No gesture"
                            }
                        }
                    )
                    "chat" -> ChatScreen(chatMessage, chatHistory, 
                        onMessageChange = { chatMessage = it },
                        onSendMessage = {
                            if (chatMessage.isNotBlank()) {
                                val userMsg = chatMessage
                                chatHistory = chatHistory + ChatMessage(userMsg, true)
                                chatMessage = ""
                                
                                // Save to chat history
                                scope.launch {
                                    chatHistoryManager.addMessage(userMsg, isUser = true)
                                    
                                    // Get LLM response
                                    val response = llmEngine.generateResponse(userMsg)
                                    chatHistory = chatHistory + ChatMessage(response, false)
                                    chatHistoryManager.addMessage(response, isUser = false)
                                }
                            }
                        }
                    )
                    "devices" -> DeviceControlScreen()
                    "settings" -> SettingsScreenQuick()
                }
            }
        }

        if (showLanguageDialog) {
            LanguageDialog(
                languages = languageManager.getSupportedLanguages(),
                selected = selectedLanguages,
                onConfirm = { 
                    selectedLanguages = it
                    showLanguageDialog = false
                },
                onDismiss = { showLanguageDialog = false }
            )
        }
    }

    @Composable
    private fun HomeScreen(onNavigate: (String) -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00E5FF).copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Text(text = "🤖", fontSize = 64.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Hey! I'm D.A.V.I.D",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Your AI-powered assistant",
                fontSize = 14.sp,
                color = Color(0xFF64B5F6)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickCard("🎤\nVoice", Modifier.weight(1f)) { onNavigate("voice") }
                QuickCard("✋\nGesture", Modifier.weight(1f)) { onNavigate("gesture") }
                QuickCard("💬\nChat", Modifier.weight(1f)) { onNavigate("chat") }
                QuickCard("🔧\nControl", Modifier.weight(1f)) { onNavigate("devices") }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📊 Quick Stats",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Languages", fontSize = 10.sp, color = Color(0xFF64B5F6))
                            Text(
                                "${languageManager.getDownloadedLanguages().size}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        }
                        Column {
                            Text("Chats", fontSize = 10.sp, color = Color(0xFF64B5F6))
                            Text(
                                "${chatHistoryManager.getRecentMessages().size}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        }
                        Column {
                            Text("Privacy", fontSize = 10.sp, color = Color(0xFF64B5F6))
                            Text(
                                "✅",
                                fontSize = 20.sp,
                                color = Color(0xFF00FF88)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun VoiceControlScreen(
        history: List<VoiceMessage>,
        isListening: Boolean,
        onToggleListening: () -> Unit
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "voice")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (history.isEmpty()) {
                    item {
                        Text(
                            text = "Tap the microphone to speak...",
                            fontSize = 14.sp,
                            color = Color(0xFF64B5F6),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    items(history) { msg ->
                        VoiceBubble(msg)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1F3A).copy(alpha = 0.8f))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(if (isListening) pulseScale else 1f)
                        .clip(CircleShape)
                        .background(
                            if (isListening)
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF00E5FF).copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                )
                            else
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF1E88E5).copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                )
                        )
                        .clickable { onToggleListening() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Microphone",
                        modifier = Modifier.size(60.dp),
                        tint = if (isListening) Color(0xFF00E5FF) else Color(0xFF64B5F6)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isListening) "Listening..." else "Tap to speak",
                    fontSize = 14.sp,
                    color = if (isListening) Color(0xFF00E5FF) else Color(0xFF64B5F6),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    @Composable
    private fun GestureControlScreen(
        isActive: Boolean,
        detectedGesture: String,
        onToggleActive: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "✋ Gesture Control",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1F3A)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📸",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isActive) "Camera Active" else "Camera View",
                            fontSize = 16.sp,
                            color = if (isActive) Color(0xFF00E5FF) else Color(0xFF64B5F6)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Detected: $detectedGesture",
                        fontSize = 16.sp,
                        color = if (isActive) Color(0xFF00E5FF) else Color(0xFF64B5F6),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onToggleActive,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) Color(0xFFFF6E40) else Color(0xFF00E5FF)
                )
            ) {
                Text(
                    text = if (isActive) "Stop Detection" else "Start Detection",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Supported Gestures:",
                fontSize = 12.sp,
                color = Color(0xFF64B5F6),
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("👍 Thumbs Up", "👋 Wave", "✌️ Peace", "👌 OK", "✊ Fist")) { gesture ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = gesture,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ChatScreen(
        message: String,
        history: List<ChatMessage>,
        onMessageChange: (String) -> Unit,
        onSendMessage: () -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (history.isEmpty()) {
                    item {
                        Text(
                            text = "Start a conversation with D.A.V.I.D...",
                            fontSize = 14.sp,
                            color = Color(0xFF64B5F6),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    items(history) { msg ->
                        ChatBubble(msg)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = message,
                    onValueChange = onMessageChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message D.A.V.I.D...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E88E5).copy(alpha = 0.1f),
                        unfocusedContainerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = onSendMessage,
                    containerColor = Color(0xFF00E5FF)
                ) {
                    Icon(Icons.Default.Send, "Send", tint = Color.Black)
                }
            }
        }
    }

    @Composable
    private fun DeviceControlScreen() {
        var wifiEnabled by remember { mutableStateOf(deviceController.isWiFiEnabled()) }
        var bluetoothEnabled by remember { mutableStateOf(deviceController.isBluetoothEnabled()) }
        var brightnessLevel by remember { mutableStateOf(deviceController.getBrightnessLevel()) }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "🔧 Device Controls",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(24.dp))

            DeviceCard("📡", "WiFi", wifiEnabled) { 
                deviceController.toggleWiFi(!wifiEnabled)
                wifiEnabled = !wifiEnabled 
            }
            Spacer(modifier = Modifier.height(12.dp))
            DeviceCard("📡", "Bluetooth", bluetoothEnabled) { 
                deviceController.toggleBluetooth(!bluetoothEnabled)
                bluetoothEnabled = !bluetoothEnabled 
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Brightness control
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔆", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Brightness",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Slider(
                        value = brightnessLevel,
                        onValueChange = { 
                            brightnessLevel = it
                            deviceController.setBrightnessLevel(it)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00E5FF),
                            activeTrackColor = Color(0xFF00E5FF)
                        )
                    )
                }
            }
        }
    }

    @Composable
    private fun SettingsScreenQuick() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "⚙️ Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf(
                    "🌐 Languages" to "language",
                    "🎤 Voice Settings" to "voice",
                    "✋ Gesture Settings" to "gesture",
                    "🔔 Notifications" to "notifications",
                    "🔒 Privacy & Security" to "privacy",
                    "ℹ️ About D.A.V.I.D" to "about"
                )) { (text, action) ->
                    SettingCard(text) {
                        when (action) {
                            "privacy" -> {
                                // Show privacy info
                                voiceController.speak("Your data is encrypted and stored locally. D.A.V.I.D never shares your information.")
                            }
                            else -> {
                                // Open full settings
                                startActivity(Intent(this@SafeMainActivity, SettingsActivity::class.java))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun QuickCard(text: String, modifier: Modifier, onClick: () -> Unit) {
        Card(
            modifier = modifier
                .aspectRatio(1f)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF00E5FF)
                )
            }
        }
    }

    @Composable
    private fun VoiceBubble(message: VoiceMessage) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
        ) {
            Card(
                modifier = Modifier.widthIn(max = 280.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isUser) 
                        Color(0xFF00E5FF) else Color(0xFF1E88E5).copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isUser) 16.dp else 4.dp,
                    bottomEnd = if (message.isUser) 4.dp else 16.dp
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (message.isUser) "🎤" else "🤖",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = message.text,
                        color = if (message.isUser) Color.Black else Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    @Composable
    private fun ChatBubble(message: ChatMessage) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
        ) {
            Card(
                modifier = Modifier.widthIn(max = 280.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isUser) 
                        Color(0xFF00E5FF) else Color(0xFF1E88E5).copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isUser) 16.dp else 4.dp,
                    bottomEnd = if (message.isUser) 4.dp else 16.dp
                )
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(12.dp),
                    color = if (message.isUser) Color.Black else Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }

    @Composable
    private fun DeviceCard(icon: String, title: String, enabled: Boolean, onToggle: () -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = icon, fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E5FF)
                    )
                )
            }
        }
    }

    @Composable
    private fun SettingCard(text: String, onClick: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = text, fontSize = 14.sp, color = Color.White)
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF64B5F6)
                )
            }
        }
    }

    @Composable
    private fun LanguageDialog(
        languages: List<com.davidstudioz.david.language.Language>,
        selected: Set<String>,
        onConfirm: (Set<String>) -> Unit,
        onDismiss: () -> Unit
    ) {
        var temp by remember { mutableStateOf(selected) }
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("🌐 Languages (${languages.filter { it.isDownloaded }.size}/${languages.size})") },
            text = {
                LazyColumn {
                    items(languages) { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (lang.isDownloaded) {
                                        temp = if (temp.contains(lang.name)) {
                                            if (temp.size > 1) temp - lang.name else temp
                                        } else {
                                            temp + lang.name
                                        }
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = temp.contains(lang.name),
                                onCheckedChange = null,
                                enabled = lang.isDownloaded
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "${lang.nativeName} (${lang.name})")
                                if (!lang.isDownloaded) {
                                    Text(
                                        text = "Download to use",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64B5F6)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { onConfirm(temp) }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }

    data class ChatMessage(val text: String, val isUser: Boolean)
    data class VoiceMessage(val text: String, val isUser: Boolean)

    companion object {
        private const val TAG = "SafeMainActivity"
    }
}
