package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import com.kyant.shapes.RoundedRectangle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.feedback.LiquidLinearProgressIndicator
import com.anto426.uniapp.didactics.presentation.QuestionnaireDetailUiState
import com.anto426.uniapp.ui.components.layout.UniScreenLazyColumn
import com.anto426.uniapp.ui.didactics.components.SurveyQuestionCard
import com.anto426.unisdk.backend.model.SurveyQuestionData

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun QuestionnaireDetailScreen(
    uiState: QuestionnaireDetailUiState,
    onAnswerSelected: (String, String, Boolean) -> Unit,
    onFreeTextChanged: (String, String) -> Unit,
    onSubmit: () -> Unit,
) {
    val questions = uiState.survey?.pages.orEmpty().flatMap { page -> page.questions }
    UniScreenLazyColumn {
        item(key = "survey-progress") {
            LiquidCard(
                shape = RoundedRectangle(24.dp),
                contentPadding = 18.dp,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(uiState.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(Res.string.ui_questionnaires_progress, uiState.answeredQuestions, uiState.totalQuestions),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LiquidLinearProgressIndicator(progress = uiState.progress)
                }
            }
        }

        itemsIndexed(questions, key = { index, question -> "${question.pageId}|${question.questionId}|$index" }) { index, question ->
            SurveyQuestionCard(
                index = index,
                question = question,
                selectedAnswers = uiState.selectedAnswers[question.questionId].orEmpty(),
                freeText = uiState.freeTextAnswers[question.questionId].orEmpty(),
                onAnswerSelected = onAnswerSelected,
                onFreeTextChanged = onFreeTextChanged,
            )
        }

        item(key = "survey-submit") {
            LiquidButton(
                text = if (uiState.submitted) stringResource(Res.string.ui_questionnaire_submitted) else stringResource(Res.string.ui_questionnaire_submit),
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.submitted,
                isLoading = uiState.isSubmitting,
            )
        }
    }
}
