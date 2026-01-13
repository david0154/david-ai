package com.davidstudioz.david.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * AboutActivity - About D.A.V.I.D Screen
 * NEW: Complete about page with app info
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
            // App Icon & Title
            Text(
                text = "🤖",
                fontSize = 64.sp
            )
            
            Text(
                text = "D.A.V.I.D",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF),
                letterSpacing = 6.sp
            )
            
            Text(
                text = "Digital Assistant with Voice Interaction and Device control",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Version Info
            InfoCard(
                title = "📱 Version",
                content = "1.0.0 (Build 1)\nRelease: January 2026"
            )
            
            // Features
            InfoCard(
                title = "✨ Features",
                content = """• Voice Control & Recognition
• Gesture-based Device Control
• Advanced AI Chat (LLM)
• Computer Vision
• 15 Language Support
• Offline-first & Privacy-focused
• Smart Device Management
• Weather & Time Integration
• Accessibility Service"""
            )
            
            // Technology
            InfoCard(
                title = "🔧 Technology",
                content = """• Whisper AI (Voice Recognition)
• LLaMA/Phi-2 (Language Models)
• MediaPipe (Gesture Detection)
• ONNX Runtime (Computer Vision)
• Jetpack Compose (UI)
• Kotlin Coroutines
• End-to-End Encryption"""
            )
            
            // Developer Info
            InfoCard(
                title = "👨‍💻 Developer",
                content = "Created by David Studioz\nNexuzy Tech Ltd.\n\nDeveloped with ❤️ in India"
            )
            
            // Links
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🔗 Links",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    LinkItem("GitHub Repository", "github.com/david0154/david-ai") {
                        openUrl("https://github.com/david0154/david-ai")
                    }
                    
                    LinkItem("Report Issues", "Submit bug reports") {
                        openUrl("https://github.com/david0154/david-ai/issues")
                    }
                    
                    LinkItem("Privacy Policy", "How we protect your data") {
                        startActivity(Intent(this@AboutActivity, PrivacyActivity::class.java))
                    }
                    
                    LinkItem("License", "Apache License 2.0") {
                        openUrl("https://github.com/david0154/david-ai/blob/main/LICENSE")
                    }
                }
            }
            
            // Credits
            InfoCard(
                title = "🙏 Credits",
                content = """D.A.V.I.D uses open-source AI models:
• OpenAI Whisper
• Meta LLaMA
• Microsoft Phi-2
• Google MediaPipe
• ONNX Runtime

Thank you to the open-source community!"""
            )
            
            // Copyright
            Text(
                text = "© 2026 David Studioz / Nexuzy Tech Ltd.\nAll rights reserved.",
                fontSize = 10.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 16.dp)
            )
            
            Text(
                text = "Made with ❤️ for the AI community",
                fontSize = 10.sp,
                color = Color(0xFF00E5FF),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
    
    @Composable
    private fun InfoCard(title: String, content: String) {
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
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = content,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    lineHeight = 18.sp
                )
            }
        }
    }
    
    @Composable
    private fun LinkItem(title: String, subtitle: String, onClick: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF00E5FF)
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF)
            )
        }
    }
    
    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}