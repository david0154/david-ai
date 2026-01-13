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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import androidx.work.*
import com.davidstudioz.david.R
import com.davidstudioz.david.SafeMainActivity
import com.davidstudioz.david.models.ModelManager
import com.davidstudioz.david.workers.ModelDownloadWorker
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

/**
 * D.A.V.I.D Splash Screen - WITH MODEL VERIFICATION
 * ✅ Verifies essential models are actually downloaded
 * ✅ Redirects to ModelDownloadActivity if models missing
 * ✅ Doesn't rely only on SharedPreferences flag
 * ✅ Checks actual file existence and size
 * ✅ Handles download failures gracefully
 */
class SplashActivity : ComponentActivity() {

    private var splashMinDuration = 3500L
    private var splashStartTime = 0L
    private var initializationError by mutableStateOf<String?>(null)
    private var permissionsGranted by mutableStateOf(false)
    private lateinit var modelManager: ModelManager

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        permissionsGranted = allGranted
        if (!allGranted) {
            Log.w(TAG, "Some permissions denied: ${permissions.filter { !it.value }}")
        } else {
            Log.d(TAG, "All permissions granted successfully")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        splashStartTime = System.currentTimeMillis()

        try {
            // Initialize ModelManager to check models
            modelManager = ModelManager(this)
            Log.d(TAG, "ModelManager initialized")
            
            startModelDownloadWorker()
            Log.d(TAG, "Model download worker started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting model download", e)
            initializationError = "Model download initialization error: ${e.localizedMessage}"
        }

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF00E5FF),
                    secondary = Color(0xFF9CA3AF),
                    tertiary = Color(0xFF64B5F6),
                    background = Color(0xFF0A0E27),
                    surface = Color(0xFF1A1F3A),
                    error = Color(0xFFFF6E40)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BeautifulSplashScreen()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        try {
            val permissions = mutableListOf(
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
                permissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
            }

            val needed = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }

