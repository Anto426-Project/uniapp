package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.feedback.LiquidLinearProgressIndicator
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.uniapp.ui.components.items.QuestionnaireItem
import com.anto426.uniapp.didactics.presentation.QuestionnairesUiState
import com.kyant.backdrop.Backdrop

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun QuestionnairesScreen(
    backdropState: Backdrop,
    uiState: QuestionnairesUiState,
    onQuestionnaireClick: (com.anto426.uniapp.model.didactics.QuestionnaireData) -> Unit,
) {
    UniScreenColumn {
        // 1. Progress Summary Hero
        LiquidCard(
            backdropState = backdropState,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
            contentPadding = 20.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = stringResource(Res.string.ui_questionnaire_status).uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.ui_questionnaires_progress, uiState.completed.size, uiState.totalCount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${(uiState.completedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                LiquidLinearProgressIndicator(
                    progress = uiState.completedProgress,
                    backdropState = backdropState
                )
                Text(
                    text = stringResource(Res.string.ui_questionnaire_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LiquidSectionHeader(
            title = stringResource(Res.string.ui_questionnaires_pending),
            subtitle = stringResource(Res.string.ui_questionnaires_pending_subtitle)
        )

        Column(
            modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.pending.forEach { data ->
                QuestionnaireItem(data, backdropState) { onQuestionnaireClick(data) }
            }
        }

        LiquidSectionHeader(
            title = stringResource(Res.string.ui_questionnaires_completed),
            subtitle = stringResource(Res.string.ui_questionnaires_completed_subtitle)
        )

        Column(
            modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.completed.forEach { data ->
                QuestionnaireItem(data, backdropState)
            }
        }

        if (uiState.unavailable.isNotEmpty()) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_questionnaires_unavailable),
                subtitle = stringResource(Res.string.ui_questionnaires_unavailable_desc)
            )
            Column(
                modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                uiState.unavailable.forEach { data -> QuestionnaireItem(data, backdropState) }
            }
        }
    }
}
