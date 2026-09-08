package com.anto426.uniapp.didactics.presentation

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.model.didactics.ThesisData
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import com.anto426.unisdk.backend.model.ProfessorContentItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AcademicItemDetailUiState(
    val item: ProfessorContentItem? = null,
    val thesisData: ThesisData? = null,
    val detailFields: List<Pair<String, String>> = emptyList(),
    val selectedTab: Int = 0,
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
)

class AcademicItemDetailViewModel(
    private val section: AcademicSection,
    private val itemKey: String,
    private val dataSource: UniAppDataSource,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(AcademicItemDetailUiState())
    val uiState: StateFlow<AcademicItemDetailUiState> = mutableUiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            mutableUiState.value =
                mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
            try {
                val dashboard = dataSource.loadProfessorDashboard(force)
                val candidates =
                    when (section) {
                        AcademicSection.Teachings -> dashboard.teachings
                        AcademicSection.ExamRounds -> dashboard.examRounds
                        AcademicSection.Theses -> dashboard.theses
                        AcademicSection.Reports -> dashboard.reports
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
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value =
                    AcademicItemDetailUiState(
                        loadState = mutableUiState.value.loadState.onRefreshFailure(),
                        errorMessage = error.userMessage(getString(Res.string.msg_impossibile_caricare_il_dettaglio)),
                    )
            }
        }
    }

    fun selectTab(index: Int) {
        mutableUiState.value = mutableUiState.value.copy(selectedTab = index.coerceIn(0, 1))
    }
}
