package com.davidstudioz.david

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.delay

/**
 * Safe MainActivity - Fallback UI that won't crash
 * Simple, beautiful Jarvis-style interface without complex dependencies
 */
class SafeMainActivity : ComponentActivity() {

    private var statusMessage by mutableStateOf("D.A.V.I.D AI Ready")
    private var isListening by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "SafeMainActivity started")
            
            setContent {
                MaterialTheme(
                    colorScheme = darkColorScheme(
                        primary = Color(0xFF00E5FF),
                        secondary = Color(0xFF9CA3AF),
                        background = Color(0xFF0A0E27)
                    )
                ) {
                    SafeMainScreen()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            // Show minimal error UI
            setContent {
                ErrorScreen(e.message ?: "Unknown error")
            }
        }
    }

    @Composable
    private fun SafeMainScreen() {
        var currentTime by remember { mutableStateOf("00:00:00") }

        // Time updater
        LaunchedEffect(Unit) {
            while (true) {
                currentTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                    .format(java.util.Date())
                delay(1000)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "D.A.V.I.D",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00E5FF),
                            letterSpacing = 6.sp
                        )
                        Text(
                            text = "Digital Assistant Voice Intelligence Device",
                            fontSize = 9.sp,
                            color = Color(0xFF64B5F6),
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = currentTime,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // AI Orb
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer glow
                    Box(
                        modifier = Modifier
                            .size(200.dp)
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

                    // Inner orb
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF00E5FF).copy(alpha = 0.2f),
                                        Color(0xFF1E88E5).copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🤖",
                            fontSize = 64.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Status message
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E88E5).copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    Text(
                        text = statusMessage,
                        fontSize = 14.sp,
                        color = Color(0xFF00E5FF),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Feature cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        icon = "🎤",
                        title = "Voice",
                        subtitle = "Ready",
                        modifier = Modifier.weight(1f)
                    )
                    FeatureCard(
                        icon = "🧠",
                        title = "AI",
                        subtitle = "Online",
                        modifier = Modifier.weight(1f)
                    )
                    FeatureCard(
                        icon = "🌤",
                        title = "Features",
                        subtitle = "Active",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Quick actions
                Text(
                    text = "QUICK ACTIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64B5F6),
                    letterSpacing = 2.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionButton(
                        icon = "💬",
                        text = "Chat",
                        onClick = {
                            statusMessage = "Chat feature coming soon!"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        icon = "🔍",
                        text = "Search",
                        onClick = {
                            statusMessage = "Search feature coming soon!"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionButton(
                        icon = "⚙️",
                        text = "Settings",
                        onClick = {
                            statusMessage = "Settings feature coming soon!"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        icon = "📊",
                        text = "Stats",
                        onClick = {
                            statusMessage = "Statistics feature coming soon!"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Voice button
                FloatingActionButton(
                    onClick = {
                        isListening = !isListening
                        statusMessage = if (isListening) "Listening..." else "Voice assistant ready"
                    },
                    modifier = Modifier.size(72.dp),
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color.Black
                ) {
                    Text(
                        text = if (isListening) "🔊" else "🎤",
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Powered by David Studioz",
                    fontSize = 10.sp,
                    color = Color(0xFF4B5563),
                    letterSpacing = 1.sp
                )
            }
        }
    }

    @Composable
    private fun FeatureCard(
        icon: String,
        title: String,
        subtitle: String,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E88E5).copy(alpha = 0.1f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = icon,
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    @Composable
    private fun ActionButton(
        icon: String,
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Button(
            onClick = onClick,
            modifier = modifier.height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00E5FF).copy(alpha = 0.2f),
                contentColor = Color(0xFF00E5FF)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    @Composable
    private fun ErrorScreen(errorMsg: String) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0E27)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "⚠️",
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Error",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6E40)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMsg,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { finish() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF)
                    )
                ) {
                    Text("Close", color = Color.Black)
                }
            }
        }
    }

    companion object {
        private const val TAG = "SafeMainActivity"
    }
}
