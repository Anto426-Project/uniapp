package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.stableUiId
import com.anto426.uniapp.data.toExamSessions
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.model.didactics.ExamSession
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import com.anto426.unisdk.backend.model.ExamRoundData
import com.anto426.unisdk.backend.model.ProfessorContentItem
import com.anto426.unisdk.backend.model.examDateTimeOrNull
import com.anto426.unisdk.backend.model.isPastExamRound
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class ExamsUiState(
    val isProfessor: Boolean = false,
    val selectedTab: Int = 0,
    val exams: List<ExamSession> = emptyList(),
    val professorExamRounds: List<ProfessorContentItem> = emptyList(),
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
    val mutatingExamId: String? = null,
) {
    val bookableCount: Int get() = exams.count { !it.isBooked }
    val bookedCount: Int get() = exams.count { it.isBooked }
    val visibleExams: List<ExamSession> get() = exams.filter { it.isBooked == (selectedTab == 1) }
    val visibleProfessorExamRounds: List<ProfessorContentItem>
        get() = professorExamRounds
}

class ExamsViewModel(
    private val dataSource: UniAppDataSource,
    private val toastSink: AppToastSink = AppToastSink.None,
    private val today: () -> LocalDate = { Clock.System.todayIn(TimeZone.currentSystemDefault()) },
    private val account: UniAccountSummary? = null,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ExamsUiState())
    val uiState: StateFlow<ExamsUiState> = mutableUiState.asStateFlow()
    private var roundsById: Map<String, ExamRoundData> = emptyMap()

    init { refresh() }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(loadState = FeatureLoadState.Loading, errorMessage = null)
            try {
                if (account?.isProfessor == true) {
                    val rounds = dataSource.loadProfessorDashboard(force).examRounds
                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isProfessor = true,
                            professorExamRounds = rounds,
                            loadState = if (rounds.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
                            mutatingExamId = null,
                        )
                    return@launch
                }
                val rounds = dataSource.loadExamRounds(force)
                val upcomingRounds = rounds
                    .filterNot { it.isPastExamRound(today()) }
                    .sortedWith(EXAM_ROUND_ASCENDING)
                roundsById = upcomingRounds.associateBy(ExamRoundData::stableUiId)
                mutableUiState.value = mutableUiState.value.copy(
                    exams = upcomingRounds.toExamSessions(),
                    loadState = if (upcomingRounds.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
                    mutatingExamId = null,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(
                    loadState = FeatureLoadState.Error,
                    errorMessage = error.userMessage("Impossibile caricare gli appelli."),
                    mutatingExamId = null,
                )
            }
        }
    }

    fun selectTab(index: Int) {
        if (mutableUiState.value.isProfessor) return
        mutableUiState.value = mutableUiState.value.copy(selectedTab = index.coerceIn(0, 1))
    }

    fun toggleBooking(examId: String) {
        if (mutableUiState.value.isProfessor) return
        val round = roundsById[examId] ?: return
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(mutatingExamId = examId, errorMessage = null)
            try {
                val message = if (round.booked) dataSource.cancelExamRound(round) else dataSource.bookExamRound(round)
                toastSink.success(message)
                refresh(force = true)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                val message = error.userMessage("Operazione sull'appello non riuscita.")
                mutableUiState.value = mutableUiState.value.copy(mutatingExamId = null)
                toastSink.error(message)
            }
        }
    }

    private companion object {
        val EXAM_ROUND_ASCENDING = Comparator<ExamRoundData> { left, right ->
            val leftDate = left.examDateTimeOrNull()
            val rightDate = right.examDateTimeOrNull()
            when {
                leftDate != null && rightDate != null -> leftDate.compareTo(rightDate)
                leftDate != null -> -1
                rightDate != null -> 1
                else -> left.courseName.compareTo(right.courseName, ignoreCase = true)
            }
        }
    }
}
