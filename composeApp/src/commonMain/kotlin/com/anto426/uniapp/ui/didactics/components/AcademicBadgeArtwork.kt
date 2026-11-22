package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.uniapp.codes.QrCodeStyle
import com.kyant.shapes.RoundedRectangle

/** Engraved contour ribbons, cached at the card size; the QR quiet zone stays opaque. */
@Composable
internal fun AcademicBadgeArtwork(modifier: Modifier = Modifier) {
    val palette = MaterialTheme.colorScheme
    Box(modifier.drawWithCache {
        val ribbons = List(14) { index ->
            val shift = index * size.height * .028f
            Path().apply {
                moveTo(-size.width * .12f, size.height * .38f + shift)
                cubicTo(size.width * .26f, -size.height * .24f + shift,
                    size.width * .50f, size.height * 1.12f + shift,
                    size.width * 1.16f, size.height * .16f + shift)
            }
        }
        val ink = Brush.linearGradient(listOf(
            palette.primary.copy(alpha = .04f),
            palette.secondary.copy(alpha = .20f),
            palette.tertiary.copy(alpha = .08f),
        ))
        onDrawBehind {
            ribbons.forEach { drawPath(it, ink, style = Stroke(.7.dp.toPx())) }
            drawCircle(palette.primary.copy(alpha = .10f), size.width * .18f,
                Offset(size.width * .94f, size.height * .10f), style = Stroke(1.dp.toPx()))
            drawCircle(palette.tertiary.copy(alpha = .12f), size.width * .22f,
                Offset(size.width * .94f, size.height * .10f), style = Stroke(.7.dp.toPx()))
        }
    })
}

internal data class UniQrThemePalette(
    val plate: Color,
    val module: Color,
    val accent: Color,
)

/**
 * Derives distinctly colored Material You tokens for QR plates & ink while
 * preserving >= 5:1–7:1 optical contrast across light, dark, and custom seeds.
 */
@Composable
internal fun rememberUniQrThemePalette(): UniQrThemePalette {
    val scheme = MaterialTheme.colorScheme
    return remember(
        scheme.primary,
        scheme.primaryContainer,
        scheme.onPrimary,
        scheme.onPrimaryContainer,
        scheme.tertiary,
        scheme.tertiaryContainer,
        scheme.onTertiary,
        scheme.onTertiaryContainer,
    ) {
        val lightContainer = if (scheme.primaryContainer.luminance() >= scheme.onPrimaryContainer.luminance()) {
            scheme.primaryContainer
        } else {
            scheme.onPrimaryContainer
        }
        val plate = lerp(lightContainer, Color.White, 0.08f).ensureMinLuminance(0.76f)

        val deepPrimary = if (scheme.onPrimaryContainer.luminance() < scheme.primaryContainer.luminance()) {
            lerp(scheme.onPrimaryContainer, scheme.primary, 0.35f)
        } else {
            lerp(scheme.onPrimary, scheme.primaryContainer, 0.45f)
        }
        val module = deepPrimary.ensureMaxLuminance(0.085f)

        val deepTertiary = if (scheme.onTertiaryContainer.luminance() < scheme.tertiaryContainer.luminance()) {
            lerp(scheme.onTertiaryContainer, scheme.tertiary, 0.35f)
        } else {
            lerp(scheme.onTertiary, scheme.tertiaryContainer, 0.45f)
        }
        val accent = deepTertiary.ensureMaxLuminance(0.10f)

        UniQrThemePalette(
            plate = plate,
            module = module,
            accent = accent,
        )
    }
}

private fun Color.ensureMinLuminance(minLuminance: Float): Color {
    if (luminance() >= minLuminance) return this
    var low = 0f
    var high = 1f
    repeat(14) {
        val mid = (low + high) * 0.5f
        if (lerp(this, Color.White, mid).luminance() < minLuminance) low = mid else high = mid
    }
    return lerp(this, Color.White, high)
}

private fun Color.ensureMaxLuminance(maxLuminance: Float): Color {
    if (luminance() <= maxLuminance) return this
    val fallbackInk = lerp(Color(0xFF0A080C), this, 0.18f)
    var low = 0f
    var high = 1f
    repeat(14) {
        val mid = (low + high) * 0.5f
        if (lerp(this, Color.Black, mid).luminance() > maxLuminance) low = mid else high = mid
    }
    return lerp(this, Color.Black, high).takeIf { it.luminance() > 0.004f } ?: fallbackInk
}

/** Single soft Material You squircle plate with theme-colored high-contrast QR ink. */
@Composable
internal fun AcademicBadgeQr(value: String, modifier: Modifier = Modifier) {
    val qrPalette = rememberUniQrThemePalette()

    Box(
        modifier
            .size(190.dp)
            .clip(RoundedRectangle(28.dp))
            .background(qrPalette.plate)
            .padding(6.dp),
    ) {
        QrCodeMatrixCanvas(
            codeValue = value,
            modifier = Modifier.fillMaxSize(),
            color = qrPalette.module,
            accentColor = qrPalette.accent,
            backgroundColor = qrPalette.plate,
            style = QrCodeStyle.Artistic,
        )
    }
}

/** Soft, clutter-free theme-tinted barcode plate with plain monospace code underneath. */
@Composable
internal fun UniThemedBarcodePlate(
    codeValue: String,
    painter: Painter,
    contentDescription: String,
    qrPalette: UniQrThemePalette = rememberUniQrThemePalette(),
    modifier: Modifier = Modifier,
    barcodeHeight: androidx.compose.ui.unit.Dp? = null,
    codeLetterSpacing: TextUnit = 2.sp,
) {
    Box(
        modifier = modifier
            .clip(RoundedRectangle(24.dp))
            .background(qrPalette.plate)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = if (barcodeHeight != null) {
                    Modifier.fillMaxWidth().height(barcodeHeight)
                } else {
                    Modifier.fillMaxWidth().weight(1f)
                },
            )

            Text(
                text = codeValue.uppercase(),
                color = qrPalette.module.copy(alpha = 0.82f),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                letterSpacing = codeLetterSpacing,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}
