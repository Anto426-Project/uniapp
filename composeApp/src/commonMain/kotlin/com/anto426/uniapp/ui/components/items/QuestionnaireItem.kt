package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.didactics.QuestionnaireData
import com.anto426.uniapp.model.didactics.QuestionnaireStatus
import com.kyant.backdrop.Backdrop

@Composable
fun QuestionnaireItem(
    data: QuestionnaireData,
    backdropState: Backdrop,
    onClick: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val isPending = data.status == QuestionnaireStatus.PENDING
    val isCompleted = data.status == QuestionnaireStatus.COMPLETED

    val (iconVector, iconTint, iconBg) = when {
        isCompleted -> Triple(LiquidIcons.Check, colorScheme.primary, colorScheme.primary.copy(alpha = 0.12f))
        isPending -> Triple(LiquidIcons.Feedback, colorScheme.primary, colorScheme.primary.copy(alpha = 0.16f))
        else -> Triple(LiquidIcons.Lock, colorScheme.onSurfaceVariant.copy(alpha = 0.6f), colorScheme.surfaceVariant.copy(alpha = 0.35f))
    }

    val (badgeText, badgeBg, badgeFg) = when {
        isCompleted -> Triple(stringResource(Res.string.ui_questionnaire_status_completed), colorScheme.primaryContainer, colorScheme.primary)
        isPending -> Triple(stringResource(Res.string.ui_questionnaire_status_pending), colorScheme.primary.copy(alpha = 0.16f), colorScheme.primary)
        else -> Triple(stringResource(Res.string.ui_questionnaire_status_inactive), colorScheme.surfaceVariant.copy(alpha = 0.4f), colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
    }

    LiquidCard(
        backdropState = backdropState,
        shape = RoundedCornerShape(22.dp),
        contentPadding = 16.dp,
        onClick = if (isPending) onClick else null,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.liquidIconContainer(
                    containerSize = 42.dp,
                    iconSize = 20.dp,
                    containerColor = iconBg,
                    shape = RoundedCornerShape(14.dp),
                ),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = data.course,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp,
                )
                Text(
                    text = listOf(data.prof.takeIf { it.isNotBlank() }, data.code.takeIf { it.isNotBlank() })
                        .filterNotNull()
                        .joinToString(" • "),
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.width(2.dp))

            LiquidBadge(
                text = badgeText,
                containerColor = badgeBg,
                contentColor = badgeFg,
                backdropState = backdropState,
            )
        }
    }
}
