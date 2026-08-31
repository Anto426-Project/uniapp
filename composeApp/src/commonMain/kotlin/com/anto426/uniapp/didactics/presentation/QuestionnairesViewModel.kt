package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import com.anto426.uniapp.model.didactics.QuestionnaireData
import com.anto426.uniapp.model.didactics.QuestionnaireStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class QuestionnairesUiState(
    val pending: List<QuestionnaireData> = emptyList(),
    val completed: List<QuestionnaireData> = emptyList(),
) {
    val totalCount: Int get() = pending.size + completed.size
    val completedProgress: Float
        get() = if (totalCount == 0) 0f else completed.size.toFloat() / totalCount
}

class QuestionnairesViewModel(questionnaires: List<QuestionnaireData>) : ViewModel() {
    private val mutableUiState =
        MutableStateFlow(
            QuestionnairesUiState(
                pending = questionnaires.filter { it.status == QuestionnaireStatus.PENDING },
                completed = questionnaires.filter { it.status == QuestionnaireStatus.COMPLETED },
            ),
        )
    val uiState: StateFlow<QuestionnairesUiState> = mutableUiState.asStateFlow()
}
