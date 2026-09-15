package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
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

/** Pearl inset with a colored rim; the square QR fits entirely inside the rounded white surface. */
@Composable
internal fun AcademicBadgeQr(value: String, modifier: Modifier = Modifier) {
    val palette = MaterialTheme.colorScheme
    Box(modifier.size(208.dp)
        .border(1.dp, Brush.linearGradient(listOf(
            palette.primary.copy(alpha = .68f),
            Color.White.copy(alpha = .9f),
            palette.tertiary.copy(alpha = .55f),
        )), RoundedRectangle(32.dp))
        .padding(4.dp)
        .background(Color.White.copy(alpha = .34f), RoundedRectangle(28.dp))
        .padding(4.dp)
        .background(Color.White, RoundedRectangle(24.dp))
        // Keep the full 176.dp painter and its quiet zone, clear of the rounded corners.
        .padding(8.dp)
    ) {
        QrCodeMatrixCanvas(value, Modifier.fillMaxSize(), style = QrCodeStyle.Artistic)
    }
}
