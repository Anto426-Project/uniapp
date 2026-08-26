package com.anto426.uniapp.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object UniColors {
    // Backgrounds & Surfaces
    val BackgroundDark = Color(0xFF0C0814)
    val SurfaceDark = Color(0xFF161022)
    val SurfaceCardGlass = Color(0x38221638)
    val SurfaceCardGlassBorder = Color(0x33B47AFF)
    val SurfaceCardGlassHighlight = Color(0x22FFFFFF)

    // Accents & Gradients
    val PrimaryViolet = Color(0xFFC084FC)
    val PrimaryMagenta = Color(0xFFFF007F)
    val PrimaryPurple = Color(0xFF9333EA)
    val AccentAmber = Color(0xFFFFB74D)
    val AccentEmerald = Color(0xFF34D399)
    val AccentCyan = Color(0xFF38BDF8)
    val AccentRose = Color(0xFFFB7185)

    // Text & Content
    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextTertiary = Color(0xFF64748B)
    val TextAccent = Color(0xFFE879F9)

    // Badges & Status
    val StatusSuccess = Color(0xFF10B981)
    val StatusWarning = Color(0xFFF59E0B)
    val StatusError = Color(0xFFEF4444)
    val StatusInfo = Color(0xFF3B82F6)

    // Gradients
    val CareerHeroGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4A154B).copy(alpha = 0.85f),
            Color(0xFF2E0854).copy(alpha = 0.95f),
            Color(0xFF130722).copy(alpha = 0.98f)
        )
    )

    val ProgressBarGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFF007F),
            Color(0xFFC084FC),
            Color(0xFF38BDF8)
        )
    )

    val GlassBorderGradient = Brush.linearGradient(
        colors = listOf(
            Color(0x88FFFFFF),
            Color(0x22C084FC),
            Color(0x11000000),
            Color(0x44FF007F)
        )
    )

    val AmbientGlowGradient = Brush.radialGradient(
        colors = listOf(
            Color(0x44C084FC),
            Color(0x22FF007F),
            Color(0x00000000)
        )
    )
}