            if (needed.isNotEmpty()) {
                Log.d(TAG, "Requesting ${needed.size} permissions: ${needed.joinToString()}")
                permissionLauncher.launch(needed.toTypedArray())
            } else {
                permissionsGranted = true
                Log.d(TAG, "All critical permissions already granted")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permissions", e)
            permissionsGranted = true
        }
    }

    @Composable
    private fun BeautifulSplashScreen() {
        var progress by remember { mutableStateOf(0f) }
        var statusText by remember { mutableStateOf("Initializing D.A.V.I.D") }
        var hasError by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }
        var currentFeature by remember { mutableStateOf(0) }

        LaunchedEffect(initializationError) {
            initializationError?.let {
                hasError = true
                errorMessage = it
                Log.e(TAG, "Initialization error: $it")
            }
        }

        val infiniteTransition = rememberInfiniteTransition(label = "splash_animation")
        
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )

        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_alpha"
        )

        val rotationAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )

        LaunchedEffect(Unit) {
            data class InitStep(val progress: Float, val message: String, val feature: Int)
            
            val initializationSteps = listOf(
                InitStep(0.15f, "Loading core systems", 0),
                InitStep(0.30f, "Checking AI models", 1),
                InitStep(0.45f, "Verifying voice models", 2),
                InitStep(0.60f, "Checking vision models", 3),
                InitStep(0.75f, "Verifying language models", 4),
                InitStep(0.90f, "Preparing user interface", 5),
                InitStep(1.0f, "Ready to launch", 6)
            )

            initializationSteps.forEach { step ->
                statusText = step.message
                currentFeature = step.feature
                val startProgress = progress
                val duration = 500L
                val startTime = System.currentTimeMillis()

                while (progress < step.progress) {
                    val elapsed = System.currentTimeMillis() - startTime
                    val animProgress = (elapsed.toFloat() / duration).coerceAtMost(1f)
                    progress = startProgress + (step.progress - startProgress) * animProgress
                    delay(16)
                }
                progress = step.progress
                delay(200)
            }

            val elapsed = System.currentTimeMillis() - splashStartTime
            val remaining = splashMinDuration - elapsed
            if (remaining > 0) {
                Log.d(TAG, "Waiting ${remaining}ms for minimum splash duration")
                delay(remaining)
            }

            // ✅ CRITICAL: Verify models are actually downloaded
            verifyAndNavigate()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0E27),
                            Color(0xFF1A1F3A),
                            Color(0xFF0F1629),
                            Color(0xFF0A0E27)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .rotate(rotationAngle)
                    .alpha(0.1f)
            ) {
                repeat(8) { index ->
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .offset(
                                x = (200 * kotlin.math.cos(index * 45.0 * Math.PI / 180)).dp,
                                y = (200 * kotlin.math.sin(index * 45.0 * Math.PI / 180)).dp
                            )
                            .clip(CircleShape)
                            .background(Color(0xFF00E5FF))
                    )
                }
            }

            if (!hasError) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .scale(pulseScale),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .alpha(glowAlpha * 0.4f)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF00E5FF).copy(alpha = 0.4f),
                                            Color(0xFF1E88E5).copy(alpha = 0.2f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .alpha(glowAlpha * 0.6f)
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
                                .size(150.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF00E5FF).copy(alpha = 0.15f),
                                            Color(0xFF1E88E5).copy(alpha = 0.08f),
                                            Color(0xFF0A0E27)
                                        )
                                    )
                                )
                        )

                        LogoDisplay(
                            modifier = Modifier.size(110.dp),
                            tint = Color(0xFF00E5FF)
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = "D.A.V.I.D",
                        fontSize = 52.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00E5FF),
                        letterSpacing = 10.sp,
                        style = MaterialTheme.typography.displayLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Digital Assistant with Voice \n& Intelligent Decisions",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64B5F6),
                        letterSpacing = 2.5.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF00E5FF).copy(alpha = 0.6f),
                                        Color(0xFF00E5FF),
                                        Color(0xFF00E5FF).copy(alpha = 0.6f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Your AI-Powered Voice Assistant",
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Light
                    )

                    Spacer(modifier = Modifier.height(56.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(300.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1E293B))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
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

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = statusText,
                            fontSize = 14.sp,
                            color = Color(0xFF64B5F6),
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val features = listOf(
                            "🎤" to "Voice",
                            "🧠" to "AI",
                            "👁️" to "Vision",
                            "🌐" to "Web"
                        )
                        features.forEachIndexed { index, (icon, label) ->
                            FeatureIndicator(
                                icon = icon,
                                label = label,
                                isActive = currentFeature >= index
                            )
                            if (index < features.size - 1) {
                                Spacer(modifier = Modifier.width(24.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Powered by Advanced AI",
                            fontSize = 10.sp,
                            color = Color(0xFF4B5563),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Developed by Nexuzy Tech Pvt Ltd",
                            fontSize = 10.sp,
                            color = Color(0xFF4B5563),
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                ErrorSplashScreen(errorMessage)
            }
        }
    }

    @Composable
    private fun LogoDisplay(modifier: Modifier = Modifier, tint: Color? = null) {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            (tint ?: Color(0xFF00E5FF)).copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🤖", fontSize = 56.sp)
        }
    }

    @Composable
    private fun FeatureIndicator(icon: String, label: String, isActive: Boolean) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive)
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00E5FF).copy(alpha = 0.2f),
                                    Color(0xFF1E88E5).copy(alpha = 0.1f)
                                )
                            )
                        else
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF1E293B),
                                    Color(0xFF0F1629)
                                )
                            )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 20.sp,
                    modifier = Modifier.alpha(if (isActive) 1f else 0.3f)
                )
            }
            Text(
                text = label,
                fontSize = 9.sp,
                color = if (isActive) Color(0xFF00E5FF) else Color(0xFF4B5563),
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
        }
    }

    @Composable
    private fun ErrorSplashScreen(errorMsg: String) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF6E40).copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⚠️", fontSize = 48.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Initialization Error",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6E40)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = errorMsg,
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = Color(0xFF64B5F6),
                strokeWidth = 3.dp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Continuing to app...",
                fontSize = 12.sp,
                color = Color(0xFF64B5F6),
                fontWeight = FontWeight.Medium
            )
        }
    }

    private fun startModelDownloadWorker() {
        try {
            if (!WorkManager.isInitialized()) {
                try {
                    WorkManager.initialize(
                        this,
                        Configuration.Builder()
                            .setMinimumLoggingLevel(Log.INFO)
                            .build()
                    )
                    Log.d(TAG, "WorkManager initialized successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "WorkManager initialization error", e)
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

            WorkManager.getInstance(this).enqueueUniqueWork(
                "model_download",
                ExistingWorkPolicy.KEEP,
                downloadWork
            )
            Log.d(TAG, "Model download worker enqueued successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting model download worker", e)
        }
    }

    /**
     * ✅ CRITICAL: Verify models are actually downloaded before navigating
     * Don't rely on SharedPreferences alone - check actual files!
     */
    private fun verifyAndNavigate() {
        try {
            Log.d(TAG, "✅ Verifying model downloads...")
            
            // Check if essential models are actually present and valid
            val modelsPresent = modelManager.areEssentialModelsDownloaded()
            val downloadedModels = modelManager.getDownloadedModels()
            val modelCount = downloadedModels.size
            val totalSizeMB = modelManager.getTotalDownloadedSizeMB()
            
            Log.d(TAG, "Models present: $modelsPresent")
            Log.d(TAG, "Model count: $modelCount")
            Log.d(TAG, "Total size: ${totalSizeMB.toInt()} MB")
            
            if (modelsPresent && modelCount >= 5 && totalSizeMB > 100f) {
                // Models are actually downloaded - proceed to main app
                Log.d(TAG, "✅ All essential models verified! Proceeding to main app")
                navigateToMain()
            } else {
                // Models not downloaded - go to download screen
                Log.w(TAG, "⚠️ Models missing or incomplete. Redirecting to download screen")
                Log.d(TAG, "Model files found: ${downloadedModels.map { it.name }}")
                
                // Clear the false flag if it was set
                val prefs = getSharedPreferences("david_prefs", MODE_PRIVATE)
                prefs.edit().apply {
                    putBoolean("model_downloaded", false)
                    apply()
                }
                
                navigateToModelDownload()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying models", e)
            // On error, go to download screen to be safe
            navigateToModelDownload()
        }
    }

    private fun navigateToModelDownload() {
        try {
            Log.d(TAG, "Navigating to ModelDownloadActivity")
            val intent = Intent(this, ModelDownloadActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Navigation error to ModelDownloadActivity", e)
            navigateToMain()
        }
    }

    private fun navigateToMain() {
        try {
            Log.d(TAG, "Navigating to SafeMainActivity")
            val intent = Intent(this, SafeMainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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
            Log.d(TAG, "SplashActivity destroyed")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
    }

    companion object {
        private const val TAG = "SplashActivity"
    }
}