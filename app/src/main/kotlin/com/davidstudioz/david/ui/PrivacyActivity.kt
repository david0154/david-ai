package com.davidstudioz.david.ui

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
 * PrivacyActivity - Privacy Policy Screen
 * NEW: Complete privacy policy viewer
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
                            title = { Text("Privacy Policy", color = Color.White) },
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
            PrivacySection(
                title = "🔒 Privacy-First Design",
                content = """D.A.V.I.D (Digital Assistant with Voice Interaction and Device control) is designed with your privacy as the top priority.
                
                All AI processing happens ON YOUR DEVICE. We never send your voice, images, or personal data to external servers."""
            )
            
            PrivacySection(
                title = "📱 Data Collection",
                content = """D.A.V.I.D collects:
                • Voice recordings (processed locally, deleted after use)
                • Camera images (for gesture control, not stored)
                • Device settings (stored locally, encrypted)
                • Chat history (stored locally, never shared)
                
                We DO NOT collect:
                • Personal identification
                • Location tracking (except when you request weather)
                • Browsing history
                • Contacts or messages (unless you command it)"""
            )
            
            PrivacySection(
                title = "🔐 Data Storage",
                content = """All data is stored locally on your device using AES-256 encryption.
                
                • AI models: Stored in app directory
                • Chat history: Encrypted local database
                • Settings: Encrypted shared preferences
                • Voice data: Never permanently stored
                
                You can delete ALL data anytime from Settings."""
            )
            
            PrivacySection(
                title = "🌐 Network Usage",
                content = """D.A.V.I.D only uses internet for:
                • Downloading AI models (one-time)
                • Weather updates (when you ask)
                • App updates (optional)
                
                You can use D.A.V.I.D completely offline after initial setup."""
            )
            
            PrivacySection(
                title = "♿ Accessibility Service",
                content = """The accessibility service is used ONLY for:
                • Background voice activation
                • Gesture-based device control
                • Scrolling and navigation commands
                
                We DO NOT:
                • Monitor your activity
                • Read your screen content
                • Track your app usage
                • Send accessibility data anywhere"""
            )
            
            PrivacySection(
                title = "📋 Permissions",
                content = """D.A.V.I.D requests permissions for:
                • 🎤 Microphone: Voice commands
                • 📷 Camera: Gesture recognition
                • 📞 Phone: Making calls (only when you ask)
                • 💬 SMS: Sending messages (only when you ask)
                • 📍 Location: Weather updates
                • 📱 Device Controls: WiFi, Bluetooth, etc.
                
                All permissions are used ONLY when you explicitly command D.A.V.I.D."""
            )
            
            PrivacySection(
                title = "🛡️ Your Rights",
                content = """You have the right to:
                • View all stored data
                • Delete all data at any time
                • Disable any feature
                • Revoke any permission
                • Export your chat history
                • Use the app completely offline"""
            )
            
            PrivacySection(
                title = "📧 Contact",
                content = """For privacy concerns:
                Email: support@davidstudioz.com
                GitHub: github.com/david0154/david-ai
                
                Last updated: January 2026"""
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF00E5FF).copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "✅ Your Privacy is Protected",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "D.A.V.I.D is 100% offline-capable and privacy-focused. Your data never leaves your device.",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
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
}