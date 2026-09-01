package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.toStudyCourse
import com.anto426.uniapp.model.didactics.StudyCourse
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CourseDetailUiState(
    val course: StudyCourse? = null,
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
)

class CourseDetailViewModel(
    private val courseId: String,
    private val dataSource: UniAppDataSource,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(CourseDetailUiState())
    val uiState: StateFlow<CourseDetailUiState> = mutableUiState.asStateFlow()

    init { refresh() }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            try {
                val course = dataSource.loadCourseSyllabus(courseId, force).toStudyCourse()
                mutableUiState.value = CourseDetailUiState(course, FeatureLoadState.Content)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = CourseDetailUiState(
                    loadState = FeatureLoadState.Error,
                    errorMessage = error.userMessage("Impossibile caricare il corso."),
                )
            }
        }
    }
}
