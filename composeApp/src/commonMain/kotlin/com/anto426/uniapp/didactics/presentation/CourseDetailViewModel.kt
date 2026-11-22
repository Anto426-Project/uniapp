package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.contract.ContactData
import com.anto426.uniapp.data.contract.StudyCourse
import com.anto426.uniapp.data.courseDetailContract
import com.anto426.uniapp.data.observeIn
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

data class CourseDetailUiState(
    val course: StudyCourse? = null,
    val professorContact: ContactData? = null,
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
)

class CourseDetailViewModel(
    private val courseId: String,
    private val dataSource: UniGateway,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(CourseDetailUiState())
    val uiState: StateFlow<CourseDetailUiState> = mutableUiState.asStateFlow()

    private val repository = dataSource.asUniRepository(viewModelScope)
    private val sharedData = repository.engine
    private val dataRequests = listOf(
        UniDatasets.syllabus(courseId),
        UniDatasets.StudyPlan,
        UniDatasets.Career,
        UniDatasets.Contacts,
    )
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        try {
            val detail = snapshot.courseDetailContract(courseId)
            mutableUiState.value = CourseDetailUiState(
                course = detail.course,
                professorContact = detail.professorContact,
                loadState = FeatureLoadState.Content,
            )
            snapshot.state(UniDatasets.syllabus(courseId)).error?.let { throw it }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = CourseDetailUiState(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage { getString(Res.string.msg_impossibile_caricare_il_corso) },
            )
        }
    }

    fun refresh(force: Boolean = false) {
        repository.didactics.refresh(dataRequests, force)
    }
}
