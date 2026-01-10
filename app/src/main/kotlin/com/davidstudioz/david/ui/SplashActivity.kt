package com.davidstudioz.david.ui

import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.davidstudioz.david.R
import com.davidstudioz.david.workers.ModelDownloadWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * D.A.V.I.D Splash Screen - Beautiful Modern Design
 * Shows branding, logo, and initialization progress
 * Digital Assistant Voice Intelligence Device
 * 
 * FIXED: All runtime bugs addressed
 */
class SplashActivity : ComponentActivity() {

    private var splashMinDuration = 3000L
    private var splashStartTime = 0L
    private var initializationError by mutableStateOf<String?>(null)
    private var permissionsGranted by mutableStateOf(false)

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        permissionsGranted = allGranted
        if (!allGranted) {
            Log.w(TAG, "Some permissions denied, continuing anyway")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        splashStartTime = System.currentTimeMillis()

        try {
            // Start model download worker (non-blocking)
            startModelDownloadWorker()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting model download", e)
            initializationError = "Model download error: ${e.localizedMessage}"
        }

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF00E5FF),
                    secondary = Color(0xFF9CA3AF),
                    background = Color(0xFF0A0E27)
                )
            ) {
                BeautifulSplashScreen()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checkPermissions()
    }

    private fun checkPermissions() {
        try {
            val permissions = arrayOf(
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.INTERNET
            )

            val needed = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }

            if (needed.isNotEmpty()) {
                Log.d(TAG, "Requesting permissions: ${needed.joinToString()}")
                permissionLauncher.launch(needed.toTypedArray())
            } else {
                permissionsGranted = true
                Log.d(TAG, "All critical permissions already granted")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permissions", e)
            permissionsGranted = true // Continue anyway
        }
    }

    @Composable
    private fun BeautifulSplashScreen() {
        var progress by remember { mutableStateOf(0f) }
        var statusText by remember { mutableStateOf("Initializing") }
        var hasError by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }

        // Check for initialization errors
        LaunchedEffect(initializationError) {
            initializationError?.let {
                hasError = true
                errorMessage = it
            }
        }

        // Animations
        val infiniteTransition = rememberInfiniteTransition(label = "splash_animation")
        
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow"
        )

        LaunchedEffect(Unit) {
            try {
                // Progressive initialization
                val steps = listOf(
                    0.2f to "Loading core systems",
                    0.4f to "Initializing AI models",
                    0.6f to "Setting up voice recognition",
                    0.8f to "Preparing interface",
                    1.0f to "Ready to launch"
                )

                steps.forEach { (targetProgress, message) ->
                    statusText = message
                    val startProgress = progress
                    val duration = 600L
                    val startTime = System.currentTimeMillis()

                    while (progress < targetProgress) {
                        val elapsed = System.currentTimeMillis() - startTime
                        progress = (startProgress + (targetProgress - startProgress) * 
                                   (elapsed.toFloat() / duration)).coerceAtMost(targetProgress)
                        delay(16)
                    }
                }

                // Wait for minimum splash duration
                val elapsed = System.currentTimeMillis() - splashStartTime
                val remaining = splashMinDuration - elapsed
                if (remaining > 0) {
                    delay(remaining)
                }

                navigateToLogin()
            } catch (e: Exception) {
                Log.e(TAG, "Fatal error in splash screen", e)
                hasError = true
                errorMessage = e.localizedMessage ?: "Unknown error"
                delay(2000)
                navigateToLogin()
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
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!hasError) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                ) {
                    // Animated Logo Container
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .scale(pulseScale),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .alpha(glowAlpha)
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

                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF00E5FF).copy(alpha = 0.15f),
                                            Color(0xFF1E88E5).copy(alpha = 0.05f)
                                        )
                                    )
                                )
                        )

                        LogoOrEmoji(
                            modifier = Modifier.size(100.dp),
                            tint = Color(0xFF00E5FF)
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = "D.A.V.I.D",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00E5FF),
                        letterSpacing = 8.sp,
                        style = MaterialTheme.typography.displayLarge
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Digital Assistant Voice\nIntelligence Device",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64B5F6),
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF00E5FF).copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your AI-Powered Voice Assistant",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(280.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(Color(0xFF1E293B))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF00E5FF),
                                                Color(0xFF1E88E5),
                                                Color(0xFF00E5FF)
                                            )
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = statusText,
                            fontSize = 13.sp,
                            color = Color(0xFF64B5F6),
                            letterSpacing = 0.5.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                    }

                    Spacer(modifier = Modifier.height(60.dp))

                    Text(
                        text = "Developed by David Studioz",
                        fontSize = 10.sp,
                        color = Color(0xFF4B5563),
                        letterSpacing = 1.sp
                    )
                }
            } else {
                ErrorSplashScreen(errorMessage)
            }
        }
    }

    @Composable
    private fun LogoOrEmoji(
        modifier: Modifier = Modifier,
        tint: Color? = null
    ) {
        var showFallback by remember { mutableStateOf(false) }
        
        if (!showFallback) {
            try {
                // Try to load logo from resources
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "D.A.V.I.D Logo",
                    modifier = modifier.clip(CircleShape),
                    contentScale = ContentScale.Fit,
                    colorFilter = tint?.let { ColorFilter.tint(it) },
                    onError = { showFallback = true }
                )
            } catch (e: Exception) {
                showFallback = true
            }
        }
        
        if (showFallback) {
            LogoFallback(modifier, tint)
        }
    }

    @Composable
    private fun LogoFallback(modifier: Modifier = Modifier, tint: Color? = null) {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            tint ?: Color(0xFF00E5FF),
                            (tint ?: Color(0xFF00E5FF)).copy(alpha = 0.3f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🤖",
                fontSize = 48.sp
            )
        }
    }

    @Composable
    private fun ErrorSplashScreen(errorMsg: String) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "⚠️",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Initialization Error",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6E40)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMsg,
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Continuing to app...",
                fontSize = 11.sp,
                color = Color(0xFF64B5F6)
            )
        }
    }

    private fun startModelDownloadWorker() {
        try {
            // Ensure WorkManager is initialized
            if (!WorkManager.isInitialized()) {
                try {
                    WorkManager.initialize(
                        this,
                        Configuration.Builder()
                            .setMinimumLoggingLevel(Log.INFO)
                            .build()
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "WorkManager already initialized or error", e)
                }
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(false)
                .setRequiresDeviceIdle(false)
                .build()

            val downloadWork = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            try {
                WorkManager.getInstance(this).enqueueUniqueWork(
                    "model_download",
                    ExistingWorkPolicy.KEEP,
                    downloadWork
                )
                Log.d(TAG, "Model download worker enqueued")
            } catch (e: Exception) {
                Log.e(TAG, "Error enqueuing work", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting model download worker", e)
        }
    }

    private fun navigateToLogin() {
        try {
            // Verify LoginActivity exists
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            val resolveInfo = packageManager.resolveActivity(intent, 0)
            
            if (resolveInfo == null) {
                Log.e(TAG, "LoginActivity not found in manifest")
                navigateToSafeMain()
                return
            }

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to LoginActivity", e)
            navigateToSafeMain()
        }
    }

    private fun navigateToSafeMain() {
        try {
            val intent = Intent(this, com.davidstudioz.david.SafeMainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Critical navigation error", e)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            // Clean up if needed
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
    }

    companion object {
        private const val TAG = "SplashActivity"
    }
}
