package com.davidstudioz.david.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import kotlin.math.cos
import kotlin.math.sin

/**
 * Reusable Jarvis UI Components
 * - AI Orb with animations and center content support
 * - Resource rings
 * - Animated grid background
 */
object JarvisComponents {

    @Composable
    fun AIOrb(
        isListening: Boolean,
        centerContent: @Composable () -> Unit = {}
    ) {
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
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Outer glow
            Canvas(modifier = Modifier.fillMaxSize()) {
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
            Canvas(modifier = Modifier.size(120.dp)) {
                for (i in 0..2) {
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                        radius = (size.minDimension / 2) - (i * 12f),
                        style = Stroke(width = 1.5f)
                    )
                }
            }

            // Core orb
            Box(
                modifier = Modifier
                    .size(80.dp * pulseScale)
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
                    .blur(if (isListening) 8.dp else 0.dp),
                contentAlignment = Alignment.Center
            ) {
                // Center content (logo or custom content)
                centerContent()
            }

            // Voice wave when listening
            if (isListening) {
                VoiceWaveAnimation()
            }
        }
    }

    @Composable
    private fun VoiceWaveAnimation() {
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

        Canvas(modifier = Modifier.size(100.dp)) {
            for (i in 0..8) {
                val angle = (i * 40f) + (wave1 * 360f)
                val radius = 50f + (wave1 * 15f)
                val x = center.x + radius * cos(Math.toRadians(angle.toDouble())).toFloat()
                val y = center.y + radius * sin(Math.toRadians(angle.toDouble())).toFloat()
                
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 1f - wave1),
                    radius = 3f,
                    center = Offset(x, y)
                )
            }
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
            modifier = Modifier.width(80.dp)
        ) {
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(60.dp)) {
                    // Background circle
                    drawCircle(
                        color = Color.White.copy(alpha = 0.1f),
                        style = Stroke(width = 6f)
                    )
                    // Progress arc
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = (usage / 100f) * 360f,
                        useCenter = false,
                        style = Stroke(width = 6f, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${usage.toInt()}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 9.sp,
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
}
