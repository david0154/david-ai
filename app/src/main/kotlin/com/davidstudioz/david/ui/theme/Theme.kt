package com.davidstudioz.david.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0088CC),
    secondary = Color(0xFFFF6B35),
    tertiary = Color(0xFF00BCD4)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF0088CC),
    secondary = Color(0xFFFF6B35),
    tertiary = Color(0xFF00BCD4)
)

@Composable
fun DavidAITheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}