package com.anto426.uniapp.ui.settings

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

internal data class HsvColor(
    val hue: Float,
    val saturation: Float,
    val value: Float,
)

internal fun hsvToColor(hue: Float, saturation: Float, value: Float): Color {
    val h = ((hue % 360f) + 360f) % 360f
    val s = saturation.coerceIn(0f, 1f)
    val v = value.coerceIn(0f, 1f)
    val c = v * s
    val hPrime = h / 60f
    val x = c * (1f - abs((hPrime % 2f) - 1f))
    val m = v - c

    val (r1, g1, b1) =
        when {
            hPrime < 1f -> Triple(c, x, 0f)
            hPrime < 2f -> Triple(x, c, 0f)
            hPrime < 3f -> Triple(0f, c, x)
            hPrime < 4f -> Triple(0f, x, c)
            hPrime < 5f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

    return Color(
        red = (r1 + m).coerceIn(0f, 1f),
        green = (g1 + m).coerceIn(0f, 1f),
        blue = (b1 + m).coerceIn(0f, 1f),
        alpha = 1f,
    )
}

internal fun colorToHsv(color: Color, fallbackHue: Float = 210f): HsvColor {
    val r = color.red.coerceIn(0f, 1f)
    val g = color.green.coerceIn(0f, 1f)
    val b = color.blue.coerceIn(0f, 1f)
    val max = maxOf(r, maxOf(g, b))
    val min = minOf(r, minOf(g, b))
    val delta = max - min

    val h =
        if (delta < 0.0001f) {
            ((fallbackHue % 360f) + 360f) % 360f
        } else {
            val raw =
                when (max) {
                    r -> 60f * (((g - b) / delta) % 6f)
                    g -> 60f * (((b - r) / delta) + 2f)
                    else -> 60f * (((r - g) / delta) + 4f)
                }
            if (raw < 0f) raw + 360f else raw
        }

    val s = if (max < 0.0001f) 0f else delta / max
    return HsvColor(
        hue = h.coerceIn(0f, 360f),
        saturation = s.coerceIn(0f, 1f),
        value = max.coerceIn(0f, 1f),
    )
}

internal fun colorToHex(color: Color): String {
    val r = (color.red * 255f).roundToInt().coerceIn(0, 255)
    val g = (color.green * 255f).roundToInt().coerceIn(0, 255)
    val b = (color.blue * 255f).roundToInt().coerceIn(0, 255)
    return "#${r.toHexByte()}${g.toHexByte()}${b.toHexByte()}"
}

internal fun parseHexColor(input: String): Color? {
    val clean = input.trim().removePrefix("#")
    if (clean.any { it !in '0'..'9' && it !in 'a'..'f' && it !in 'A'..'F' }) return null
    return when (clean.length) {
        3 -> {
            val r = clean.substring(0, 1).repeat(2).toInt(16) / 255f
            val g = clean.substring(1, 2).repeat(2).toInt(16) / 255f
            val b = clean.substring(2, 3).repeat(2).toInt(16) / 255f
            Color(r, g, b, 1f)
        }
        6 -> {
            val r = clean.substring(0, 2).toInt(16) / 255f
            val g = clean.substring(2, 4).toInt(16) / 255f
            val b = clean.substring(4, 6).toInt(16) / 255f
            Color(r, g, b, 1f)
        }
        else -> null
    }
}

internal fun randomHarmoniousColor(random: Random = Random.Default): Color {
    val hue = random.nextFloat() * 360f
    val saturation = 0.56f + random.nextFloat() * 0.34f
    val brightness = 0.62f + random.nextFloat() * 0.30f
    return hsvToColor(hue, saturation, brightness)
}

internal fun Color.sameRgbAs(other: Color): Boolean {
    val r1 = (red * 255f).roundToInt()
    val g1 = (green * 255f).roundToInt()
    val b1 = (blue * 255f).roundToInt()
    val r2 = (other.red * 255f).roundToInt()
    val g2 = (other.green * 255f).roundToInt()
    val b2 = (other.blue * 255f).roundToInt()
    return r1 == r2 && g1 == g2 && b1 == b2
}

private fun Int.toHexByte(): String {
    val digits = "0123456789ABCDEF"
    return "${digits[(this ushr 4) and 0x0F]}${digits[this and 0x0F]}"
}
