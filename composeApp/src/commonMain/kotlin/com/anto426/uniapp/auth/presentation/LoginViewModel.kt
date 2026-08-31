package com.anto426.uniapp.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.account.model.UniAccountCredentials
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
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionController.state.collect(::applySessionState)
        }
    }

    fun updateUsername(value: String) {
        mutableUiState.update { it.copy(username = value, errorMessage = null) }
    }

    fun updatePassword(value: String) {
        mutableUiState.update { it.copy(password = value, errorMessage = null) }
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
            mutableUiState.update {
                it.copy(errorMessage = "Inserisci sia il nome utente che la password.")
            }
            return
        }
        viewModelScope.launch {
            sessionController.authenticate(
                credentials = UniAccountCredentials(state.username, state.password),
                selectedCareer = career,
            )
        }
    }

    private fun applySessionState(sessionState: AppSessionState) {
        mutableUiState.update { current ->
            when (sessionState) {
                AppSessionState.Initializing -> current.copy(isLoading = true)
                AppSessionState.Authenticating -> current.copy(isLoading = true, errorMessage = null)
                is AppSessionState.CareerSelectionRequired ->
                    current.copy(isLoading = false, careers = sessionState.careers)

                is AppSessionState.SignedOut ->
                    current.copy(
                        isLoading = false,
                        careers = emptyList(),
                        errorMessage = sessionState.message,
                    )

                is AppSessionState.ReauthenticationRequired ->
                    current.copy(
                        isLoading = false,
                        careers = emptyList(),
                        errorMessage = sessionState.message,
                    )

                is AppSessionState.Authenticated ->
                    current.copy(
                        password = "",
                        isLoading = false,
                        careers = emptyList(),
                        errorMessage = null,
                    )
            }
        }
    }
}
