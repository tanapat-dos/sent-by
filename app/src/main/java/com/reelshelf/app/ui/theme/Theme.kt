package com.reelshelf.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1B3A4B),
    secondary = Color(0xFFF4A261),
    background = Color(0xFFF7F4EF),
    surface = Color(0xFFF7F4EF),
    onPrimary = Color.White,
    onSecondary = Color(0xFF1B3A4B),
    onBackground = Color(0xFF1B3A4B),
    onSurface = Color(0xFF1B3A4B),
)

@Composable
fun ReelShelfTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
