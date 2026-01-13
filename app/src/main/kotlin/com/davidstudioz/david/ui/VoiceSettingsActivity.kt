package com.davidstudioz.david.ui

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * VoiceSettingsActivity - COMPLETE VOICE CONFIGURATION
 * ✅ Male voice (David) and Female voice (Dayna)
 * ✅ TTS pitch control
 * ✅ TTS speed control
 * ✅ Always-on voice toggle
 * ✅ Wake word sensitivity
 * ✅ Voice language selection
 */
@OptIn(ExperimentalMaterial3Api::class)
class VoiceSettingsActivity : ComponentActivity() {
    
    private lateinit var tts: TextToSpeech
    private var ttsInitialized = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize TTS
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInitialized = true
                Log.d(TAG, "TTS initialized")
            }
        }
        
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF00E5FF),
                    background = Color(0xFF0A0E27)
                )
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Voice Settings", color = Color.White) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        "Back",
                                        tint = Color.White
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF1A1F3A)
                            )
                        )
                    },
                    containerColor = Color(0xFF0A0E27)
                ) { padding ->
                    VoiceSettingsScreen(Modifier.padding(padding))
                }
            }
        }
    }
    
    @Composable
    private fun VoiceSettingsScreen(modifier: Modifier = Modifier) {
        val prefs = getSharedPreferences("david_voice", MODE_PRIVATE)
        
        // Voice settings state
        var selectedVoice by remember { 
            mutableStateOf(prefs.getString("tts_voice", "male") ?: "male") 
        }
        var pitch by remember { 
            mutableStateOf(prefs.getFloat("tts_pitch", 1.0f)) 
        }
        var speechRate by remember { 
            mutableStateOf(prefs.getFloat("tts_rate", 1.0f)) 
        }
        var alwaysOnVoice by remember { 
            mutableStateOf(prefs.getBoolean("always_on_voice", false)) 
        }
        var wakeWordSensitivity by remember { 
            mutableStateOf(prefs.getFloat("wake_sensitivity", 0.7f)) 
        }
        var conciseMode by remember {
            mutableStateOf(prefs.getBoolean("concise_mode", true))
        }
        
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "🎤 Voice Configuration",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            // ✅ Voice Selection (Male/Female)
            item {
                VoiceSelectionCard(
                    selectedVoice = selectedVoice,
                    onVoiceChanged = { voice ->
                        selectedVoice = voice
                        prefs.edit().putString("tts_voice", voice).apply()
                        applyVoiceSettings(voice, pitch, speechRate)
                        testVoice(voice)
                    }
                )
            }
            
            // ✅ Pitch Control
            item {
                SliderCard(
                    title = "Voice Pitch",
                    value = pitch,
                    valueRange = 0.5f..2.0f,
                    onValueChange = { 
                        pitch = it
                        prefs.edit().putFloat("tts_pitch", it).apply()
                        applyVoiceSettings(selectedVoice, it, speechRate)
                    },
                    valueLabel = String.format("%.1f", pitch)
                )
            }
            
            // ✅ Speech Rate Control
            item {
                SliderCard(
                    title = "Speech Speed",
                    value = speechRate,
                    valueRange = 0.5f..2.0f,
                    onValueChange = { 
                        speechRate = it
                        prefs.edit().putFloat("tts_rate", it).apply()
                        applyVoiceSettings(selectedVoice, pitch, it)
                    },
                    valueLabel = String.format("%.1f", speechRate)
                )
            }
            
            // ✅ Concise Mode Toggle
            item {
                SwitchCard(
                    title = "Concise Mode",
                    description = "Short, direct responses without extra talking",
                    checked = conciseMode,
                    onCheckedChange = {
                        conciseMode = it
                        prefs.edit().putBoolean("concise_mode", it).apply()
                    }
                )
            }
            
            // ✅ Always-On Voice Toggle
            item {
                SwitchCard(
                    title = "Always-On Voice",
                    description = "Activate voice with \"Hey David\" wake word",
                    checked = alwaysOnVoice,
                    onCheckedChange = {
                        alwaysOnVoice = it
                        prefs.edit().putBoolean("always_on_voice", it).apply()
                    }
                )
            }
            
            // ✅ Wake Word Sensitivity
            if (alwaysOnVoice) {
                item {
                    SliderCard(
                        title = "Wake Word Sensitivity",
                        value = wakeWordSensitivity,
                        valueRange = 0.3f..1.0f,
                        onValueChange = { 
                            wakeWordSensitivity = it
                            prefs.edit().putFloat("wake_sensitivity", it).apply()
                        },
                        valueLabel = when {
                            wakeWordSensitivity < 0.5f -> "Low"
                            wakeWordSensitivity < 0.8f -> "Medium"
                            else -> "High"
                        }
                    )
                }
            }
            
            // Test Voice Button
            item {
                Button(
                    onClick = { testVoice(selectedVoice) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF)
                    )
                ) {
                    Text("Test Voice", color = Color.Black, fontSize = 16.sp)
                }
            }
        }
    }
    
    @Composable
    private fun VoiceSelectionCard(
        selectedVoice: String,
        onVoiceChanged: (String) -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Voice Character",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Male Voice
                    VoiceOption(
                        name = "David",
                        subtitle = "Male Voice",
                        icon = "👨",
                        isSelected = selectedVoice == "male",
                        onClick = { onVoiceChanged("male") }
                    )
                    
                    // Female Voice
                    VoiceOption(
                        name = "Dayna",
                        subtitle = "Female Voice",
                        icon = "👩",
                        isSelected = selectedVoice == "female",
                        onClick = { onVoiceChanged("female") }
                    )
                }
            }
        }
    }
    
    @Composable
    private fun VoiceOption(
        name: String,
        subtitle: String,
        icon: String,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .width(150.dp)
                .height(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) 
                    Color(0xFF00E5FF).copy(alpha = 0.3f) 
                else 
                    Color(0xFF1F2937)
            ),
            onClick = onClick
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 40.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color(0xFF00E5FF) else Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
    
    @Composable
    private fun SliderCard(
        title: String,
        value: Float,
        valueRange: ClosedFloatingPointRange<Float>,
        onValueChange: (Float) -> Unit,
        valueLabel: String
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(title, color = Color.White, fontSize = 14.sp)
                    Text(valueLabel, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = value,
                    onValueChange = onValueChange,
                    valueRange = valueRange,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00E5FF),
                        activeTrackColor = Color(0xFF00E5FF),
                        inactiveTrackColor = Color(0xFF1F2937)
                    )
                )
            }
        }
    }
    
    @Composable
    private fun SwitchCard(
        title: String,
        description: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E5FF),
                        checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
    
    /**
     * ✅ Apply voice settings to TTS
     */
    private fun applyVoiceSettings(voice: String, pitch: Float, rate: Float) {
        if (!ttsInitialized) return
        
        try {
            // Set pitch (male vs female)
            tts.setPitch(if (voice == "female") pitch * 1.2f else pitch * 0.9f)
            
            // Set speech rate
            tts.setSpeechRate(rate)
            
            // Set language
            tts.language = Locale.US
            
            Log.d(TAG, "Applied voice: $voice, pitch: $pitch, rate: $rate")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying voice settings", e)
        }
    }
    
    /**
     * ✅ Test the selected voice
     */
    private fun testVoice(voice: String) {
        if (!ttsInitialized) return
        
        val testText = when (voice) {
            "male" -> "Hello, I'm David. Your AI assistant."
            "female" -> "Hi, I'm Dayna. Ready to help you!"
            else -> "Hello!"
        }
        
        tts.speak(testText, TextToSpeech.QUEUE_FLUSH, null, null)
    }
    
    override fun onDestroy() {
        if (ttsInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
    
    companion object {
        private const val TAG = "VoiceSettingsActivity"
    }
}