package com.davidstudioz.david.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.davidstudioz.david.R
import com.davidstudioz.david.workers.ModelDownloadWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * D.A.V.I.D Splash Screen
 * SKIP LOGIN - Go directly to Model Download
 */
class SplashActivity : ComponentActivity() {

    private var splashMinDuration = 2000L
    private var splashStartTime = 0L
    private var permissionsGranted by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.all { it.value }
        Log.d(TAG, "Permissions: $permissionsGranted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashStartTime = System.currentTimeMillis()

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF00E5FF),
                    background = Color(0xFF0A0E27)
                )
            ) {
                QuickSplashScreen()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checkPermissions()
    }

    private fun checkPermissions() {
        val permissions = mutableListOf(
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.INTERNET
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
        }

        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        } else {
            permissionsGranted = true
        }
    }

    @Composable
    private fun QuickSplashScreen() {
        var progress by remember { mutableStateOf(0f) }

        val infiniteTransition = rememberInfiniteTransition(label = "splash")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        LaunchedEffect(Unit) {
            while (progress < 1f) {
                delay(16)
                progress = (progress + 0.02f).coerceAtMost(1f)
            }
            
            val elapsed = System.currentTimeMillis() - splashStartTime
            val remaining = splashMinDuration - elapsed
            if (remaining > 0) delay(remaining)
            
            // SKIP LOGIN - Go directly to Model Download
            navigateToModelDownload()
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
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(pulseScale),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🤖", fontSize = 64.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.width(200.dp),
                    color = Color(0xFF00E5FF)
                )
            }
        }
    }

    private fun navigateToModelDownload() {
        try {
            Log.d(TAG, "Navigating to ModelDownloadActivity (SKIP LOGIN)")
            val intent = Intent(this, ModelDownloadActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Navigation error", e)
        }
    }

    companion object {
        private const val TAG = "SplashActivity"
    }
}
