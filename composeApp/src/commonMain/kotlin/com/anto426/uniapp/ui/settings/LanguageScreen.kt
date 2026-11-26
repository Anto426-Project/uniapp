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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.display.emptystate.LiquidExpressiveHeroFrame
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.settings.presentation.LanguageUiState
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniNotice
import com.anto426.uniapp.ui.components.items.LanguageItem
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.shapes.RoundedRectangle

@Composable
private fun LanguageExpressiveHero() {
    val iconTint = MaterialTheme.colorScheme.primary
    LiquidExpressiveHeroFrame(
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
                imageVector = LiquidIcons.Globe,
                contentDescription = null,
                tint = iconTint.copy(alpha = 0.90f),
                modifier = Modifier
                    .offset(x = 20.dp, y = (-3).dp)
                    .size(28.dp),
            )

            Icon(
                imageVector = LiquidIcons.SpeechBubble,
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
