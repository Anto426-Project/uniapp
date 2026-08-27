package com.anto426.uniapp.android.ui.components.interactive

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UniCelebration(
    onClose: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "uniCelebrationClean")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(26000, easing = LinearEasing)),
        label = "uniCelebrationRotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.surface)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClose() })
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.5f, size.height * 0.50f)
            val radius = size.minDimension * 0.34f
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(scheme.surfaceVariant, scheme.surface, scheme.background)
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(scheme.primary.copy(alpha = 0.28f), Color.Transparent),
                    center,
                    radius * 1.45f
                ),
                center = center,
                radius = radius * 1.45f
            )
            rotate(rotation, pivot = center) {
                drawArc(
                    color = scheme.primary.copy(alpha = 0.72f),
                    startAngle = -35f,
                    sweepAngle = 82f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = scheme.tertiary.copy(alpha = 0.46f),
                    startAngle = 150f,
                    sweepAngle = 58f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius * 1.10f, center.y - radius * 1.10f),
                    size = Size(radius * 2.20f, radius * 2.20f),
                    style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "NEVER",
                color = scheme.onSurface,
                fontSize = 42.sp,
                lineHeight = 43.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 5.sp
            )
            Text(
                text = "SETTLE",
                color = scheme.tertiary,
                fontSize = 42.sp,
                lineHeight = 43.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 5.sp
            )
        }
    }
}
