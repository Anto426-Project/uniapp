package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.anto426.liquidmonet.components.inputs.LiquidTextField
import com.anto426.liquidmonet.components.inputs.LiquidTextFieldType
import com.anto426.liquidmonet.components.selection.LiquidCheckbox
import com.anto426.liquidmonet.components.selection.LiquidRadioButton
import com.anto426.uniapp.didactics.presentation.QuestionnaireDetailUiState
import com.anto426.uniapp.ui.components.layout.UniScreenLazyColumn
import com.anto426.unisdk.backend.model.SurveyQuestionData
import com.kyant.backdrop.Backdrop

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun QuestionnaireDetailScreen(
    backdropState: Backdrop,
    uiState: QuestionnaireDetailUiState,
    onAnswerSelected: (String, String, Boolean) -> Unit,
    onFreeTextChanged: (String, String) -> Unit,
    onSubmit: () -> Unit,
) {
    val questions = uiState.survey?.pages.orEmpty().flatMap { page -> page.questions }
    UniScreenLazyColumn {
        item(key = "survey-progress") {
            LiquidCard(
                backdropState = backdropState,
                shape = RoundedCornerShape(24.dp),
                contentPadding = 18.dp,
                interactiveGelatin = false,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(uiState.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(Res.string.ui_questionnaires_progress, uiState.answeredQuestions, uiState.totalQuestions),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LiquidLinearProgressIndicator(uiState.progress, backdropState = backdropState)
                }
            }
        }

        itemsIndexed(questions, key = { index, question -> "${question.pageId}|${question.questionId}|$index" }) { index, question ->
            SurveyQuestionCard(
                index = index,
                question = question,
                selectedAnswers = uiState.selectedAnswers[question.questionId].orEmpty(),
                freeText = uiState.freeTextAnswers[question.questionId].orEmpty(),
                backdropState = backdropState,
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
                backdropState = backdropState,
            )
        }
    }
}

@Composable
private fun SurveyQuestionCard(
    index: Int,
    question: SurveyQuestionData,
    selectedAnswers: Set<String>,
    freeText: String,
    backdropState: Backdrop,
    onAnswerSelected: (String, String, Boolean) -> Unit,
    onFreeTextChanged: (String, String) -> Unit,
) {
    LiquidCard(
        backdropState = backdropState,
        shape = RoundedCornerShape(22.dp),
        contentPadding = 18.dp,
        interactiveGelatin = false,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "${index + 1}. ${question.questionText}${if (question.required) " *" else ""}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            question.answers.forEach { answer ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                onAnswerSelected(question.questionId, answer.answerId, question.multipleChoice)
                            }
                            .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (question.multipleChoice) {
                        LiquidCheckbox(
                            checked = answer.answerId in selectedAnswers,
                            onCheckedChange = {
                                onAnswerSelected(question.questionId, answer.answerId, true)
                            },
                            backdropState = backdropState,
                        )
                    } else {
                        LiquidRadioButton(
                            selected = answer.answerId in selectedAnswers,
                            onClick = {
                                onAnswerSelected(question.questionId, answer.answerId, false)
                            },
                            backdropState = backdropState,
                        )
                    }
                    Text(
                        answer.answerText,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            if (question.freeTextEnabled) {
                LiquidTextField(
                    value = freeText,
                    onValueChange = { onFreeTextChanged(question.questionId, it) },
                    modifier = Modifier.fillMaxWidth(),
                    type = LiquidTextFieldType.TextArea,
                    label = "Osservazioni",
                    backdropState = backdropState,
                )
            }
        }
    }
}
