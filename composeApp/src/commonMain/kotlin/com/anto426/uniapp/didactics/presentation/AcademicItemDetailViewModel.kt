package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.contract.AcademicContentItem
import com.anto426.uniapp.data.contract.ThesisData
import com.anto426.uniapp.data.contract.extractThesisData
import com.anto426.uniapp.data.contract.orderedAcademicDetailFields
import com.anto426.uniapp.data.observeIn
import com.anto426.uniapp.data.professorContract
import com.anto426.uniapp.localization.readAppString
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uniapp.composeapp.generated.resources.*

data class AcademicItemDetailUiState(
    val item: AcademicContentItem? = null,
    val thesisData: ThesisData? = null,
    val detailFields: List<Pair<String, String>> = emptyList(),
    val selectedTab: Int = 0,
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
)

class AcademicItemDetailViewModel(
    private val section: AcademicSection,
    private val itemKey: String,
    private val dataSource: UniGateway,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(AcademicItemDetailUiState())
    val uiState: StateFlow<AcademicItemDetailUiState> = mutableUiState.asStateFlow()

    private val repository = dataSource.asUniRepository(viewModelScope)
    private val sharedData = repository.engine
    private val dataRequests = listOf(UniDatasets.Professor)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        mutableUiState.value =
            mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
        try {
            val professor = snapshot.professorContract(requireDashboard = true)
            val candidates = when (section) {
                AcademicSection.Teachings -> professor.teachings
                AcademicSection.ExamRounds -> professor.examRounds
                AcademicSection.Theses -> professor.theses
                AcademicSection.Reports -> professor.reports
            }
            val item = candidates.firstOrNull { it.academicItemKey() == itemKey }
            val thesisData = if (item != null && section == AcademicSection.Theses) extractThesisData(item) else null
            val detailFields = item?.orderedAcademicDetailFields(section).orEmpty()
            mutableUiState.value =
                AcademicItemDetailUiState(
                    item = item,
                    thesisData = thesisData,
                    detailFields = detailFields,
                    selectedTab = mutableUiState.value.selectedTab,
                    loadState = if (item == null) FeatureLoadState.Empty else FeatureLoadState.Content,
                )
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value =
                AcademicItemDetailUiState(
                    loadState = mutableUiState.value.loadState.onRefreshFailure(),
                    errorMessage = error.userMessage { readAppString(Res.string.msg_impossibile_caricare_il_dettaglio) },
                )
        }
    }

    fun refresh(force: Boolean = false) {
        repository.didactics.refresh(dataRequests, force)
    }

    fun selectTab(index: Int) {
        mutableUiState.value = mutableUiState.value.copy(selectedTab = index.coerceIn(0, 1))
    }
}
