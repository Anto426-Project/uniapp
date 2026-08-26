package com.anto426.uniapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val UniDarkColorScheme = darkColorScheme(
    primary = UniColors.PrimaryViolet,
    onPrimary = Color.Black,
    primaryContainer = UniColors.PrimaryPurple,
    onPrimaryContainer = UniColors.TextPrimary,
    secondary = UniColors.PrimaryMagenta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3B1038),
    onSecondaryContainer = UniColors.TextPrimary,
    tertiary = UniColors.AccentCyan,
    onTertiary = Color.Black,
    background = UniColors.BackgroundDark,
    onBackground = UniColors.TextPrimary,
    surface = UniColors.SurfaceDark,
    onSurface = UniColors.TextPrimary,
    surfaceVariant = Color(0xFF241B35),
    onSurfaceVariant = UniColors.TextSecondary,
    outline = UniColors.SurfaceCardGlassBorder
)

private val UniLightColorScheme = lightColorScheme(
    primary = UniColors.PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E8FF),
    onPrimaryContainer = Color(0xFF3B0764),
    secondary = UniColors.PrimaryMagenta,
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    outline = Color(0xFFE2E8F0)
)

@Composable
fun UniTheme(
    darkTheme: Boolean = true, // Default to rich dark glass
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) UniDarkColorScheme else UniLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
