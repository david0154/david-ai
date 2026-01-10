package com.davidstudioz.david.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidstudioz.david.MainActivity
import com.davidstudioz.david.R

/**
 * Login Activity with Google Sign-In
 * Beautiful Jarvis-style login screen
 */
class LoginActivity : ComponentActivity() {

    private var isLoggedIn by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if already logged in
        val prefs = getSharedPreferences("david_prefs", MODE_PRIVATE)
        isLoggedIn = prefs.getBoolean("is_logged_in", false)

        if (isLoggedIn) {
            navigateToModelDownload()
            return
        }

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF00E5FF),
                    secondary = Color(0xFF9CA3AF),
                    background = Color(0xFF0A0E27)
                )
            ) {
                LoginScreen()
            }
        }
    }

    @Composable
    private fun LoginScreen() {
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

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
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                // Logo
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00E5FF).copy(alpha = 0.3f),
                                    Color.Transparent
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

                Spacer(modifier = Modifier.height(32.dp))

                // Title
                Text(
                    text = "D.A.V.I.D",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00E5FF),
                    letterSpacing = 6.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Digital Assistant Voice Intelligence Device",
                    fontSize = 12.sp,
                    color = Color(0xFF64B5F6),
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Welcome message
                Text(
                    text = "Welcome Back",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sign in to continue to your AI assistant",
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Google Sign-In Button
                Button(
                    onClick = {
                        isLoading = true
                        // Simulate Google Sign-In
                        handleGoogleSignIn()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(28.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "👑", // Google logo emoji
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue with Google",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Guest Mode Button
                OutlinedButton(
                    onClick = {
                        isLoading = true
                        handleGuestMode()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF00E5FF)
                    ),
                    shape = RoundedCornerShape(28.dp),
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Guest",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue as Guest",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Error message
                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = error,
                        fontSize = 12.sp,
                        color = Color(0xFFFF6E40),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Terms & Privacy
                Text(
                    text = "By continuing, you agree to our Terms of Service\nand Privacy Policy",
                    fontSize = 10.sp,
                    color = Color(0xFF4B5563),
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
        }
    }

    private fun handleGoogleSignIn() {
        try {
            // TODO: Implement actual Google Sign-In
            // For now, simulate successful login
            val prefs = getSharedPreferences("david_prefs", MODE_PRIVATE)
            prefs.edit().apply {
                putBoolean("is_logged_in", true)
                putString("user_name", "User")
                putString("user_email", "user@example.com")
                apply()
            }

            Log.d(TAG, "Google Sign-In successful")
            navigateToModelDownload()
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In error", e)
        }
    }

    private fun handleGuestMode() {
        try {
            val prefs = getSharedPreferences("david_prefs", MODE_PRIVATE)
            prefs.edit().apply {
                putBoolean("is_logged_in", true)
                putString("user_name", "Guest")
                putString("user_email", "guest@david.ai")
                putBoolean("is_guest", true)
                apply()
            }

            Log.d(TAG, "Guest mode activated")
            navigateToModelDownload()
        } catch (e: Exception) {
            Log.e(TAG, "Guest mode error", e)
        }
    }

    private fun navigateToModelDownload() {
        try {
            val intent = Intent(this, ModelDownloadActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Navigation error", e)
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}
