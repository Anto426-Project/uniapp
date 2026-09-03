package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidEmptyState
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.QuestionnairesUiState
import com.anto426.uniapp.ui.components.items.QuestionnaireItem
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun QuestionnairesScreen(
    backdropState: Backdrop,
    uiState: QuestionnairesUiState,
    onQuestionnaireClick: (com.anto426.uniapp.model.didactics.QuestionnaireData) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isEmpty = uiState.pending.isEmpty() && uiState.completed.isEmpty() && uiState.unavailable.isEmpty()

    UniScreenColumn {
        if (uiState.pending.isNotEmpty()) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_questionnaires_pending),
                subtitle = stringResource(Res.string.ui_questionnaires_pending_subtitle),
            )
            Column(
                modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                uiState.pending.forEach { data ->
                    QuestionnaireItem(data, backdropState) { onQuestionnaireClick(data) }
                }
            }
        }

        if (uiState.completed.isNotEmpty()) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_questionnaires_completed),
                subtitle = stringResource(Res.string.ui_questionnaires_completed_subtitle),
            )
            Column(
                modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                uiState.completed.forEach { data ->
                    QuestionnaireItem(data, backdropState)
                }
            }
        }

        if (uiState.unavailable.isNotEmpty()) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_questionnaires_unavailable),
                subtitle = stringResource(Res.string.ui_questionnaires_unavailable_desc),
            )
            Column(
                modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                uiState.unavailable.forEach { data ->
                    QuestionnaireItem(data, backdropState)
                }
            }
        }

        if (isEmpty) {
            LiquidEmptyState(
                title = stringResource(Res.string.ui_questionnaires_empty_title),
                description = stringResource(Res.string.ui_questionnaires_empty_desc),
                icon = LiquidIcons.Feedback,
                backdropState = backdropState,
            )
        }
    }
}
