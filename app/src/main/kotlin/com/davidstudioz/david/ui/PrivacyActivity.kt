package com.davidstudioz.david.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * PrivacyActivity - Display privacy policy and data handling
 * NEW: Created to fix settings privacy navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
class PrivacyActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
                            title = { Text("Privacy & Security", color = Color.White) },
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
                    PrivacyContent(Modifier.padding(padding))
                }
            }
        }
    }
    
    @Composable
    private fun PrivacyContent(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🔒 Privacy First",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF)
            )
            
            PrivacySection(
                title = "Offline-First Design",
                content = "D.A.V.I.D processes everything locally on your device. All AI models run offline, ensuring your data never leaves your phone."
            )
            
            PrivacySection(
                title = "No Data Collection",
                content = "We don't collect, store, or transmit any personal data. No analytics, no tracking, no cloud servers. Your conversations and commands stay on your device."
            )
            
            PrivacySection(
                title = "Encrypted Storage",
                content = "All local data, including chat history and voice recordings, is encrypted using AES-256 encryption and protected by your device's security."
            )
            
            PrivacySection(
                title = "Permissions",
                content = """D.A.V.I.D requests permissions only for features you use:
                    |• Camera: For gesture control and visual commands
                    |• Microphone: For voice commands and speech recognition
                    |• Accessibility: For background automation
                    |• Storage: To save AI models locally
                    |• Network: Only for downloading AI models (optional)
                    """.trimMargin()
            )
            
            PrivacySection(
                title = "Open Source",
                content = "D.A.V.I.D is open source. You can review our code, verify our privacy claims, and contribute on GitHub."
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://github.com/david0154/david-ai/blob/main/PRIVACY_POLICY.md")
                    }
                    startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF)
                )
            ) {
                Text(
                    "View Full Privacy Policy",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    
    @Composable
    private fun PrivacySection(title: String, content: String) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = content,
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF),
                    lineHeight = 20.sp
                )
            }
        }
    }
}