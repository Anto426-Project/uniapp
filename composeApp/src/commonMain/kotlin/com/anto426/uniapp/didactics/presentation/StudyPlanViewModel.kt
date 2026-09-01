package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.toStudyYears
import com.anto426.uniapp.model.didactics.StudyYear
import com.anto426.uniapp.presentation.FeatureLoadState
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
        get() = if (selectedYearIndex == 0) years else listOfNotNull(years.getOrNull(selectedYearIndex - 1))
}

class StudyPlanViewModel(private val dataSource: UniAppDataSource) : ViewModel() {
    private val mutableUiState = MutableStateFlow(StudyPlanUiState())
    val uiState: StateFlow<StudyPlanUiState> = mutableUiState.asStateFlow()

    init { refresh() }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(loadState = FeatureLoadState.Loading, errorMessage = null)
            try {
                val years = dataSource.loadStudyPlan(force).toStudyYears()
                mutableUiState.value = mutableUiState.value.copy(
                    years = years,
                    loadState = if (years.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(
                    loadState = FeatureLoadState.Error,
                    errorMessage = error.userMessage("Impossibile caricare il piano di studi."),
                )
            }
        }
    }

    fun selectYear(index: Int) {
        mutableUiState.value = mutableUiState.value.copy(
            selectedYearIndex = index.coerceIn(0, mutableUiState.value.years.size),
        )
    }
}
