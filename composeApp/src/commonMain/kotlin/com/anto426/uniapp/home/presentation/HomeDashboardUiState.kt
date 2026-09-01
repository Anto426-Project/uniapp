package com.anto426.uniapp.home.presentation

import androidx.compose.runtime.Immutable
import com.anto426.uniapp.model.home.QuickActionItem
import com.anto426.uniapp.model.news.NewsItem
import com.anto426.uniapp.presentation.FeatureLoadState

@Immutable
data class HomeDashboardUiState(
    val isProfessor: Boolean = false,
    val profileName: String = "",
    val matricola: String = "",
    val departmentName: String = "",
    val profileInitials: String = "",
    val profilePhotoData: ByteArray? = null,
    val news: List<NewsItem> = emptyList(),
    val quickActions: List<QuickActionItem> = emptyList(),
    val degreeName: String = "",
    val academicYear: String = "",
    val acquiredCfu: String = "0",
    val targetCfu: Int = 0,
    val degreeBase: String = "—",
    val average: String = "—",
    val completedExams: Int = 0,
    val progress: Float = 0f,
    val openExamRounds: Int = 0,
    val teachingCount: Int = 0,
    val thesisCount: Int = 0,
    val nextExamLabel: String = "Nessun appello disponibile",
    val dueAmount: String = "—",
    val nextTaxLabel: String = "Nessuna rata in scadenza",
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
    val selectedNews: NewsItem? = null,
    val activeNewsIndex: Int = 0,
    val selectedActionIds: Set<String> = DEFAULT_ACTION_IDS,
    val isCustomizing: Boolean = false,
) {
    val activeNews: NewsItem?
        get() = news.getOrNull(activeNewsIndex.coerceIn(0, news.lastIndex.coerceAtLeast(0)))

    val visibleQuickActions: List<QuickActionItem>
        get() = quickActions.filter { it.id in selectedActionIds }

    companion object {
        val DEFAULT_ACTION_IDS =
            setOf("libretto", "appelli", "didattica", "trasporti", "tasse", "rubrica")

        val PROFESSOR_DEFAULT_ACTION_IDS =
            setOf("insegnamenti", "appelli", "tesi", "verbali", "rubrica", "notifiche")
    }
}
