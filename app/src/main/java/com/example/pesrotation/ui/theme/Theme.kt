package com.example.pesrotation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BroadcastColorScheme = darkColorScheme(
    primary = BroadcastLime,
    onPrimary = BroadcastBackground,
    secondary = BroadcastGreen,
    onSecondary = BroadcastBackground,
    background = BroadcastBackground,
    onBackground = BroadcastText,
    surface = BroadcastSurface,
    onSurface = BroadcastText,
    surfaceVariant = BroadcastSurfaceHigh,
    onSurfaceVariant = BroadcastTextMuted,
    outline = BroadcastLine,
    error = BroadcastDanger,
)

@Composable
fun PesRotationTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BroadcastColorScheme,
        typography = Typography,
        content = content,
    )
}
