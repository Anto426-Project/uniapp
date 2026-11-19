package com.anto426.uniapp.ui.components.display

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.icons.LiquidIcons
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Hero illustrativo in stile Material 3 Expressive su sfondo completamente trasparente.
 */
@Composable
fun UniExpressiveHeroFrame(
    modifier: Modifier = Modifier,
    mainContent: @Composable BoxScope.() -> Unit,
    badgeContent: @Composable BoxScope.() -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val ringColor = colorScheme.primary.copy(alpha = 0.85f)
    val cloverTint = colorScheme.primary.copy(alpha = 0.24f)
    val pebbleTint = colorScheme.onSurfaceVariant.copy(alpha = 0.16f)
    val starburstTint = colorScheme.onSurfaceVariant.copy(alpha = 0.14f)

    val ringSize = 122.dp
    val badgeSize = 54.dp
    val badgeOffsetX = 43.dp
    val badgeOffsetY = 40.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(172.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Offscreen compositing so BlendMode.Clear cleanly erases the ring intersection behind the badge
        // without drawing any opaque circle background over the Liquid Aurora scene.
        Canvas(
            modifier = Modifier
                .width(236.dp)
                .height(164.dp)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
        ) {
            val w = size.width
            val h = size.height
            val centerPoint = Offset(w * 0.50f, h * 0.48f)
            val mainRadius = (ringSize / 2f).toPx()
            val badgeRadius = (badgeSize / 2f).toPx()
            val strokePx = 5.dp.toPx()

            drawPath(
                path = buildScallopedPath(
                    center = Offset(w * 0.15f, h * 0.19f),
                    radius = 18.dp.toPx(),
                    lobes = 4,
                    amplitude = 0.13f,
                    phaseRadians = 0.25f,
                ),
                color = cloverTint,
            )

            drawPath(
                path = buildScallopedPath(
                    center = Offset(w * 0.15f, h * 0.78f),
                    radius = 17.dp.toPx(),
                    lobes = 12,
                    amplitude = 0.16f,
                    phaseRadians = 0f,
                ),
                color = starburstTint,
            )

            val pebbleCenter = Offset(w * 0.86f, h * 0.28f)
            rotate(degrees = 24f, pivot = pebbleCenter) {
                drawOval(
                    color = pebbleTint,
                    topLeft = Offset(pebbleCenter.x - 15.dp.toPx(), pebbleCenter.y - 11.dp.toPx()),
                    size = Size(30.dp.toPx(), 22.dp.toPx()),
                )
            }

            drawCircle(
                color = ringColor,
                radius = mainRadius,
                center = centerPoint,
                style = Stroke(width = strokePx),
            )

            val badgeCenter = Offset(
                x = centerPoint.x + badgeOffsetX.toPx(),
                y = centerPoint.y + badgeOffsetY.toPx(),
            )
            drawCircle(
                color = Color.Transparent,
                radius = badgeRadius + strokePx * 0.45f,
                center = badgeCenter,
                blendMode = BlendMode.Clear,
            )
            drawCircle(
                color = ringColor,
                radius = badgeRadius,
                center = badgeCenter,
                style = Stroke(width = strokePx),
            )
        }

        Box(
            modifier = Modifier
                .offset(y = (-3).dp)
                .size(ringSize - 10.dp),
            contentAlignment = Alignment.Center,
            content = mainContent,
        )

        Box(
            modifier = Modifier
                .offset(x = badgeOffsetX, y = badgeOffsetY - 3.dp)
                .size(badgeSize - 10.dp),
            contentAlignment = Alignment.Center,
            content = badgeContent,
        )
    }
}

@Composable
fun UniSecurityFooterNote(
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = LiquidIcons.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun buildScallopedPath(
    center: Offset,
    radius: Float,
    lobes: Int,
    amplitude: Float,
    phaseRadians: Float,
): Path {
    val steps = 96
    return Path().apply {
        for (i in 0 until steps) {
            val theta = (2.0 * PI * i / steps).toFloat()
            val r = radius * (1f - amplitude + amplitude * cos(lobes * theta + phaseRadians))
            val x = center.x + r * cos(theta)
            val y = center.y + r * sin(theta)
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}
