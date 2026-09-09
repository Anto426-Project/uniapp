package com.anto426.uniapp.didactics.presentation

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.runtime.*
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.stableUiId
import com.anto426.uniapp.model.didactics.PastExam
import com.anto426.uniapp.model.didactics.PastExamStatus
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import com.anto426.unisdk.backend.model.CareerExamData
import com.anto426.unisdk.backend.model.ExamRoundData
import com.anto426.unisdk.backend.model.isPastExamRound
import com.anto426.unisdk.backend.model.parseExamRoundDateOrNull
import com.anto426.unisdk.backend.model.parseExamRoundDateTimeOrNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

data class ExamsHistoryUiState(
    val exams: List<PastExam> = emptyList(),
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
)

class ExamsHistoryViewModel(
    private val dataSource: UniAppDataSource,
    private val today: () -> LocalDate = { Clock.System.todayIn(TimeZone.currentSystemDefault()) },
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ExamsHistoryUiState())
    val uiState: StateFlow<ExamsHistoryUiState> = mutableUiState.asStateFlow()

    private val sharedData = dataSource.sharedData(viewModelScope)
    private val dataRequests = listOf(UniAppDataRequests.Career, UniAppDataRequests.Exams)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests) { snapshot ->
        mutableUiState.value = mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
        try {
            val exams = buildExamHistory(
                snapshot.require(UniAppDataRequests.Career).exams,
                snapshot.value(UniAppDataRequests.Exams).orEmpty(),
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
                errorMessage = error.userMessage(getString(Res.string.msg_impossibile_caricare_lo_storico_esami)),
            )
        }

    }

    fun refresh(force: Boolean = false) {
        sharedData.refresh(dataRequests, force)
    }
}

internal fun buildExamHistory(
    careerExams: List<CareerExamData>,
    rounds: List<ExamRoundData>,
    today: LocalDate,
): List<PastExam> {
    val history = linkedMapOf<String, PastExam>()

    rounds
        .asSequence()
        .filter { it.booked && it.isPastExamRound(today) }
        .forEach { round ->
            val date = round.dateTime.substringBefore(' ').substringBefore('T')
            val time = when {
                'T' in round.dateTime -> round.dateTime.substringAfter('T').take(5)
                ' ' in round.dateTime -> round.dateTime.substringAfter(' ').take(5)
                else -> ""
            }
            history[examHistoryKey(round.courseName, date)] =
                PastExam(
                    id = round.stableUiId(),
                    name = round.courseName,
                    date = date,
                    time = time,
                    status = PastExamStatus.BOOKED_PAST,
                    room = round.room.takeIf(String::isNotBlank),
                    type = round.registrationTypeDescription.takeUnless { it == "N/D" },
                    professor = round.presidentFullName,
                    bookingOpenDate = round.registrationStartingDate,
                    bookingCloseDate = round.registrationEndingDate,
                    bookedUsersCount = round.totalRegistrations,
                    availableSlots = round.availableSlots,
                    notes = round.notes,
                    code = round.adsceId,
                )
        }

    careerExams.forEach { exam ->
        val key = examHistoryKey(exam.name, exam.date)
        val bookedRound = history[key]
        history[key] =
            if (bookedRound != null) {
                bookedRound.copy(
                    status = PastExamStatus.VERBALIZED,
                    grade = exam.grade,
                    cfu = exam.cfu,
                    code = exam.adsceId ?: bookedRound.code,
                )
            } else {
                PastExam(
                    id = exam.adsceId ?: "$key|${exam.grade}",
                    name = exam.name,
                    date = exam.date,
                    status = PastExamStatus.VERBALIZED,
                    grade = exam.grade,
                    cfu = exam.cfu,
                    code = exam.adsceId,
                )
            }
    }

    return history.values.sortedWith { left, right ->
        val leftDate = parseExamRoundDateTimeOrNull("${left.date} ${left.time}".trim())
        val rightDate = parseExamRoundDateTimeOrNull("${right.date} ${right.time}".trim())
        when {
            leftDate != null && rightDate != null -> rightDate.compareTo(leftDate)
            leftDate != null -> -1
            rightDate != null -> 1
            else -> left.name.compareTo(right.name, ignoreCase = true)
        }
    }
}

private fun examHistoryKey(name: String, date: String): String {
    val normalizedDate = parseExamRoundDateOrNull(date)?.toString() ?: date.trim()
    return "${name.trim().lowercase()}|$normalizedDate"
}
