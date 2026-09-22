package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SelloRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF52151A),
    onPrimaryContainer = Color(0xFFFFDADA),
    secondary = AccentBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1C2740),
    onSecondaryContainer = Color(0xFFD2E4FF),
    background = DarkBg,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkSurface2,
    onSurfaceVariant = DarkInk2,
    outline = DarkLine
)

private val LightColorScheme = lightColorScheme(
    primary = SelloRed,
    onPrimary = Color.White,
    primaryContainer = SelloRedLight,
    onPrimaryContainer = Color(0xFF410002),
    secondary = AccentBlue,
    onSecondary = Color.White,
    secondaryContainer = AccentBlueSoft,
    onSecondaryContainer = Color(0xFF001D4D),
    background = LightBg,
    onBackground = LightInk,
    surface = LightSurface,
    onSurface = LightInk,
    surfaceVariant = LightSurface2,
    onSurfaceVariant = LightInk2,
    outline = LightLine
)

@Composable
fun NiHaoLabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
