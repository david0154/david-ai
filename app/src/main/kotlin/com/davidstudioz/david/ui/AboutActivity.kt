package com.davidstudioz.david.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * AboutActivity - Display app information and credits
 * NEW: Created to fix settings about navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
class AboutActivity : ComponentActivity() {
    
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
                            title = { Text("About D.A.V.I.D", color = Color.White) },
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
                    AboutContent(Modifier.padding(padding))
                }
            }
        }
    }
    
    @Composable
    private fun AboutContent(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // App Icon
            Text(
                text = "🤖",
                fontSize = 64.sp,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
            
            Text(
                text = "D.A.V.I.D AI",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF),
                letterSpacing = 4.sp
            )
            
            Text(
                text = "Digital Assistant with Voice Interaction & Device control",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoCard(
                title = "Version",
                value = "1.0.0 (Build 2026.01.13)"
            )
            
            InfoCard(
                title = "Developer",
                value = "David Studioz (Nexuzy Tech Ltd.)"
            )
            
            InfoCard(
                title = "Features",
                value = """• Voice Control & Speech Recognition
                    |• Gesture Control with Hand Tracking
                    |• 15 Languages Support
                    |• Offline AI Processing
                    |• Privacy-First Design
                    |• Device Automation""".trimMargin()
            )
            
            InfoCard(
                title = "Technology Stack",
                value = """• Kotlin & Jetpack Compose
                    |• Whisper.cpp for Voice
                    |• LLaMA/Phi-2 for Chat
                    |• MediaPipe for Gestures
                    |• ONNX Runtime for Vision""".trimMargin()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://github.com/david0154/david-ai")
                        }
                        startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF)
                    )
                ) {
                    Text("GitHub", color = Color.Black)
                }
                
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://github.com/david0154/david-ai/issues")
                        }
                        startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E88E5).copy(alpha = 0.3f)
                    )
                ) {
                    Text("Report Issue", color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "© 2026 David Studioz. Licensed under BSD-3-Clause.",
                fontSize = 10.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )
        }
    }
    
    @Composable
    private fun InfoCard(title: String, value: String) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64B5F6),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = Color.White,
                    lineHeight = 20.sp
                )
            }
        }
    }
}