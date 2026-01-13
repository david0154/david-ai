package com.davidstudioz.david.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.davidstudioz.david.gesture.GestureManager
import com.davidstudioz.david.gesture.GestureRecognitionService

/**
 * GestureSettingsActivity - COMPLETE GESTURE CONFIGURATION
 * ✅ Gesture control toggle
 * ✅ Gesture sensitivity
 * ✅ Visual pointer toggle
 * ✅ Gesture mapping configuration
 * ✅ Model status display
 */
@OptIn(ExperimentalMaterial3Api::class)
class GestureSettingsActivity : ComponentActivity() {
    
    private lateinit var gestureManager: GestureManager
    private val CAMERA_PERMISSION_CODE = 101
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        gestureManager = GestureManager(this)
        
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
                            title = { Text("Gesture Settings", color = Color.White) },
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
                    GestureSettingsScreen(Modifier.padding(padding))
                }
            }
        }
    }
    
    @Composable
    private fun GestureSettingsScreen(modifier: Modifier = Modifier) {
        val prefs = getSharedPreferences("david_gesture", MODE_PRIVATE)
        
        var gestureEnabled by remember { 
            mutableStateOf(prefs.getBoolean("gesture_enabled", false)) 
        }
        var sensitivity by remember { 
            mutableStateOf(prefs.getFloat("gesture_sensitivity", 0.7f)) 
        }
        var showPointer by remember { 
            mutableStateOf(prefs.getBoolean("show_pointer", true)) 
        }
        var modelStatus by remember { 
            mutableStateOf(gestureManager.getModelStatus()) 
        }
        
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "✋ Gesture Control",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            // Model Status
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (gestureManager.isModelReady()) 
                            Color(0xFF10B981).copy(alpha = 0.2f)
                        else
                            Color(0xFFEF4444).copy(alpha = 0.2f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (gestureManager.isModelReady()) "✅ Model Ready" else "❌ Model Not Loaded",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = modelStatus,
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF)
                        )
                        if (!gestureManager.isModelReady()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Please download gesture model from model manager",
                                fontSize = 11.sp,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
            
            // Enable Gesture Control
            item {
                SwitchCard(
                    title = "Enable Gesture Control",
                    description = "Control device with hand gestures",
                    checked = gestureEnabled,
                    onCheckedChange = { enabled ->
                        if (!checkCameraPermission()) {
                            requestCameraPermission()
                            return@SwitchCard
                        }
                        
                        gestureEnabled = enabled
                        prefs.edit().putBoolean("gesture_enabled", enabled).apply()
                        
                        if (enabled) {
                            startGestureService()
                        } else {
                            stopGestureService()
                        }
                    }
                )
            }
            
            // Sensitivity Control
            item {
                SliderCard(
                    title = "Gesture Sensitivity",
                    value = sensitivity,
                    valueRange = 0.3f..1.0f,
                    onValueChange = { 
                        sensitivity = it
                        prefs.edit().putFloat("gesture_sensitivity", it).apply()
                    },
                    valueLabel = when {
                        sensitivity < 0.5f -> "Low"
                        sensitivity < 0.8f -> "Medium"
                        else -> "High"
                    }
                )
            }
            
            // Show Visual Pointer
            item {
                SwitchCard(
                    title = "Visual Pointer",
                    description = "Show pointer on screen during gesture control",
                    checked = showPointer,
                    onCheckedChange = {
                        showPointer = it
                        prefs.edit().putBoolean("show_pointer", it).apply()
                    }
                )
            }
            
            // Gesture Mapping Info
            item {
                GestureMappingCard()
            }
        }
    }
    
    @Composable
    private fun GestureMappingCard() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Gesture Mapping",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                GestureItem("👆 Swipe Up", "Increase volume")
                GestureItem("👇 Swipe Down", "Decrease volume")
                GestureItem("👈 Swipe Left", "Previous track")
                GestureItem("👉 Swipe Right", "Next track")
                GestureItem("🤏 Pinch", "Play/Pause")
                GestureItem("✋ Open Palm", "Pause media")
                GestureItem("✊ Closed Fist", "Play media")
                GestureItem("✌️ Peace Sign", "Take selfie")
                GestureItem("👍 Thumbs Up", "Volume up")
                GestureItem("👎 Thumbs Down", "Volume down")
            }
        }
    }
    
    @Composable
    private fun GestureItem(gesture: String, action: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(gesture, fontSize = 14.sp, color = Color.White)
            Text(action, fontSize = 12.sp, color = Color(0xFF9CA3AF))
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
    
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }
    
    private fun startGestureService() {
        val intent = Intent(this, GestureRecognitionService::class.java)
        startService(intent)
    }
    
    private fun stopGestureService() {
        val intent = Intent(this, GestureRecognitionService::class.java)
        stopService(intent)
    }
    
    companion object {
        private const val TAG = "GestureSettingsActivity"
    }
}