package com.davidstudioz.david.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidstudioz.david.utils.DeviceResourceManager
import kotlin.math.cos
import kotlin.math.sin

/**
 * Jarvis-Style Futuristic UI
 * - Google Assistant / Iron Man Jarvis look
 * - Animated AI orb
 * - Resource monitoring (RAM/Storage/CPU)
 * - Neon blue/cyan theme
 * - Glassmorphism design
 */
@Composable
fun JarvisMainScreen(
    resourceStatus: DeviceResourceManager.ResourceStatus,
    isListening: Boolean,
    statusMessage: String,
    onVoiceClick: () -> Unit
) {
    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

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
        // Animated background grid
        AnimatedGrid()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "D.A.V.I.D",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF),
                letterSpacing = 8.sp,
                modifier = Modifier.padding(top = 20.dp)
            )
            Text(
                text = "Digital Assistant Voice Intelligence Device",
                fontSize = 10.sp,
                color = Color(0xFF64B5F6),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Central AI Orb
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow
                Canvas(modifier = Modifier.size(220.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00E5FF).copy(alpha = 0.3f * pulseScale),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension / 2
                    )
                }

                // Rotating rings
                Canvas(modifier = Modifier.size(200.dp)) {
                    val radius = size.minDimension / 2
                    for (i in 0..2) {
                        drawCircle(
                            color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                            radius = radius - (i * 20f),
                            style = Stroke(width = 2f)
                        )
                    }
                }

                // Core orb
                Box(
                    modifier = Modifier
                        .size(120.dp * pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00E5FF),
                                    Color(0xFF0091EA),
                                    Color(0xFF01579B)
                                )
                            )
                        )
                        .blur(if (isListening) 10.dp else 0.dp)
                )

                // Voice wave animation when listening
                if (isListening) {
                    VoiceWaveAnimation()
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Status message
            GlassCard {
                Text(
                    text = statusMessage,
                    fontSize = 14.sp,
                    color = Color(0xFF00E5FF),
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Resource Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResourceRing(
                    label = "RAM",
                    usage = resourceStatus.ramUsagePercent,
                    value = "${resourceStatus.usedRamMB / 1024}/${resourceStatus.totalRamMB / 1024}GB",
                    color = Color(0xFF00E5FF)
                )
                ResourceRing(
                    label = "STORAGE",
                    usage = resourceStatus.storageUsagePercent,
                    value = "${resourceStatus.usedStorageGB}/${resourceStatus.totalStorageGB}GB",
                    color = Color(0xFF00FF88)
                )
                ResourceRing(
                    label = "CPU",
                    usage = resourceStatus.cpuUsagePercent,
                    value = "${resourceStatus.cpuCores} cores",
                    color = Color(0xFFFF6E40)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AI Model Info
            GlassCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI MODEL: ${resourceStatus.canUseForAI.recommendedModel.name}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF),
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = resourceStatus.canUseForAI.reason,
                        fontSize = 10.sp,
                        color = Color(0xFF90CAF9)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Voice button
            FloatingActionButton(
                onClick = onVoiceClick,
                modifier = Modifier.size(70.dp),
                containerColor = Color(0xFF00E5FF),
                contentColor = Color.Black
            ) {
                Text(
                    text = if (isListening) "🔊" else "🎤",
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun GlassCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Color(0xFF1E88E5).copy(alpha = 0.1f)
            )
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF00E5FF).copy(alpha = 0.05f),
                        Color.Transparent,
                        Color(0xFF00E5FF).copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        content()
    }
}

@Composable
fun ResourceRing(
    label: String,
    usage: Float,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(80.dp)) {
                // Background circle
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f),
                    style = Stroke(width = 8f)
                )
                // Progress arc
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = (usage / 100f) * 360f,
                    useCenter = false,
                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                )
            }
            Text(
                text = "${usage.toInt()}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color(0xFF90CAF9),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            fontSize = 8.sp,
            color = Color(0xFF64B5F6)
        )
    }
}

@Composable
fun VoiceWaveAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )

    Canvas(modifier = Modifier.size(150.dp)) {
        for (i in 0..8) {
            val angle = (i * 40f) + (wave1 * 360f)
            val radius = 60f + (wave1 * 20f)
            val x = center.x + radius * cos(Math.toRadians(angle.toDouble())).toFloat()
            val y = center.y + radius * sin(Math.toRadians(angle.toDouble())).toFloat()
            
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 1f - wave1),
                radius = 4f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun AnimatedGrid() {
    val infiniteTransition = rememberInfiniteTransition(label = "grid")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gridOffset"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridSize = 50f
        val linesX = (size.width / gridSize).toInt()
        val linesY = (size.height / gridSize).toInt()

        for (i in 0..linesX) {
            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = 0.05f),
                start = Offset((i * gridSize) + offset, 0f),
                end = Offset((i * gridSize) + offset, size.height),
                strokeWidth = 1f
            )
        }
        for (i in 0..linesY) {
            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = 0.05f),
                start = Offset(0f, (i * gridSize) + offset),
                end = Offset(size.width, (i * gridSize) + offset),
                strokeWidth = 1f
            )
        }
    }
}
