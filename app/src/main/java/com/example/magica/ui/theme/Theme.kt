package com.example.magica.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7C4DFF),
    secondary = Color(0xFF448AFF),
    tertiary = Color(0xFF00BCD4),
    background = Color(0xFF0D0D1A),
    surface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFF252540),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFE0E0FF),
    onSurface = Color(0xFFE0E0FF),
    outline = Color(0xFF3A3A5C),
)

@Composable
fun MagicaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
