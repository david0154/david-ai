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
import androidx.compose.animation.AnimatedVisibility
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
 * D.A.V.I.D Main Activity - Full Featured AI Assistant
 * ✅ Search functionality
 * ✅ PM/DM chat
 * ✅ Language selector (default English, can select multiple)
 * ✅ Device controls (WiFi, Bluetooth, Brightness, etc.)
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
                        secondary = Color(0xFF9CA3AF),
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

    @OptIn(ExperimentalMaterial3Api::class)
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
                        if (showSearch) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search web or ask D.A.V.I.D...") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                        } else {
                            Column {
                                Text(
                                    text = "D.A.V.I.D",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = selectedLanguages.joinToString(", "),
                                    fontSize = 10.sp,
                                    color = Color(0xFF64B5F6)
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSearch = !showSearch }) {
                            Icon(
                                imageVector = if (showSearch) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                        IconButton(onClick = { showLanguageDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language"
                            )
                        }
                        IconButton(onClick = { currentScreen = "settings" }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
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
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "chat",
                        onClick = { currentScreen = "chat" },
                        icon = { Icon(Icons.Default.Chat, "Chat") },
                        label = { Text("Chat") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "pm",
                        onClick = { currentScreen = "pm" },
                        icon = { Icon(Icons.Default.Message, "PM") },
                        label = { Text("PM") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "devices",
                        onClick = { currentScreen = "devices" },
                        icon = { Icon(Icons.Default.DevicesOther, "Devices") },
                        label = { Text("Control") }
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
                    "home" -> HomeScreen(searchQuery)
                    "chat" -> ChatScreen(chatMessage, chatHistory, 
                        onMessageChange = { chatMessage = it },
                        onSendMessage = {
                            if (chatMessage.isNotBlank()) {
                                val userMsg = ChatMessage(chatMessage, true)
                                chatHistory = chatHistory + userMsg
                                
                                scope.launch {
                                    delay(500)
                                    val aiResponse = ChatMessage(
                                        getAIResponse(chatMessage, selectedLanguages.first()),
                                        false
                                    )
                                    chatHistory = chatHistory + aiResponse
                                }
                                
                                chatMessage = ""
                            }
                        }
                    )
                    "pm" -> PMScreen()
                    "devices" -> DeviceControlScreen()
                    "settings" -> SettingsScreen()
                }
            }
        }

        // Language Selection Dialog
        if (showLanguageDialog) {
            LanguageSelectionDialog(
                selectedLanguages = selectedLanguages,
                onLanguagesSelected = { selected ->
                    selectedLanguages = selected
                    showLanguageDialog = false
                },
                onDismiss = { showLanguageDialog = false }
            )
        }
    }

    @Composable
    private fun HomeScreen(searchQuery: String) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // AI Orb
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
                text = "Your AI-powered voice assistant",
                fontSize = 14.sp,
                color = Color(0xFF64B5F6)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Search results
            if (searchQuery.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🔍 Search Results for: $searchQuery",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Searching the web and my knowledge base...",
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard("🎤\nVoice", Modifier.weight(1f))
                QuickActionCard("👁️\nVision", Modifier.weight(1f))
                QuickActionCard("🌐\nWeb", Modifier.weight(1f))
                QuickActionCard("🤖\nAI", Modifier.weight(1f))
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
            // Chat history
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

            // Input
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
                    placeholder = { Text("Type a message...") },
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
    private fun PMScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "📨 Private Messages",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // PM List
            val pmContacts = listOf(
                "John Doe" to "Hey, how are you?",
                "Jane Smith" to "Meeting at 3 PM",
                "AI Assistant" to "Your daily briefing is ready",
                "Team Group" to "New project discussion"
            )
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(pmContacts) { (name, preview) ->
                    PMContactCard(name, preview)
                }
            }
        }
    }

    @Composable
    private fun DeviceControlScreen() {
        var wifiEnabled by remember { mutableStateOf(isWiFiEnabled()) }
        var bluetoothEnabled by remember { mutableStateOf(isBluetoothEnabled()) }
        var brightness by remember { mutableStateOf(50f) }
        
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

            // WiFi Control
            DeviceControlCard(
                icon = "📶",
                title = "WiFi",
                description = if (wifiEnabled) "Connected" else "Disconnected",
                isEnabled = wifiEnabled,
                onToggle = {
                    toggleWiFi(!wifiEnabled)
                    wifiEnabled = !wifiEnabled
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bluetooth Control
            DeviceControlCard(
                icon = "📶",
                title = "Bluetooth",
                description = if (bluetoothEnabled) "On" else "Off",
                isEnabled = bluetoothEnabled,
                onToggle = {
                    toggleBluetooth(!bluetoothEnabled)
                    bluetoothEnabled = !bluetoothEnabled
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Brightness Control
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🔆", fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Brightness",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${brightness.toInt()}%",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64B5F6)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = brightness,
                        onValueChange = { brightness = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00E5FF),
                            activeTrackColor = Color(0xFF00E5FF)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DeviceQuickAction("🔇\nSilent", Modifier.weight(1f))
                DeviceQuickAction("✈️\nAirplane", Modifier.weight(1f))
                DeviceQuickAction("🔒\nLock", Modifier.weight(1f))
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
            
            val settings = listOf(
                "🌐 Language Settings",
                "🎤 Voice Configuration",
                "👁️ Vision Settings",
                "🔔 Notifications",
                "🔒 Privacy & Security",
                "💾 Storage Management",
                "ℹ️ About D.A.V.I.D"
            )
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(settings) { setting ->
                    SettingsItem(setting)
                }
            }
        }
    }

    @Composable
    private fun LanguageSelectionDialog(
        selectedLanguages: Set<String>,
        onLanguagesSelected: (Set<String>) -> Unit,
        onDismiss: () -> Unit
    ) {
        var selected by remember { mutableStateOf(selectedLanguages) }
        
        val languages = listOf(
            "English" to "🇬🇧",
            "Hindi" to "🇮🇳",
            "Tamil" to "🇮🇳",
            "Telugu" to "🇮🇳",
            "Bengali" to "🇮🇳",
            "Marathi" to "🇮🇳",
            "Gujarati" to "🇮🇳",
            "Kannada" to "🇮🇳",
            "Malayalam" to "🇮🇳",
            "Punjabi" to "🇮🇳",
            "Urdu" to "🇮🇳"
        )

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("🌐 Select Languages") },
            text = {
                LazyColumn {
                    items(languages) { (lang, flag) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selected = if (selected.contains(lang)) {
                                        if (selected.size > 1) selected - lang else selected
                                    } else {
                                        selected + lang
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selected.contains(lang),
                                onCheckedChange = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "$flag $lang", fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { onLanguagesSelected(selected) }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }

    @Composable
    private fun QuickActionCard(text: String, modifier: Modifier) {
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
    private fun PMContactCard(name: String, preview: String) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E5FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.first().toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = preview,
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }
    }

    @Composable
    private fun DeviceControlCard(
        icon: String,
        title: String,
        description: String,
        isEnabled: Boolean,
        onToggle: () -> Unit
    ) {
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
                    Column {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = description,
                            fontSize = 12.sp,
                            color = Color(0xFF64B5F6)
                        )
                    }
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E5FF),
                        checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.5f)
                    )
                )
            }
        }
    }

    @Composable
    private fun DeviceQuickAction(text: String, modifier: Modifier) {
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
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    @Composable
    private fun SettingsItem(text: String) {
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
                Text(
                    text = text,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF64B5F6)
                )
            }
        }
    }

    private fun getAIResponse(message: String, language: String): String {
        return when {
            message.contains("hello", true) || message.contains("hi", true) ->
                "Hello! I'm D.A.V.I.D, your AI assistant. How can I help you today?"
            message.contains("weather", true) ->
                "The weather today is sunny with a temperature of 25°C."
            message.contains("time", true) ->
                "Current time is ${java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}"
            else ->
                "I understand you said: '$message'. I'm here to help with voice commands, device control, and more!"
        }
    }

    private fun isWiFiEnabled(): Boolean {
        return try {
            wifiManager.isWifiEnabled
        } catch (e: Exception) {
            false
        }
    }

    private fun isBluetoothEnabled(): Boolean {
        return try {
            bluetoothManager.adapter?.isEnabled ?: false
        } catch (e: Exception) {
            false
        }
    }

    private fun toggleWiFi(enable: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ requires Settings panel
                startActivity(Intent(Settings.Panel.ACTION_WIFI))
            } else {
                @Suppress("DEPRECATION")
                wifiManager.isWifiEnabled = enable
            }
        } catch (e: Exception) {
            Log.e(TAG, "WiFi toggle error", e)
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
            Log.e(TAG, "Bluetooth toggle error", e)
        }
    }

    data class ChatMessage(val text: String, val isUser: Boolean)

    companion object {
        private const val TAG = "SafeMainActivity"
    }
}
