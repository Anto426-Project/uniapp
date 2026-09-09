package com.anto426.uniapp.home.presentation

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.data.runtime.*
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.UniAppInitialData
import com.anto426.uniapp.data.toNewsItems
import com.anto426.uniapp.model.home.QuickActionItem
import com.anto426.uniapp.model.didactics.firstAcademicIntegerOrNull
import com.anto426.uniapp.model.news.NewsItem
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
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

    private val sharedData = dataSource.sharedData(viewModelScope)
    private val dataRequests = if (account?.isProfessor == true) {
        listOf(UniAppDataRequests.Professor, UniAppDataRequests.News)
    } else {
        listOf(UniAppDataRequests.Student, UniAppDataRequests.Career, UniAppDataRequests.Taxes,
            UniAppDataRequests.Exams, UniAppDataRequests.News)
    }

    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, partial = true, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        val failure = snapshot.firstError?.userMessage(getString(Res.string.msg_impossibile_aggiornare_la_panoramica))
        mutableUiState.update { current ->
            var next = current.copy(errorMessage = failure)
            snapshot.value(UniAppDataRequests.Student)?.let { student ->
                next = next.copy(profileName = student.fullName, matricola = student.matricola.orEmpty(),
                    profileInitials = student.fullName.initials(), departmentName = student.departmentName.orEmpty(),
                    degreeName = student.degreeName.orEmpty(), loadState = FeatureLoadState.Content)
            }
            snapshot.value(UniAppDataRequests.Career)?.let { career ->
                val target = career.cfuTarget ?: 0
                val acquired = career.cfu.firstAcademicIntegerOrNull() ?: 0
                next = next.copy(academicYear = career.year, acquiredCfu = acquired.toString(), targetCfu = target,
                    degreeBase = career.degreeBase ?: "—", average = career.average, completedExams = career.exams.size,
                    progress = if (target > 0) (acquired.toFloat() / target).coerceIn(0f, 1f) else 0f,
                    loadState = FeatureLoadState.Content)
            }
            snapshot.value(UniAppDataRequests.Taxes)?.let { taxes ->
                val tax = taxes.unpaidInstallments.firstOrNull()
                next = next.copy(dueAmount = taxes.dueAmount,
                    nextTaxLabel = tax?.let { "${it.title} • ${it.deadline}" }.orEmpty())
            }
            snapshot.value(UniAppDataRequests.Exams)?.let { rounds ->
                val upcoming = rounds.firstOrNull { it.open && !it.booked }
                next = next.copy(openExamRounds = rounds.count { it.open && !it.booked },
                    nextExamLabel = upcoming?.let { "${it.courseName} • ${it.dateTime}" }.orEmpty())
            }
            snapshot.value(UniAppDataRequests.News)?.let { news ->
                val items = news.toNewsItems()
                next = next.copy(news = items, activeNewsIndex = next.activeNewsIndex.coerceIn(0, items.lastIndex.coerceAtLeast(0)))
            }
            if (account?.isProfessor == true) {
                val profile = account.profiles.firstOrNull { it.profileId == account.activeProfileId }
                    ?: account.profiles.firstOrNull { it.type == com.anto426.unisdk.backend.model.BackendCareerType.PROFESSOR }
                next = next.copy(isProfessor = true, profileName = account.displayName,
                    profileInitials = account.displayName.initials(), matricola = account.serverUserId,
                    departmentName = profile?.departmentName.orEmpty(), degreeName = profile?.departmentName.orEmpty())
                snapshot.value(UniAppDataRequests.Professor)?.let { dashboard ->
                    val round = dashboard.examRounds.firstOrNull()
                    next = next.copy(teachingCount = dashboard.teachings.size, thesisCount = dashboard.theses.size,
                        openExamRounds = dashboard.examRounds.size, nextExamLabel = round?.subtitle ?: round?.title.orEmpty(),
                        loadState = FeatureLoadState.Content)
                }
            }
            if (snapshot.resolved && next.loadState == FeatureLoadState.Loading && failure != null) {
                next = next.copy(loadState = FeatureLoadState.Error)
            }
            next
        }
    }

    init {
        sharedData.startPortrait(account)
        viewModelScope.launch {
            sharedData.portrait.collect { image -> mutableUiState.update { it.copy(profilePhotoData = image) } }
        }
    }

    fun refresh(force: Boolean = false) {
        sharedData.refresh(dataRequests, force)
        if (force) sharedData.refreshPortrait()
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


}

private fun String.initials(): String =
    split(' ').filter(String::isNotBlank).take(2).map { it.first() }.joinToString("")
