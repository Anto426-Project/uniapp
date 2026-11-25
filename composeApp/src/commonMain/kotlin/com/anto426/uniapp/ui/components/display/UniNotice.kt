package com.anto426.uniapp.ui.components.display

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.shapes.RoundedRectangle

enum class UniNoticeTone {
    Info,
    Primary,
    Success,
    Warning,
    Error,
}

@Composable
fun UniNotice(
    text: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector? = null,
    tone: UniNoticeTone = UniNoticeTone.Info,
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val resolvedIcon = icon ?: when (tone) {
        UniNoticeTone.Info, UniNoticeTone.Primary -> LiquidIcons.Info
        UniNoticeTone.Success -> LiquidIcons.Check
        UniNoticeTone.Warning, UniNoticeTone.Error -> LiquidIcons.Warning
    }
    val accentColor: Color = when (tone) {
        UniNoticeTone.Info, UniNoticeTone.Primary -> colorScheme.primary
        UniNoticeTone.Success -> glassColors.success
        UniNoticeTone.Warning -> glassColors.warning
        UniNoticeTone.Error -> glassColors.error
    }

    LiquidCard(
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedRectangle(20.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.20f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = resolvedIcon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.liquidIconContainer(
                    contentPadding = PaddingValues(9.dp),
                    containerSize = 36.dp,
                    containerColor = accentColor.copy(alpha = 0.14f),
                    shape = RoundedRectangle(12.dp),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.24f)),
                ),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (!title.isNullOrBlank()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (tone == UniNoticeTone.Info || tone == UniNoticeTone.Primary) {
                            glassColors.content
                        } else {
                            accentColor
                        },
                    )
                }
                if (text.isNotBlank()) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp,
                        color = glassColors.secondaryContent,
                    )
                }
            }
        }
    }
}
