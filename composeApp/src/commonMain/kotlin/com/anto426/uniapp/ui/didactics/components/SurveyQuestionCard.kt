package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import com.kyant.shapes.RoundedRectangle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.inputs.LiquidTextField
import com.anto426.liquidmonet.components.inputs.LiquidTextFieldType
import com.anto426.liquidmonet.components.selection.LiquidCheckbox
import com.anto426.liquidmonet.components.selection.LiquidRadioButton
import com.anto426.unisdk.backend.model.SurveyQuestionData
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun SurveyQuestionCard(
    index: Int,
    question: SurveyQuestionData,
    selectedAnswers: Set<String>,
    freeText: String,
    onAnswerSelected: (String, String, Boolean) -> Unit,
    onFreeTextChanged: (String, String) -> Unit,
) {
    LiquidCard(
        shape = RoundedRectangle(22.dp),
        contentPadding = 18.dp,
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
                        )
                    } else {
                        LiquidRadioButton(
                            selected = answer.answerId in selectedAnswers,
                            onClick = {
                                onAnswerSelected(question.questionId, answer.answerId, false)
                            },
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
                    label = stringResource(Res.string.ui_questionnaires_observations),
                )
            }
        }
    }
}
