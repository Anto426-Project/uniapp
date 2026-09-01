package com.anto426.uniapp.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.toNewsItems
import com.anto426.uniapp.model.home.QuickActionItem
import com.anto426.uniapp.model.didactics.firstAcademicIntegerOrNull
import com.anto426.uniapp.model.news.NewsItem
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeDashboardViewModel(
    private val dataSource: UniAppDataSource,
    quickActions: List<QuickActionItem>,
    private val account: UniAccountSummary? = null,
) : ViewModel() {
    private val mutableUiState =
        MutableStateFlow(
            HomeDashboardUiState(
                quickActions = quickActions,
                isProfessor = account?.isProfessor == true,
                selectedActionIds =
                    if (account?.isProfessor == true) HomeDashboardUiState.PROFESSOR_DEFAULT_ACTION_IDS
                    else HomeDashboardUiState.DEFAULT_ACTION_IDS,
            ),
        )
    val uiState: StateFlow<HomeDashboardUiState> = mutableUiState.asStateFlow()

    init { refresh() }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            mutableUiState.update { it.copy(loadState = FeatureLoadState.Loading, errorMessage = null) }
            try {
                if (account?.isProfessor == true) {
                    loadProfessorHome(force)
                    return@launch
                }
                val snapshot = coroutineScope {
                    val student = async { dataSource.loadStudentDetails(force) }
                    val career = async { dataSource.loadCareer(force) }
                    val taxes = async { dataSource.loadTaxes(force) }
                    val rounds = async { dataSource.loadExamRounds(force) }
                    val news = async { dataSource.loadUniversityNews(force) }
                    HomeSnapshot(student.await(), career.await(), taxes.await(), rounds.await(), news.await())
                }
                val targetCfu = snapshot.career.cfuTarget ?: 0
                val acquiredCfu = snapshot.career.cfu.firstAcademicIntegerOrNull() ?: 0
                val nextRound = snapshot.rounds.firstOrNull { it.open && !it.booked }
                val nextTax = snapshot.taxes.unpaidInstallments.firstOrNull()
                mutableUiState.update { current ->
                    current.copy(
                        profileName = snapshot.student.fullName,
                        matricola = snapshot.student.matricola.orEmpty(),
                        departmentName = snapshot.student.departmentName.orEmpty(),
                        profileInitials = snapshot.student.fullName.split(' ').filter(String::isNotBlank).take(2).map { it.first() }.joinToString(""),
                        news = snapshot.news.toNewsItems(),
                        degreeName = snapshot.student.degreeName ?: snapshot.career.status,
                        academicYear = snapshot.career.year,
                        acquiredCfu = acquiredCfu.toString(),
                        targetCfu = targetCfu,
                        degreeBase = snapshot.career.degreeBase ?: "—",
                        average = snapshot.career.average,
                        completedExams = snapshot.career.exams.size,
                        progress = if (targetCfu > 0) (acquiredCfu.toFloat() / targetCfu).coerceIn(0f, 1f) else 0f,
                        openExamRounds = snapshot.rounds.count { it.open && !it.booked },
                        nextExamLabel = nextRound?.let { "${it.courseName} • ${it.dateTime}" } ?: "Nessun appello disponibile",
                        dueAmount = snapshot.taxes.dueAmount,
                        nextTaxLabel = nextTax?.let { "${it.title} • ${it.deadline}" } ?: "Nessuna rata in scadenza",
                        loadState = FeatureLoadState.Content,
                    )
                }
                snapshot.student.photoUrl?.takeIf(String::isNotBlank)?.let { source ->
                    runCatching { dataSource.loadProfileImage(source, force) }
                        .getOrNull()
                        ?.let { image ->
                            mutableUiState.update { it.copy(profilePhotoData = image) }
                        }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.update {
                    it.copy(
                        loadState = if (it.degreeName.isBlank()) FeatureLoadState.Error else FeatureLoadState.Content,
                        errorMessage = error.userMessage("Impossibile aggiornare la panoramica."),
                    )
                }
            }
        }
    }

    private suspend fun loadProfessorHome(force: Boolean) {
        val professor = checkNotNull(account)
        val snapshot = coroutineScope {
            val dashboard = async { dataSource.loadProfessorDashboard(force) }
            val news = async { dataSource.loadUniversityNews(force) }
            dashboard.await() to news.await()
        }
        val (dashboard, news) = snapshot
        val activeProfile =
            professor.profiles.firstOrNull { it.profileId == professor.activeProfileId }
                ?: professor.profiles.firstOrNull { it.type == com.anto426.unisdk.backend.model.BackendCareerType.PROFESSOR }
        val firstRound = dashboard.examRounds.firstOrNull()
        mutableUiState.update { current ->
            current.copy(
                isProfessor = true,
                profileName = professor.displayName,
                matricola = professor.serverUserId,
                departmentName = activeProfile?.departmentName.orEmpty(),
                profileInitials = professor.displayName.initials(),
                news = news.toNewsItems(),
                degreeName = activeProfile?.departmentName.orEmpty(),
                academicYear = "",
                teachingCount = dashboard.teachings.size,
                openExamRounds = dashboard.examRounds.size,
                thesisCount = dashboard.theses.size,
                nextExamLabel = firstRound?.subtitle ?: firstRound?.title ?: "",
                loadState = FeatureLoadState.Content,
                errorMessage =
                    dashboard.unavailableSections.takeIf { it.isNotEmpty() }
                        ?.let { "Alcuni dati docente non sono momentaneamente disponibili." },
            )
        }
        professor.photoUrl?.takeIf(String::isNotBlank)?.let { source ->
            runCatching { dataSource.loadProfileImage(source, force) }
                .getOrNull()
                ?.let { image -> mutableUiState.update { it.copy(profilePhotoData = image) } }
        }
    }

    fun showNews(news: NewsItem) {
        mutableUiState.update { it.copy(selectedNews = news) }
    }

    fun dismissNews() {
        mutableUiState.update { it.copy(selectedNews = null) }
    }

    fun showNextNews() {
        mutableUiState.update { state ->
            state.copy(
                activeNewsIndex =
                    if (state.news.isEmpty()) 0 else (state.activeNewsIndex + 1) % state.news.size,
            )
        }
    }

    fun showPreviousNews() {
        mutableUiState.update { state ->
            state.copy(
                activeNewsIndex =
                    when {
                        state.news.isEmpty() -> 0
                        state.activeNewsIndex > 0 -> state.activeNewsIndex - 1
                        else -> state.news.lastIndex
                    },
            )
        }
    }

    fun toggleCustomization() {
        mutableUiState.update { it.copy(isCustomizing = !it.isCustomizing) }
    }

    fun finishCustomization() {
        mutableUiState.update { it.copy(isCustomizing = false) }
    }

    fun toggleQuickAction(actionId: String) {
        mutableUiState.update { state ->
            val selected = state.selectedActionIds.toMutableSet()
            if (actionId in selected) {
                if (selected.size > MIN_ACTIONS) selected.remove(actionId)
            } else {
                selected.add(actionId)
            }
            state.copy(selectedActionIds = selected)
        }
    }

    private companion object {
        const val MIN_ACTIONS = 2
    }

    private data class HomeSnapshot(
        val student: com.anto426.unisdk.backend.model.StudentDetailsData,
        val career: com.anto426.unisdk.backend.model.CareerData,
        val taxes: com.anto426.unisdk.backend.model.TaxesData,
        val rounds: List<com.anto426.unisdk.backend.model.ExamRoundData>,
        val news: List<com.anto426.unisdk.backend.model.UniversityNews>,
    )
}

private fun String.initials(): String =
    split(' ').filter(String::isNotBlank).take(2).map { it.first() }.joinToString("")
