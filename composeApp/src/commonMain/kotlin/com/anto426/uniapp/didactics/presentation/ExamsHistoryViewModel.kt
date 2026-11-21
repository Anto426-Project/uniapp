package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.buildExamHistory
import com.anto426.uniapp.data.observeIn
import com.anto426.uniapp.model.didactics.PastExam
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import com.anto426.uniapp.localization.readAppString
import uniapp.composeapp.generated.resources.*

data class ExamsHistoryUiState(
    val exams: List<PastExam> = emptyList(),
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
)

class ExamsHistoryViewModel(
    private val dataSource: UniGateway,
    private val today: () -> LocalDate = { Clock.System.todayIn(TimeZone.currentSystemDefault()) },
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ExamsHistoryUiState())
    val uiState: StateFlow<ExamsHistoryUiState> = mutableUiState.asStateFlow()

    private val repository = dataSource.asUniRepository(viewModelScope)
    private val sharedData = repository.engine
    private val dataRequests = listOf(UniDatasets.Career, UniDatasets.Exams)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        mutableUiState.value = mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
        try {
            val exams = buildExamHistory(
                snapshot.require(UniDatasets.Career).exams,
                snapshot.value(UniDatasets.Exams).orEmpty(),
                today(),
            )
            mutableUiState.value = ExamsHistoryUiState(
                exams = exams,
                loadState = if (exams.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
            )
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage { readAppString(Res.string.msg_impossibile_caricare_lo_storico_esami) },
            )
        }
    }

    fun refresh(force: Boolean = false) {
        repository.didactics.refresh(dataRequests, force)
    }
}
