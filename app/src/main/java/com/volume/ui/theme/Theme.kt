package com.volume.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BoosterCyan,
    onPrimary = Color.Black,
    primaryContainer = BoosterCyanDark,
    onPrimaryContainer = Color.White,
    secondary = BoosterAmber,
    onSecondary = Color.Black,
    error = BoosterRed,
    onError = Color.White,
    background = Color.Transparent,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary
)

@Composable
fun VolumeBoosterTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
