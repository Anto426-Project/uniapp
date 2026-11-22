package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.QuestionnairesUiState
import com.anto426.uniapp.data.contract.QuestionnaireData
import com.anto426.uniapp.ui.components.display.UniExpressiveHeroFrame
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.display.UniSecurityFooterNote
import com.anto426.uniapp.ui.components.items.QuestionnaireItem
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
private fun QuestionnairesExpressiveHero() {
    val iconTint = MaterialTheme.colorScheme.primary
    UniExpressiveHeroFrame(
        mainContent = {
            Box(
                modifier = Modifier
                    .offset(x = (-16).dp, y = (-14).dp)
                    .size(34.dp)
                    .background(iconTint.copy(alpha = 0.20f), RoundedRectangle(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = LiquidIcons.Check,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp),
                )
            }

            Icon(
                imageVector = LiquidIcons.Feedback,
                contentDescription = null,
                tint = iconTint.copy(alpha = 0.90f),
                modifier = Modifier
                    .offset(x = 16.dp, y = (-2).dp)
                    .size(28.dp),
            )

            Icon(
                imageVector = LiquidIcons.Star,
                contentDescription = null,
                tint = iconTint.copy(alpha = 0.78f),
                modifier = Modifier
                    .offset(x = (-10).dp, y = 22.dp)
                    .size(22.dp),
            )
        },
        badgeContent = {
            Canvas(modifier = Modifier.size(22.dp)) {
                val stroke = 2.dp.toPx()
                drawRoundRect(
                    color = iconTint,
                    topLeft = Offset(size.width * 0.14f, size.height * 0.14f),
                    size = Size(size.width * 0.72f, size.height * 0.72f),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    style = Stroke(width = stroke),
                )
                drawLine(
                    color = iconTint.copy(alpha = 0.8f),
                    start = Offset(size.width * 0.32f, size.height * 0.42f),
                    end = Offset(size.width * 0.68f, size.height * 0.42f),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = iconTint.copy(alpha = 0.8f),
                    start = Offset(size.width * 0.32f, size.height * 0.60f),
                    end = Offset(size.width * 0.58f, size.height * 0.60f),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        },
    )
}

@Composable
fun QuestionnairesScreen(
    uiState: QuestionnairesUiState,
    onQuestionnaireClick: (QuestionnaireData) -> Unit,
) {
    val isEmpty = uiState.pending.isEmpty() && uiState.completed.isEmpty() && uiState.unavailable.isEmpty()

    UniScreenColumn {
        UniAnimatedEntrance(order = 0) {
            QuestionnairesExpressiveHero()
        }

        if (uiState.pending.isNotEmpty()) {
            QuestionnaireSectionGroup(
                title = stringResource(Res.string.ui_questionnaires_pending),
                subtitle = stringResource(Res.string.ui_questionnaires_pending_subtitle),
                items = uiState.pending,
                entranceOrder = 1,
                onItemClick = onQuestionnaireClick,
            )
        }

        if (uiState.completed.isNotEmpty()) {
            QuestionnaireSectionGroup(
                title = stringResource(Res.string.ui_questionnaires_completed),
                subtitle = stringResource(Res.string.ui_questionnaires_completed_subtitle),
                items = uiState.completed,
                entranceOrder = 2,
            )
        }

        if (uiState.unavailable.isNotEmpty()) {
            QuestionnaireSectionGroup(
                title = stringResource(Res.string.ui_questionnaires_unavailable),
                subtitle = stringResource(Res.string.ui_questionnaires_unavailable_desc),
                items = uiState.unavailable,
                entranceOrder = 3,
            )
        }

        if (isEmpty) {
            UniInsetGroup(entranceOrder = 1) {
                UniInsetRow(
                    title = stringResource(Res.string.ui_questionnaires_empty_title),
                    subtitle = stringResource(Res.string.ui_questionnaires_empty_desc),
                    icon = LiquidIcons.Feedback,
                    showDivider = false,
                )
            }
        }

        UniSecurityFooterNote(text = stringResource(Res.string.ui_questionnaire_note))
    }
}

@Composable
private fun QuestionnaireSectionGroup(
    title: String,
    subtitle: String,
    items: List<QuestionnaireData>,
    entranceOrder: Int = 0,
    onItemClick: ((QuestionnaireData) -> Unit)? = null,
) {
    UniAnimatedEntrance(order = entranceOrder) {
        LiquidSectionHeader(
            title = title,
            subtitle = subtitle,
        )
    }

    UniInsetGroup(entranceOrder = entranceOrder) {
        items.forEachIndexed { index, data ->
            key("${data.code}|${data.course}|$index") {
                QuestionnaireItem(
                    data = data,
                    onClick = if (onItemClick != null) { { onItemClick(data) } } else { {} },
                    showDivider = index < items.lastIndex,
                )
            }
        }
    }
}
