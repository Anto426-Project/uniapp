package com.anto426.uniapp.account.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.session.AppSessionController
import com.anto426.uniapp.session.model.AppSessionState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountSwitcherViewModel(
    private val sessionController: AppSessionController,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(AccountSwitcherUiState())
    val uiState: StateFlow<AccountSwitcherUiState> = mutableUiState.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            sessionController.state.collect { state ->
                val activeId = (state as? AppSessionState.Authenticated)?.account?.accountId
                mutableUiState.update { it.copy(activeAccountId = activeId) }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoading = true, errorMessage = null) }
            mutableUiState.value =
                try {
                    val snapshot = sessionController.accounts()
                    val activeId =
                        (sessionController.state.value as? AppSessionState.Authenticated)
                            ?.account
                            ?.accountId
                    AccountSwitcherUiState(
                        accounts = snapshot,
                        activeAccountId = activeId,
                        isLoading = false,
                    )
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    mutableUiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Impossibile caricare gli account",
                    )
                }
        }
    }

    fun selectAccount(accountId: String) {
        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoading = true, errorMessage = null) }
            sessionController.activate(accountId)
            refresh()
        }
    }

    fun addAccount() {
        viewModelScope.launch { sessionController.signOut() }
    }
}
