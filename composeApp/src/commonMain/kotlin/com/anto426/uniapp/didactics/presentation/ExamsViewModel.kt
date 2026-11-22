package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.contract.AcademicContentItem
import com.anto426.uniapp.data.contract.ExamSession
import com.anto426.uniapp.data.examsContract
import com.anto426.uniapp.data.observeIn
import com.anto426.uniapp.data.professorContract
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.localization.readAppString
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import uniapp.composeapp.generated.resources.*

data class ExamsUiState(
    val isProfessor: Boolean = false,
    val selectedTab: Int = 0,
    val exams: List<ExamSession> = emptyList(),
    val professorExamRounds: List<AcademicContentItem> = emptyList(),
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
    val mutatingExamId: String? = null,
) {
    val bookableCount: Int get() = exams.count { !it.isBooked }
    val bookedCount: Int get() = exams.count { it.isBooked }
    val visibleExams: List<ExamSession> get() = exams.filter { it.isBooked == (selectedTab == 1) }
    val visibleProfessorExamRounds: List<AcademicContentItem>
        get() = professorExamRounds
}

class ExamsViewModel(
    private val dataSource: UniGateway,
    private val toastSink: AppToastSink = AppToastSink.None,
    private val today: () -> LocalDate = { Clock.System.todayIn(TimeZone.currentSystemDefault()) },
    private val account: UniAccountSummary? = null,
    initialTab: Int = 0,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(
        ExamsUiState(
            isProfessor = account?.isProfessor == true,
            selectedTab = if (account?.isProfessor == true) 0 else initialTab.coerceIn(0, 2),
        ),
    )
    val uiState: StateFlow<ExamsUiState> = mutableUiState.asStateFlow()

    private val repository = dataSource.asUniRepository(viewModelScope)
    private val sharedData = repository.engine
    private val dataRequests = listOf(if (account?.isProfessor == true) UniDatasets.Professor else UniDatasets.Exams)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        mutableUiState.value = mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
        try {
            if (account?.isProfessor == true) {
                val rounds = snapshot.professorContract(requireDashboard = true).examRounds
                mutableUiState.value = mutableUiState.value.copy(
                    isProfessor = true,
                    professorExamRounds = rounds,
                    loadState = if (rounds.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
                )
                return@observeIn
            }
            val exams = snapshot.examsContract(today = today(), requireRounds = true)
            mutableUiState.value = mutableUiState.value.copy(
                exams = exams.upcomingRounds,
                loadState = if (exams.upcomingRounds.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
            )
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage { readAppString(Res.string.msg_impossibile_caricare_gli_appelli) },
            )
        }
    }

    fun refresh(force: Boolean = false) {
        repository.didactics.refresh(dataRequests, force)
    }

    fun selectTab(index: Int) {
        if (mutableUiState.value.isProfessor) return
        mutableUiState.value = mutableUiState.value.copy(selectedTab = index.coerceIn(0, 2))
    }

    fun toggleBooking(examId: String) {
        if (mutableUiState.value.isProfessor || mutableUiState.value.mutatingExamId != null) return
        val session = mutableUiState.value.exams.firstOrNull { it.id == examId } ?: return
        mutableUiState.value = mutableUiState.value.copy(mutatingExamId = examId, errorMessage = null)
        viewModelScope.launch {
            try {
                if (!session.isBooked && !session.canBook) {
                    toastSink.error(Res.string.ui_exam_not_yet_bookable)
                    return@launch
                }
                val message = repository.didactics.toggleExamBooking(examId, today())
                toastSink.success(message)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                val message = error.userMessage { readAppString(Res.string.msg_operazione_sull_appello_non_riuscita) }
                toastSink.error(message)
            } finally {
                mutableUiState.value = mutableUiState.value.copy(mutatingExamId = null)
            }
        }
    }

    fun openCalendar(examId: String, openUrl: (String) -> Unit) {
        val url = mutableUiState.value.exams.firstOrNull { it.id == examId }?.calendarUrl
        viewModelScope.launch {
            if (url == null) {
                toastSink.error(Res.string.ui_exam_calendar_date_missing)
                return@launch
            }
            try { openUrl(url) }
            catch (error: CancellationException) { throw error }
            catch (_: Exception) { toastSink.error(Res.string.ui_exam_calendar_open_failed) }
        }
    }
}
