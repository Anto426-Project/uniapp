package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.contract.QuestionnaireData
import com.anto426.uniapp.data.observeIn
import com.anto426.uniapp.data.surveysContract
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

data class QuestionnairesUiState(
    val pending: List<QuestionnaireData> = emptyList(),
    val completed: List<QuestionnaireData> = emptyList(),
    val unavailable: List<QuestionnaireData> = emptyList(),
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
) {
    val totalCount: Int get() = pending.size + completed.size
    val completedProgress: Float
        get() = if (totalCount == 0) 0f else completed.size.toFloat() / totalCount
}

class QuestionnairesViewModel(private val dataSource: UniGateway) : ViewModel() {
    private val mutableUiState = MutableStateFlow(QuestionnairesUiState())
    val uiState: StateFlow<QuestionnairesUiState> = mutableUiState.asStateFlow()

    private val repository = dataSource.asUniRepository(viewModelScope)
    private val sharedData = repository.engine
    private val dataRequests = listOf(UniDatasets.Surveys)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        mutableUiState.value = mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
        try {
            val surveys = snapshot.surveysContract(requireSurveys = true)
            mutableUiState.value = QuestionnairesUiState(
                pending = surveys.pending,
                completed = surveys.completed,
                unavailable = surveys.unavailable,
                loadState = if (surveys.all.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
            )
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage { getString(Res.string.msg_impossibile_caricare_i_questionari) },
            )
        }
    }

    fun refresh(force: Boolean = false) {
        repository.didactics.refresh(dataRequests, force)
    }
}
