package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.academicContract
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.contract.ExamRecord
import com.anto426.uniapp.data.observeIn
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

const val ALL_ACADEMIC_YEARS = -1

data class TranscriptsUiState(
    val selectedYear: Int = ALL_ACADEMIC_YEARS,
    val examsByYear: Map<Int, List<ExamRecord>> = emptyMap(),
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
    val studyPlanYears: List<Int> = emptyList(),
    val averageLabel: String = "—",
    val degreeBaseLabel: String = "—",
    val acquiredCfu: Int = 0,
    val targetCfu: Int = 180,
    val totalExamsCount: Int = 0,
    val lodeCount: Int = 0,
) {
    val availableYears: List<Int>
        get() = (studyPlanYears + examsByYear.keys).distinct()
            .sortedWith(compareBy<Int> { it == 0 }.thenBy { it })

    val displayedYears: List<Int>
        get() = if (selectedYear == ALL_ACADEMIC_YEARS) {
            availableYears
        } else {
            listOfNotNull(selectedYear.takeIf { it in availableYears })
        }
}

class TranscriptsViewModel(private val dataSource: UniGateway) : ViewModel() {
    private val mutableUiState = MutableStateFlow(TranscriptsUiState())
    val uiState: StateFlow<TranscriptsUiState> = mutableUiState.asStateFlow()

    private val repository = dataSource.asUniRepository(viewModelScope)
    private val sharedData = repository.engine
    private val dataRequests = listOf(UniDatasets.Career, UniDatasets.StudyPlan)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        mutableUiState.value = mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
        try {
            val academic = snapshot.academicContract(requireCareer = true, defaultTargetCfu = 180)
            val availableYears = academic.availableYears
            val previous = mutableUiState.value
            val hasPreviousData = previous.examsByYear.isNotEmpty() || previous.studyPlanYears.isNotEmpty()
            val currentYear = previous.selectedYear
            val selectedYear = when {
                !hasPreviousData -> if (availableYears.size > 1) ALL_ACADEMIC_YEARS else (availableYears.firstOrNull() ?: 0)
                currentYear == ALL_ACADEMIC_YEARS && availableYears.size > 1 -> ALL_ACADEMIC_YEARS
                currentYear in availableYears -> currentYear
                else -> availableYears.firstOrNull() ?: 0
            }

            mutableUiState.value = mutableUiState.value.copy(
                examsByYear = academic.examsByYear,
                selectedYear = selectedYear,
                studyPlanYears = academic.studyPlanYears,
                averageLabel = academic.averageLabel,
                degreeBaseLabel = academic.degreeBaseLabel,
                acquiredCfu = academic.acquiredCfu,
                targetCfu = academic.targetCfu,
                totalExamsCount = academic.transcriptExams.size,
                lodeCount = academic.lodeCount,
                loadState = if (academic.transcriptExams.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
            )
            snapshot.state(UniDatasets.Career).error?.let { throw it }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage { getString(Res.string.msg_impossibile_caricare_il_libretto) },
            )
        }
    }

    fun refresh(force: Boolean = false) {
        repository.didactics.refresh(dataRequests, force)
    }

    fun selectYear(year: Int) {
        val available = mutableUiState.value.availableYears
        mutableUiState.value = mutableUiState.value.copy(
            selectedYear = when {
                year == ALL_ACADEMIC_YEARS && available.size > 1 -> ALL_ACADEMIC_YEARS
                year in available -> year
                else -> available.firstOrNull() ?: 0
            },
        )
    }
}
