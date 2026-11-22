package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.contract.QuestionnaireForm
import com.anto426.uniapp.data.contract.toQuestionnaireForm
import com.anto426.uniapp.data.observeIn
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.feedback.runtime.warning
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.anto426.uniapp.localization.readAppString
import uniapp.composeapp.generated.resources.*

data class QuestionnaireDetailUiState(
    val title: String,
    val survey: QuestionnaireForm? = null,
    val selectedAnswers: Map<String, Set<String>> = emptyMap(),
    val freeTextAnswers: Map<String, String> = emptyMap(),
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val submitted: Boolean = false,
) {
    val answeredQuestions: Int
        get() = selectedAnswers.count { it.value.isNotEmpty() }

    val totalQuestions: Int
        get() = survey?.pages?.sumOf { it.questions.size } ?: 0

    val progress: Float
        get() = if (totalQuestions == 0) 0f else answeredQuestions.toFloat() / totalQuestions
}

class QuestionnaireDetailViewModel(
    private val courseId: String,
    private val tagList: String,
    title: String,
    private val dataSource: UniGateway,
    private val toastSink: AppToastSink = AppToastSink.None,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(QuestionnaireDetailUiState(title = title))
    val uiState: StateFlow<QuestionnaireDetailUiState> = mutableUiState.asStateFlow()

    private val repository = dataSource.asUniRepository(viewModelScope)
    private val sharedData = repository.engine
    private val dataRequests = listOf(UniDatasets.surveyPage(courseId, tagList))
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        mutableUiState.update {
            it.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
        }
        try {
            val survey = snapshot.require(UniDatasets.surveyPage(courseId, tagList)).toQuestionnaireForm()
            mutableUiState.update {
                it.copy(
                    survey = survey,
                    loadState = if (survey.pages.any { page -> page.questions.isNotEmpty() }) {
                        FeatureLoadState.Content
                    } else {
                        FeatureLoadState.Empty
                    },
                )
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.update {
                it.copy(
                    loadState = mutableUiState.value.loadState.onRefreshFailure(),
                    errorMessage = error.userMessage { readAppString(Res.string.msg_impossibile_caricare_il_questionario) },
                )
            }
        }
    }

    fun refresh() { repository.didactics.refresh(dataRequests, force = true) }

    fun selectAnswer(questionId: String, answerId: String, multipleChoice: Boolean) {
        mutableUiState.update { state ->
            val selections = state.selectedAnswers.toMutableMap()
            val current = selections[questionId].orEmpty()
            selections[questionId] =
                if (multipleChoice) {
                    current.toMutableSet().apply {
                        if (!add(answerId)) remove(answerId)
                    }
                } else {
                    setOf(answerId)
                }
            state.copy(selectedAnswers = selections)
        }
    }

    fun updateFreeText(questionId: String, value: String) {
        mutableUiState.update { state ->
            state.copy(
                freeTextAnswers = state.freeTextAnswers + (questionId to value),
            )
        }
    }

    fun submit() {
        val state = mutableUiState.value
        val survey = state.survey ?: return
        val missingRequired =
            survey.pages.flatMap { it.questions }.any { question ->
                question.required && state.selectedAnswers[question.questionId].isNullOrEmpty()
            }
        if (missingRequired) {
            toastSink.warning(Res.string.msg_survey_answer_required)
            return
        }

        viewModelScope.launch {
            mutableUiState.update { it.copy(isSubmitting = true) }
            try {
                val message = repository.didactics.submitQuestionnaire(
                    courseId = courseId,
                    form = survey,
                    selectedAnswers = state.selectedAnswers,
                    freeTextAnswers = state.freeTextAnswers,
                )
                mutableUiState.update { it.copy(isSubmitting = false, submitted = true) }
                toastSink.success(message)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                val message = error.userMessage { readAppString(Res.string.msg_impossibile_salvare_il_questionario) }
                mutableUiState.update { it.copy(isSubmitting = false) }
                toastSink.error(message)
            }
        }
    }
}
