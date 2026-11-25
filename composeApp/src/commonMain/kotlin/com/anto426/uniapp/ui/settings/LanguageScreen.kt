package com.anto426.uniapp.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.uniapp.settings.presentation.LanguageUiState
import com.anto426.uniapp.ui.components.display.UniExpressiveHeroFrame
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniNotice
import com.anto426.uniapp.ui.components.items.LanguageItem
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.shapes.RoundedRectangle

private object LanguageHeroIcons {
    val Globe: ImageVector by lazy {
        ImageVector.Builder(
            name = "Globe",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.EvenOdd,
            ) {
                // Outer circle
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
                curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
                close()
                // Clean inner quadrants for globe meridian + equator feel
                moveTo(11f, 4.08f)
                lineTo(11f, 8f)
                lineTo(6.5f, 8f)
                curveTo(7.55f, 5.95f, 9.16f, 4.49f, 11f, 4.08f)
                close()
                moveTo(13f, 4.08f)
                curveTo(14.84f, 4.49f, 16.45f, 5.95f, 17.5f, 8f)
                lineTo(13f, 8f)
                close()
                moveTo(4.08f, 11f)
                curveTo(4.19f, 9.93f, 4.53f, 8.93f, 5.06f, 8.05f)
                lineTo(5.55f, 9.5f)
                lineTo(11f, 9.5f)
                lineTo(11f, 11f)
                close()
                moveTo(13f, 9.5f)
                lineTo(18.45f, 9.5f)
                lineTo(18.94f, 8.05f)
                curveTo(19.47f, 8.93f, 19.81f, 9.93f, 19.92f, 11f)
                lineTo(13f, 11f)
                close()
                moveTo(4.08f, 13f)
                lineTo(11f, 13f)
                lineTo(11f, 14.5f)
                lineTo(5.55f, 14.5f)
                lineTo(5.06f, 15.95f)
                curveTo(4.53f, 15.07f, 4.19f, 14.07f, 4.08f, 13f)
                close()
                moveTo(13f, 13f)
                lineTo(19.92f, 13f)
                curveTo(19.81f, 14.07f, 19.47f, 15.07f, 18.94f, 15.95f)
                lineTo(18.45f, 14.5f)
                lineTo(13f, 14.5f)
                close()
                moveTo(11f, 16f)
                lineTo(11f, 19.92f)
                curveTo(9.16f, 19.51f, 7.55f, 18.05f, 6.5f, 16f)
                close()
                moveTo(13f, 16f)
                lineTo(17.5f, 16f)
                curveTo(16.45f, 18.05f, 14.84f, 19.51f, 13f, 19.92f)
                close()
            }
        }.build()
    }

    val SpeechBubble: ImageVector by lazy {
        ImageVector.Builder(
            name = "SpeechBubble",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 2f)
                lineTo(4f, 2f)
                curveTo(2.9f, 2f, 2f, 2.9f, 2f, 4f)
                lineTo(2f, 22f)
                lineTo(6f, 18f)
                lineTo(20f, 18f)
                curveTo(21.1f, 18f, 22f, 17.1f, 22f, 16f)
                lineTo(22f, 4f)
                curveTo(22f, 2.9f, 21.1f, 2f, 20f, 2f)
                close()
            }
        }.build()
    }
}

@Composable
private fun LanguageExpressiveHero() {
    val iconTint = MaterialTheme.colorScheme.primary
    UniExpressiveHeroFrame(
        mainContent = {
            Box(
                modifier = Modifier
                    .offset(x = (-18).dp, y = (-16).dp)
                    .size(34.dp)
                    .background(iconTint.copy(alpha = 0.20f), RoundedRectangle(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "A",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = iconTint,
                )
            }

            Icon(
                imageVector = LanguageHeroIcons.Globe,
                contentDescription = null,
                tint = iconTint.copy(alpha = 0.90f),
                modifier = Modifier
                    .offset(x = 20.dp, y = (-3).dp)
                    .size(28.dp),
            )

            Icon(
                imageVector = LanguageHeroIcons.SpeechBubble,
                contentDescription = null,
                tint = iconTint.copy(alpha = 0.78f),
                modifier = Modifier
                    .offset(x = (-10).dp, y = 22.dp)
                    .size(23.dp),
            )
        },
        badgeContent = {
            // Crisp two-tone globe emblem inside the overlapping badge
            Canvas(modifier = Modifier.size(22.dp)) {
                val r = size.minDimension / 2f
                val strokeWidth = 2.dp.toPx()
                drawCircle(
                    color = iconTint,
                    radius = r - strokeWidth / 2f,
                    style = Stroke(width = strokeWidth),
                )
                drawOval(
                    color = iconTint.copy(alpha = 0.72f),
                    topLeft = Offset(size.width * 0.28f, strokeWidth / 2f),
                    size = androidx.compose.ui.geometry.Size(
                        size.width * 0.44f,
                        size.height - strokeWidth,
                    ),
                    style = Stroke(width = 1.6.dp.toPx()),
                )
                drawLine(
                    color = iconTint.copy(alpha = 0.72f),
                    start = Offset(strokeWidth, size.height / 2f),
                    end = Offset(size.width - strokeWidth, size.height / 2f),
                    strokeWidth = 1.6.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        },
    )
}

@Composable
fun LanguageScreen(
    uiState: LanguageUiState,
    onLanguageSelected: (String) -> Unit,
) {
    UniScreenColumn {
        UniAnimatedEntrance(order = 0) {
            LanguageExpressiveHero()
        }

        UniAnimatedEntrance(order = 1) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_available_languages),
                subtitle = stringResource(Res.string.ui_language_subtitle),
            )
        }

        UniInsetGroup(
            modifier = Modifier.selectableGroup(),
            entranceOrder = 1,
        ) {
            uiState.languages.forEachIndexed { index, language ->
                LanguageItem(
                    language = language,
                    isSelected = uiState.selectedLanguageCode == language.code,
                    onClick = { onLanguageSelected(language.code) },
                    showDivider = index < uiState.languages.lastIndex,
                )
            }
        }

        UniNotice(text = stringResource(Res.string.ui_language_restart))
    }
}
