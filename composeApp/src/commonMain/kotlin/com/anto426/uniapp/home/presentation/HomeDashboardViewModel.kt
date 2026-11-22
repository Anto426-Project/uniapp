package com.anto426.uniapp.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.academicContract
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.contract.NewsItem
import com.anto426.uniapp.data.examsContract
import com.anto426.uniapp.data.identityContract
import com.anto426.uniapp.data.observeIn
import com.anto426.uniapp.data.professorContract
import com.anto426.uniapp.data.taxesContract
import com.anto426.uniapp.model.home.QuickActionItem
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

class HomeDashboardViewModel(
    private val dataSource: UniGateway,
    quickActions: List<QuickActionItem>,
    private val account: UniAccountSummary? = null,
    private val preparationDispatcher: CoroutineDispatcher = Dispatchers.Default,
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

    private val repository = dataSource.asUniRepository(viewModelScope)
    private val sharedData = repository.engine
    private val dataRequests = if (account?.isProfessor == true) {
        listOf(UniDatasets.Professor, UniDatasets.News)
    } else {
        listOf(
            UniDatasets.Student,
            UniDatasets.Career,
            UniDatasets.StudyPlan,
            UniDatasets.Taxes,
            UniDatasets.Exams,
            UniDatasets.News,
        )
    }

    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, partial = true, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        val failure = snapshot.firstError?.userMessage { getString(Res.string.msg_impossibile_aggiornare_la_panoramica) }
        val preparedNews = snapshot.value(UniDatasets.News)?.let {
            repository.didactics.prepareNews(it, preparationDispatcher)
        }
        mutableUiState.update { current ->
            var next = current.copy(errorMessage = failure)
            if (account?.isProfessor != true && (snapshot.value(UniDatasets.Student) != null || account != null)) {
                val identity = snapshot.identityContract(account)
                next = next.copy(
                    profileName = identity.fullName,
                    matricola = identity.matricola,
                    profileInitials = identity.initials,
                    departmentName = identity.departmentName,
                    degreeName = identity.degreeName,
                    loadState = if (snapshot.value(UniDatasets.Student) != null) FeatureLoadState.Content else next.loadState,
                )
            }
            if (snapshot.value(UniDatasets.Career) != null || snapshot.value(UniDatasets.StudyPlan) != null) {
                val academic = snapshot.academicContract(defaultTargetCfu = 180)
                next = next.copy(
                    academicYear = academic.academicYearLabel,
                    acquiredCfu = academic.acquiredCfu.toString(),
                    targetCfu = academic.targetCfu,
                    degreeBase = academic.degreeBaseLabel,
                    average = academic.averageLabel,
                    completedExams = academic.completedExamsCount,
                    progress = academic.progress,
                    loadState = FeatureLoadState.Content,
                )
            }
            if (snapshot.value(UniDatasets.Taxes) != null) {
                val taxes = snapshot.taxesContract()
                next = next.copy(
                    dueAmount = taxes.rawDueAmount,
                    nextTaxLabel = taxes.nextUnpaidInstallmentLabel,
                )
            }
            if (snapshot.value(UniDatasets.Exams) != null) {
                val exams = snapshot.examsContract()
                next = next.copy(
                    openExamRounds = exams.openBookableCount,
                    nextExamLabel = exams.nextOpenExamLabel,
                )
            }
            preparedNews?.let { feed ->
                val items = feed.items
                next = next.copy(news = items, activeNewsIndex = next.activeNewsIndex.coerceIn(0, items.lastIndex.coerceAtLeast(0)))
            }
            if (account?.isProfessor == true) {
                val identity = snapshot.identityContract(account)
                next = next.copy(
                    isProfessor = true,
                    profileName = identity.fullName,
                    profileInitials = identity.initials,
                    matricola = identity.matricola,
                    departmentName = identity.departmentName,
                    degreeName = identity.degreeName,
                )
                if (snapshot.value(UniDatasets.Professor) != null) {
                    val professor = snapshot.professorContract()
                    next = next.copy(
                        teachingCount = professor.teachingCount,
                        thesisCount = professor.thesisCount,
                        openExamRounds = professor.openExamRoundsCount,
                        nextExamLabel = professor.nextExamLabel,
                        loadState = FeatureLoadState.Content,
                    )
                }
            }
            if (snapshot.resolved && next.loadState == FeatureLoadState.Loading && failure != null) {
                next = next.copy(loadState = FeatureLoadState.Error)
            }
            next
        }
    }

    init {
        repository.didactics.startPortrait(account)
        viewModelScope.launch {
            repository.didactics.portrait.collect { image -> mutableUiState.update { it.copy(profilePhotoData = image) } }
        }
    }

    fun refresh(force: Boolean = false) {
        repository.didactics.refresh(dataRequests, force)
        if (force) repository.didactics.refreshPortrait()
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

    fun selectNews(index: Int) {
        mutableUiState.update { state ->
            val clamped = if (state.news.isEmpty()) 0 else index.coerceIn(0, state.news.lastIndex)
            if (clamped == state.activeNewsIndex) state else state.copy(activeNewsIndex = clamped)
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
}
