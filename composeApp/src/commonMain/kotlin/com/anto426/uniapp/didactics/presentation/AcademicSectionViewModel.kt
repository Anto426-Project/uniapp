package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import com.anto426.unisdk.backend.model.ProfessorContentItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AcademicSection {
    Teachings,
    ExamRounds,
    Theses,
    Reports,
}

data class AcademicSectionUiState(
    val items: List<ProfessorContentItem> = emptyList(),
    val query: String = "",
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
) {
    val visibleItems: List<ProfessorContentItem>
        get() {
            val normalized = query.trim()
            if (normalized.isEmpty()) return items
            return items.filter { item ->
                item.title.contains(normalized, ignoreCase = true) ||
                    item.subtitle.orEmpty().contains(normalized, ignoreCase = true) ||
                    item.detail.orEmpty().contains(normalized, ignoreCase = true)
            }
        }
}

class AcademicSectionViewModel(
    private val section: AcademicSection,
    private val dataSource: UniAppDataSource,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(AcademicSectionUiState())
    val uiState: StateFlow<AcademicSectionUiState> = mutableUiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            mutableUiState.update { it.copy(loadState = FeatureLoadState.Loading, errorMessage = null) }
            try {
                val dashboard = dataSource.loadProfessorDashboard(force)
                val items =
                    when (section) {
                        AcademicSection.Teachings -> dashboard.teachings
                        AcademicSection.ExamRounds -> dashboard.examRounds
                        AcademicSection.Theses -> dashboard.theses
                        AcademicSection.Reports -> dashboard.reports
                    }
                mutableUiState.update {
                    it.copy(
                        items = items,
                        loadState = if (items.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.update {
                    it.copy(
                        loadState = FeatureLoadState.Error,
                        errorMessage = error.userMessage("Impossibile caricare i dati della docenza."),
                    )
                }
            }
        }
    }

    fun updateQuery(query: String) {
        mutableUiState.update { it.copy(query = query) }
    }
}

fun ProfessorContentItem.academicItemKey(): String =
    listOf(id, code, date, title, subtitle, detail)
        .joinToString("|") { it.orEmpty() }
        .hashCode()
        .toString()
