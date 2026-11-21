package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.observeIn
import com.anto426.uniapp.data.toAcademicDashboardData
import com.anto426.uniapp.model.didactics.AcademicContentItem
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.anto426.uniapp.localization.readAppString
import uniapp.composeapp.generated.resources.*

enum class AcademicSection {
    Teachings,
    ExamRounds,
    Theses,
    Reports,
}

data class AcademicSectionUiState(
    val items: List<AcademicContentItem> = emptyList(),
    val query: String = "",
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
) {
    val visibleItems: List<AcademicContentItem>
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
    private val dataSource: UniGateway,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(AcademicSectionUiState())
    val uiState: StateFlow<AcademicSectionUiState> = mutableUiState.asStateFlow()

    private val repository = dataSource.asUniRepository(viewModelScope)
    private val sharedData = repository.engine
    private val dataRequests = listOf(UniDatasets.Professor)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        mutableUiState.update { it.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null) }
        try {
            val dashboard = snapshot.require(UniDatasets.Professor).toAcademicDashboardData()
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
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.update {
                it.copy(
                    loadState = mutableUiState.value.loadState.onRefreshFailure(),
                    errorMessage = error.userMessage { readAppString(Res.string.msg_impossibile_caricare_i_dati_della_docenza) },
                )
            }
        }
    }

    fun refresh(force: Boolean = false) {
        repository.didactics.refresh(dataRequests, force)
    }

    fun updateQuery(query: String) {
        mutableUiState.update { it.copy(query = query) }
    }
}

fun AcademicContentItem.academicItemKey(): String = itemKey()
