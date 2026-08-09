package com.reelshelf.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors =
    lightColorScheme(
        primary = Color(0xFF102A36),
        onPrimary = Color.White,
        secondary = Color(0xFFFF8A3D),
        onSecondary = Color(0xFF102A36),
        tertiary = Color(0xFF5EC8C0),
        background = Color(0xFFEEF4F6),
        onBackground = Color(0xFF102A36),
        surface = Color(0xFFF7FBFC),
        onSurface = Color(0xFF102A36),
        surfaceVariant = Color(0xFFDCE8EC),
        onSurfaceVariant = Color(0xFF5A7380),
        outline = Color(0xFFC5D5DB),
    )

@Composable
fun ReelShelfTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
