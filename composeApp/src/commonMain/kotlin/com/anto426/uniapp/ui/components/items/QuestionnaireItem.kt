package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.data.contract.QuestionnaireData
import com.anto426.uniapp.data.contract.QuestionnaireStatus
import com.anto426.uniapp.ui.components.display.UniInsetRow
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

    val (iconVector, accentColor) = when {
        isCompleted -> LiquidIcons.Check to glassColors.success
        isPending -> LiquidIcons.Feedback to colorScheme.primary
        else -> LiquidIcons.Lock to colorScheme.onSurfaceVariant
    }
    val subtitleText = listOfNotNull(
        data.prof.takeIf { it.isNotBlank() },
        data.code.takeIf { it.isNotBlank() },
    ).joinToString(" • ").ifBlank { null }

    UniInsetRow(
        title = data.course,
        subtitle = subtitleText,
        icon = iconVector,
        iconTint = accentColor,
        onClick = if (isPending) onClick else null,
        showDivider = showDivider,
        showTrailingChevron = isPending,
        trailingContent = {
            when {
                isPending -> LiquidBadge(
                    text = stringResource(Res.string.ui_questionnaire_status_pending),
                    containerColor = colorScheme.primary.copy(alpha = 0.14f),
                    contentColor = colorScheme.primary,
                )
                isCompleted -> Box(
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
                else -> LiquidBadge(
                    text = stringResource(Res.string.ui_questionnaire_status_inactive),
                    containerColor = colorScheme.onSurface.copy(alpha = 0.06f),
                    contentColor = colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}
