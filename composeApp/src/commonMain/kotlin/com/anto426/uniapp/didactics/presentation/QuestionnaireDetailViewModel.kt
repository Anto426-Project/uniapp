package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.feedback.runtime.warning
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import com.anto426.unisdk.backend.model.SurveyAnswerBodyDto
import com.anto426.unisdk.backend.model.SurveyFirstPageData
import com.anto426.unisdk.backend.model.SurveySavePageDto
import com.anto426.unisdk.backend.model.SurveySaveRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuestionnaireDetailUiState(
    val title: String,
    val survey: SurveyFirstPageData? = null,
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
    private val dataSource: UniAppDataSource,
    private val toastSink: AppToastSink = AppToastSink.None,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(QuestionnaireDetailUiState(title = title))
    val uiState: StateFlow<QuestionnaireDetailUiState> = mutableUiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            mutableUiState.update {
                it.copy(loadState = FeatureLoadState.Loading, errorMessage = null, submitted = false)
            }
            try {
                val survey = dataSource.loadSurveyFirstPage(courseId, tagList)
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
                        loadState = FeatureLoadState.Error,
                        errorMessage = error.userMessage("Impossibile caricare il questionario."),
                    )
                }
            }
        }
    }

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
            toastSink.warning("Completa tutte le domande obbligatorie.")
            return
        }

        viewModelScope.launch {
            mutableUiState.update { it.copy(isSubmitting = true) }
            try {
                val details = dataSource.loadStudentDetails()
                val studentId =
                    listOfNotNull(details.stuId, details.matId, details.matricola)
                        .firstOrNull { it.isNotBlank() }
                        ?: error("Identificativo studente non disponibile.")
                val userCompId = survey.userCompId?.takeIf(String::isNotBlank)
                    ?: error("Identificativo compilazione non disponibile.")
                val questCompId = survey.questCompId?.takeIf(String::isNotBlank)
                    ?: error("Identificativo questionario non disponibile.")
                val request =
                    SurveySaveRequest(
                        studentId = studentId,
                        userCompId = userCompId,
                        questCompId = questCompId,
                        surveyPageDtos = survey.pages.map { page ->
                            SurveySavePageDto(
                                pageId = page.pageId,
                                ansBodyDto = page.questions.flatMap { question ->
                                    val selected = state.selectedAnswers[question.questionId].orEmpty()
                                    selected.map { answerId ->
                                        val isFreeText = question.answers.any { it.answerId == answerId && it.freeText }
                                        SurveyAnswerBodyDto(
                                            questionId = question.questionId,
                                            answerId = answerId,
                                            answerBody = if (isFreeText) {
                                                state.freeTextAnswers[question.questionId].orEmpty().trim()
                                            } else {
                                                ""
                                            },
                                        )
                                    }
                                },
                            )
                        },
                    )
                val message = dataSource.saveSurvey(courseId, request)
                mutableUiState.update { it.copy(isSubmitting = false, submitted = true) }
                toastSink.success(message)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                val message = error.userMessage("Impossibile salvare il questionario.")
                mutableUiState.update { it.copy(isSubmitting = false) }
                toastSink.error(message)
            }
        }
    }
}
