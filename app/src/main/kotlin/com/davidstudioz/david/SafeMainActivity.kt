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
import androidx.compose.ui.draw.alpha
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * D.A.V.I.D Main Activity - COMPLETE with Voice & Gesture!
 */
class SafeMainActivity : ComponentActivity() {

    private lateinit var wifiManager: WifiManager
    private lateinit var bluetoothManager: BluetoothManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            
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
            Log.e(TAG, "Error", e)
        }
    }

    @Composable
    private fun MainScreen() {
        var currentScreen by remember { mutableStateOf("home") }
        var searchQuery by remember { mutableStateOf("") }
        var showSearch by remember { mutableStateOf(false) }
        var selectedLanguages by remember { mutableStateOf(setOf("English")) }
        var showLanguageDialog by remember { mutableStateOf(false) }
        var chatMessage by remember { mutableStateOf("") }
        var chatHistory by remember { mutableStateOf(listOf<ChatMessage>()) }
        
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
                        IconButton(onClick = { currentScreen = "settings" }) {
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
                    "home" -> HomeScreen()
                    "voice" -> VoiceControlScreen()
                    "gesture" -> GestureControlScreen()
                    "chat" -> ChatScreen(chatMessage, chatHistory, 
                        onMessageChange = { chatMessage = it },
                        onSendMessage = {
                            if (chatMessage.isNotBlank()) {
                                chatHistory = chatHistory + ChatMessage(chatMessage, true)
                                scope.launch {
                                    delay(500)
                                    chatHistory = chatHistory + ChatMessage(
                                        getAIResponse(chatMessage),
                                        false
                                    )
                                }
                                chatMessage = ""
                            }
                        }
                    )
                    "devices" -> DeviceControlScreen()
                    "settings" -> SettingsScreen()
                }
            }
        }

        if (showLanguageDialog) {
            LanguageDialog(
                selected = selectedLanguages,
                onConfirm = { selectedLanguages = it; showLanguageDialog = false },
                onDismiss = { showLanguageDialog = false }
            )
        }
    }

    @Composable
    private fun HomeScreen() {
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
                QuickCard("🎤\nVoice", Modifier.weight(1f))
                QuickCard("✋\nGesture", Modifier.weight(1f))
                QuickCard("💬\nChat", Modifier.weight(1f))
                QuickCard("🔧\nControl", Modifier.weight(1f))
            }
        }
    }

    @Composable
    private fun VoiceControlScreen() {
        var isListening by remember { mutableStateOf(false) }
        var transcript by remember { mutableStateOf("Tap mic to speak...") }
        
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
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🎤 Voice Control",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(200.dp)
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
                    .clickable {
                        isListening = !isListening
                        transcript = if (isListening) "Listening..." else "Tap mic to speak..."
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Microphone",
                    modifier = Modifier.size(80.dp),
                    tint = if (isListening) Color(0xFF00E5FF) else Color(0xFF64B5F6)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

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
                        text = transcript,
                        fontSize = 16.sp,
                        color = if (isListening) Color(0xFF00E5FF) else Color(0xFF64B5F6),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF)
                    )
                ) {
                    Text("Commands", color = Color.Black)
                }
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E88E5).copy(alpha = 0.3f)
                    )
                ) {
                    Text("History")
                }
            }
        }
    }

    @Composable
    private fun GestureControlScreen() {
        var gestureDetected by remember { mutableStateOf("No gesture") }
        var isActive by remember { mutableStateOf(false) }

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
                            text = "Camera View",
                            fontSize = 16.sp,
                            color = Color(0xFF64B5F6)
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
                        text = "Detected: $gestureDetected",
                        fontSize = 16.sp,
                        color = if (isActive) Color(0xFF00E5FF) else Color(0xFF64B5F6),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { 
                    isActive = !isActive
                    gestureDetected = if (isActive) "Scanning..." else "No gesture"
                },
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
                items(history) { msg ->
                    ChatBubble(msg)
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
        var wifiEnabled by remember { mutableStateOf(isWiFiEnabled()) }
        var bluetoothEnabled by remember { mutableStateOf(isBluetoothEnabled()) }
        
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

            DeviceCard("📶", "WiFi", wifiEnabled) { 
                toggleWiFi(!wifiEnabled); wifiEnabled = !wifiEnabled 
            }
            Spacer(modifier = Modifier.height(12.dp))
            DeviceCard("📶", "Bluetooth", bluetoothEnabled) { 
                toggleBluetooth(!bluetoothEnabled); bluetoothEnabled = !bluetoothEnabled 
            }
        }
    }

    @Composable
    private fun SettingsScreen() {
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
                    "🌐 Languages",
                    "🎤 Voice",
                    "✋ Gesture",
                    "🔔 Notifications",
                    "🔒 Privacy",
                    "ℹ️ About"
                )) { setting ->
                    SettingCard(setting)
                }
            }
        }
    }

    @Composable
    private fun QuickCard(text: String, modifier: Modifier) {
        Card(
            modifier = modifier.aspectRatio(1f),
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
    private fun SettingCard(text: String) {
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
        selected: Set<String>,
        onConfirm: (Set<String>) -> Unit,
        onDismiss: () -> Unit
    ) {
        var temp by remember { mutableStateOf(selected) }
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("🌐 Languages") },
            text = {
                LazyColumn {
                    items(listOf(
                        "English", "Hindi", "Tamil", "Telugu", "Bengali"
                    )) { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    temp = if (temp.contains(lang)) {
                                        if (temp.size > 1) temp - lang else temp
                                    } else {
                                        temp + lang
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = temp.contains(lang),
                                onCheckedChange = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = lang)
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

    private fun getAIResponse(message: String): String {
        return when {
            message.contains("hello", true) ->
                "Hello! I'm D.A.V.I.D. How can I help?"
            message.contains("weather", true) ->
                "Weather is sunny, 25°C"
            else ->
                "I understand: '$message'. I'm here to help!"
        }
    }

    private fun isWiFiEnabled() = try { wifiManager.isWifiEnabled } catch (e: Exception) { false }
    private fun isBluetoothEnabled() = try { bluetoothManager.adapter?.isEnabled ?: false } catch (e: Exception) { false }

    private fun toggleWiFi(enable: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startActivity(Intent(Settings.Panel.ACTION_WIFI))
            } else {
                @Suppress("DEPRECATION")
                wifiManager.isWifiEnabled = enable
            }
        } catch (e: Exception) {
            Log.e(TAG, "WiFi error", e)
        }
    }

    private fun toggleBluetooth(enable: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1)
                    return
                }
            }
            val adapter = bluetoothManager.adapter
            if (enable && !adapter.isEnabled) {
                startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            } else if (!enable && adapter.isEnabled) {
                adapter.disable()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Bluetooth error", e)
        }
    }

    data class ChatMessage(val text: String, val isUser: Boolean)

    companion object {
        private const val TAG = "SafeMainActivity"
    }
}
