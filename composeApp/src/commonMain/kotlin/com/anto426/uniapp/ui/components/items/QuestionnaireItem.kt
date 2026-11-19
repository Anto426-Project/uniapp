package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.model.didactics.QuestionnaireData
import com.anto426.uniapp.model.didactics.QuestionnaireStatus
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun QuestionnaireItem(
    data: QuestionnaireData,
    onClick: () -> Unit = {},
    showDivider: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val isPending = data.status == QuestionnaireStatus.PENDING
    val isCompleted = data.status == QuestionnaireStatus.COMPLETED

    val (iconVector, accentColor, iconAlpha) = when {
        isCompleted -> Triple(LiquidIcons.Check, glassColors.success, 0.14f)
        isPending -> Triple(LiquidIcons.Feedback, colorScheme.primary, 0.16f)
        else -> Triple(LiquidIcons.Lock, colorScheme.onSurfaceVariant, 0.08f)
    }
    val subtitleText = listOfNotNull(
        data.prof.takeIf { it.isNotBlank() },
        data.code.takeIf { it.isNotBlank() },
    ).joinToString(" • ")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp)
                .then(if (isPending) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedRectangle(10.dp))
                    .background(accentColor.copy(alpha = iconAlpha)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = data.course,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitleText.isNotBlank()) {
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.5.sp,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            when {
                isPending -> {
                    LiquidBadge(
                        text = stringResource(Res.string.ui_questionnaire_status_pending),
                        containerColor = colorScheme.primary.copy(alpha = 0.14f),
                        contentColor = colorScheme.primary,
                    )
                    Icon(
                        imageVector = LiquidIcons.ChevronRight,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.42f),
                        modifier = Modifier.size(16.dp),
                    )
                }
                isCompleted -> {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(glassColors.success),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = LiquidIcons.Check,
                            contentDescription = stringResource(Res.string.ui_questionnaire_status_completed),
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }
                else -> {
                    LiquidBadge(
                        text = stringResource(Res.string.ui_questionnaire_status_inactive),
                        containerColor = colorScheme.onSurface.copy(alpha = 0.06f),
                        contentColor = colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (showDivider) {
            LiquidHorizontalDivider(
                modifier = Modifier.padding(start = 64.dp, end = 16.dp),
                color = colorScheme.onSurface.copy(alpha = 0.08f),
            )
        }
    }
}
