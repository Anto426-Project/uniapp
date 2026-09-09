package com.anto426.uniapp.didactics.presentation

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.runtime.*
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.toStudyYears
import com.anto426.uniapp.model.didactics.StudyYear
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudyPlanUiState(
    val selectedYearIndex: Int = 0,
    val years: List<StudyYear> = emptyList(),
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
) {
    val displayedYears: List<StudyYear>
        get() = listOfNotNull(years.getOrNull(selectedYearIndex))
}

class StudyPlanViewModel(private val dataSource: UniAppDataSource) : ViewModel() {
    private val mutableUiState = MutableStateFlow(StudyPlanUiState())
    val uiState: StateFlow<StudyPlanUiState> = mutableUiState.asStateFlow()

    private val sharedData = dataSource.sharedData(viewModelScope)
    private val dataRequests = listOf(UniAppDataRequests.StudyPlan)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        mutableUiState.value = mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
        try {
            val years = snapshot.require(UniAppDataRequests.StudyPlan).toStudyYears()
            val previous = mutableUiState.value
            val selectedYear = previous.years.getOrNull(previous.selectedYearIndex)?.yearNumber
            mutableUiState.value = mutableUiState.value.copy(
                years = years,
                selectedYearIndex = years.indexOfFirst { it.yearNumber == selectedYear }.coerceAtLeast(0),
                loadState = if (years.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
            )
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage(getString(Res.string.msg_impossibile_caricare_il_piano_di_studi)),
            )
        }

    }

    fun refresh(force: Boolean = false) {
        sharedData.refresh(dataRequests, force)
    }

    fun selectYear(index: Int) {
        val maxIndex = (mutableUiState.value.years.size - 1).coerceAtLeast(0)
        mutableUiState.value = mutableUiState.value.copy(
            selectedYearIndex = index.coerceIn(0, maxIndex),
        )
    }
}
