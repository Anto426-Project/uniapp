package com.anto426.uniapp.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.account.model.UniAccountCredentials
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.feedback.runtime.warning
import com.anto426.uniapp.session.AppSessionController
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.unisdk.backend.model.LoginCareerOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val sessionController: AppSessionController,
    private val toastSink: AppToastSink = AppToastSink.None,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(LoginUiState())
    private var reauthenticationAccountId: String? = null
    val uiState: StateFlow<LoginUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionController.state.collect(::applySessionState)
        }
    }

    fun updateUsername(value: String) {
        mutableUiState.update { it.copy(username = value) }
    }

    fun updatePassword(value: String) {
        mutableUiState.update { it.copy(password = value) }
    }

    fun updateRememberCredentials(value: Boolean) {
        mutableUiState.update { it.copy(rememberCredentials = value) }
    }

    fun submit() {
        authenticate(career = null)
    }

    fun selectCareer(career: LoginCareerOption) {
        authenticate(career)
    }

    fun cancelCareerSelection() {
        viewModelScope.launch { sessionController.cancelAuthentication() }
    }

    fun showForgotPassword() {
        mutableUiState.update { it.copy(isForgotPasswordDialogVisible = true) }
    }

    fun dismissForgotPassword() {
        mutableUiState.update { it.copy(isForgotPasswordDialogVisible = false) }
    }

    private fun authenticate(career: LoginCareerOption?) {
        val state = mutableUiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            toastSink.warning("Inserisci sia il nome utente che la password.")
            return
        }
        viewModelScope.launch {
            sessionController.authenticate(
                credentials = UniAccountCredentials(state.username, state.password),
                selectedCareer = career,
                preferredAccountId = reauthenticationAccountId,
            )
        }
    }

    private fun applySessionState(sessionState: AppSessionState) {
        mutableUiState.update { current ->
            when (sessionState) {
                AppSessionState.Initializing -> current.copy(isLoading = true)
                is AppSessionState.UnlockRequired -> current.copy(isLoading = false)
                AppSessionState.Authenticating -> current.copy(isLoading = true)
                is AppSessionState.CareerSelectionRequired ->
                    current.copy(isLoading = false, careers = sessionState.careers)

                is AppSessionState.SignedOut ->
                    current.copy(
                        isLoading = false,
                        careers = emptyList(),
                    ).also { sessionState.message?.takeIf(String::isNotBlank)?.let(toastSink::error) }

                is AppSessionState.ReauthenticationRequired ->
                    current.copy(
                        isLoading = false,
                        careers = emptyList(),
                    ).also {
                        reauthenticationAccountId = sessionState.account.accountId
                        sessionState.message?.takeIf(String::isNotBlank)?.let(toastSink::error)
                    }

                is AppSessionState.Authenticated ->
                    current.copy(
                        password = "",
                        isLoading = false,
                        careers = emptyList(),
                    ).also {
                        reauthenticationAccountId = null
                        toastSink.success("Accesso effettuato.")
                    }
            }
        }
    }
}
