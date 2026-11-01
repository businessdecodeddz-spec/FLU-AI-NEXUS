package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NexusDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color.Black,
    primaryContainer = ElectricBlue,
    onPrimaryContainer = Color.White,
    secondary = ElectricBlue,
    onSecondary = Color.White,
    secondaryContainer = GlowBlue,
    tertiary = NeonPurple,
    background = DarkNavy,
    onBackground = TextPrimary,
    surface = DeepObsidian,
    onSurface = TextPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    error = CyberRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NexusDarkColorScheme,
        typography = Typography,
        content = content
    )
}
