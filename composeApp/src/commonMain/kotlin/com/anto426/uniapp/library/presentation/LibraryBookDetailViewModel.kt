package com.anto426.uniapp.library.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.library.model.LibraryBookCopyAction
import com.anto426.uniapp.library.model.LibraryBookDetail
import com.anto426.uniapp.library.model.LibraryBookLoanDraft
import com.anto426.uniapp.library.model.LibraryLoanSubmissionResult
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.anto426.uniapp.localization.readAppString
import uniapp.composeapp.generated.resources.*

data class LibraryBookDetailUiState(
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val book: LibraryBookDetail? = null,
    val errorMessage: String? = null,
    val preparingActionId: String? = null,
    val pendingDraft: LibraryBookLoanDraft? = null,
    val isSubmitting: Boolean = false,
    val submissionResult: LibraryLoanSubmissionResult? = null,
)

class LibraryBookDetailViewModel(
    private val bookId: String,
    private val dataSource: UniGateway,
    private val toastSink: AppToastSink = AppToastSink.None,
) : ViewModel() {
    private val repository = dataSource.asUniRepository(viewModelScope)
    private val mutableUiState = MutableStateFlow(LibraryBookDetailUiState())
    val uiState: StateFlow<LibraryBookDetailUiState> = mutableUiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        mutableUiState.value = mutableUiState.value.copy(
            loadState = FeatureLoadState.Loading,
            errorMessage = null,
        )
        viewModelScope.launch {
            try {
                val detail = repository.library.loadBook(bookId)
                mutableUiState.value = LibraryBookDetailUiState(
                    loadState = FeatureLoadState.Content,
                    book = detail,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = LibraryBookDetailUiState(
                    loadState = FeatureLoadState.Error,
                    errorMessage = error.userMessage { readAppString(Res.string.msg_impossibile_caricare_il_libro) },
                )
            }
        }
    }

    fun prepareRequest(action: LibraryBookCopyAction) {
        if (mutableUiState.value.preparingActionId != null || mutableUiState.value.isSubmitting) return
        mutableUiState.value = mutableUiState.value.copy(preparingActionId = action.actionId)
        viewModelScope.launch {
            try {
                val draft = repository.library.prepareBookRequest(bookId, action.actionId)
                mutableUiState.value = mutableUiState.value.copy(
                    preparingActionId = null,
                    pendingDraft = draft,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(preparingActionId = null)
                toastSink.error(error.userMessage { readAppString(Res.string.msg_impossibile_preparare_richiesta_prestito) })
            }
        }
    }

    fun dismissDraft() {
        if (mutableUiState.value.isSubmitting) return
        mutableUiState.value = mutableUiState.value.copy(pendingDraft = null)
    }

    fun confirmDraft() {
        val draft = mutableUiState.value.pendingDraft ?: return
        if (mutableUiState.value.isSubmitting) return
        mutableUiState.value = mutableUiState.value.copy(isSubmitting = true)
        viewModelScope.launch {
            try {
                val result = repository.library.confirmBookRequest(draft.draftId)
                mutableUiState.value = mutableUiState.value.copy(
                    pendingDraft = null,
                    isSubmitting = false,
                    submissionResult = result,
                )
                toastSink.success(readAppString(Res.string.ui_library_request_submitted))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(
                    pendingDraft = null,
                    isSubmitting = false,
                )
                toastSink.error(error.userMessage { readAppString(Res.string.msg_impossibile_inviare_richiesta_prestito) })
            }
        }
    }
}
