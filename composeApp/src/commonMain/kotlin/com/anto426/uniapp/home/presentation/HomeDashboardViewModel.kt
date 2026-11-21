package com.anto426.uniapp.home.presentation

import com.anto426.uniapp.localization.readAppString
import uniapp.composeapp.generated.resources.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.account.model.UniProfileRole
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.observeIn
import com.anto426.uniapp.model.home.QuickActionItem
import com.anto426.uniapp.model.news.NewsItem
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
            UniDatasets.Taxes,
            UniDatasets.Exams,
            UniDatasets.News,
        )
    }

    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, partial = true, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        val failure = snapshot.firstError?.userMessage { readAppString(Res.string.msg_impossibile_aggiornare_la_panoramica) }
        val preparedNews = snapshot.value(UniDatasets.News)?.let {
            repository.didactics.prepareNews(it, preparationDispatcher)
        }
        mutableUiState.update { current ->
            var next = current.copy(errorMessage = failure)
            snapshot.value(UniDatasets.Student)?.let { student ->
                next = next.copy(
                    profileName = student.fullName,
                    matricola = student.matricola.orEmpty(),
                    profileInitials = student.initials,
                    departmentName = student.departmentName.orEmpty(),
                    degreeName = student.degreeName.orEmpty(),
                    loadState = FeatureLoadState.Content,
                )
            }
            snapshot.value(UniDatasets.Career)?.let { career ->
                val target = career.resolveTargetCfu(defaultTarget = 0)
                val acquired = career.acquiredCfuValue
                next = next.copy(
                    academicYear = career.year,
                    acquiredCfu = acquired.toString(),
                    targetCfu = target,
                    degreeBase = career.degreeBase ?: "—",
                    average = career.average,
                    completedExams = career.validExams.size,
                    progress = career.resolveProgress(defaultTarget = 0),
                    loadState = FeatureLoadState.Content,
                )
            }
            snapshot.value(UniDatasets.Taxes)?.let { taxes ->
                next = next.copy(
                    dueAmount = taxes.dueAmount,
                    nextTaxLabel = taxes.nextUnpaidInstallmentLabel,
                )
            }
            snapshot.value(UniDatasets.Exams)?.let { rounds ->
                val upcoming = rounds.firstOrNull { it.open && !it.booked }
                next = next.copy(
                    openExamRounds = rounds.count { it.open && !it.booked },
                    nextExamLabel = upcoming?.let { "${it.courseName} • ${it.dateTime}" }.orEmpty(),
                )
            }
            preparedNews?.let { feed ->
                val items = feed.items
                next = next.copy(news = items, activeNewsIndex = next.activeNewsIndex.coerceIn(0, items.lastIndex.coerceAtLeast(0)))
            }
            if (account?.isProfessor == true) {
                val profile = account.profiles.firstOrNull { it.profileId == account.activeProfileId }
                    ?: account.profiles.firstOrNull { it.type == UniProfileRole.PROFESSOR }
                next = next.copy(
                    isProfessor = true,
                    profileName = account.displayName,
                    profileInitials = account.displayName.initials(),
                    matricola = account.serverUserId,
                    departmentName = profile?.departmentName.orEmpty(),
                    degreeName = profile?.departmentName.orEmpty(),
                )
                snapshot.value(UniDatasets.Professor)?.let { dashboard ->
                    val round = dashboard.examRounds.firstOrNull()
                    next = next.copy(
                        teachingCount = dashboard.teachings.size,
                        thesisCount = dashboard.theses.size,
                        openExamRounds = dashboard.examRounds.size,
                        nextExamLabel = round?.subtitle ?: round?.title.orEmpty(),
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

private fun String.initials(): String =
    split(' ').filter(String::isNotBlank).take(2).map { it.first() }.joinToString("")
