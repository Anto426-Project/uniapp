package com.anto426.uniapp.account.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.session.AppSessionController
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.info
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.session.model.AppSessionState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountSwitcherViewModel(
    private val sessionController: AppSessionController,
    private val toastSink: AppToastSink = AppToastSink.None,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(AccountSwitcherUiState())
    val uiState: StateFlow<AccountSwitcherUiState> = mutableUiState.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            sessionController.state.collect { state ->
                val activeAccount = (state as? AppSessionState.Authenticated)?.account
                mutableUiState.update { current ->
                    current.copy(
                        accounts =
                            activeAccount?.let { updated ->
                                current.accounts.map { account ->
                                    if (account.accountId == updated.accountId) updated else account
                                }
                            } ?: current.accounts,
                        activeAccountId = activeAccount?.accountId,
                    )
                }
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
                    val profileImages =
                        snapshot.mapNotNull { account ->
                            sessionController.cachedProfileImage(account)?.let { account.accountId to it }
                        }.toMap()
                    AccountSwitcherUiState(
                        accounts = snapshot,
                        activeAccountId = activeId,
                        profileImages = profileImages,
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
        val current = mutableUiState.value
        if (accountId == current.activeAccountId || current.activatingAccountId != null) return

        viewModelScope.launch {
            mutableUiState.update {
                it.copy(
                    activatingAccountId = accountId,
                    errorMessage = null,
                )
            }
            try {
                when (sessionController.activate(accountId)) {
                    is AppSessionState.Authenticated -> {
                        mutableUiState.update {
                            it.copy(
                                activeAccountId = accountId,
                                activatingAccountId = null,
                            )
                        }
                        toastSink.success("Account attivato.")
                        refresh()
                    }

                    is AppSessionState.ReauthenticationRequired -> {
                        mutableUiState.update { it.copy(activatingAccountId = null) }
                        // The session state routes to Login. LoginViewModel retains the selected
                        // account id so the refreshed credentials update that account, not a copy.
                    }

                    is AppSessionState.UnlockRequired -> {
                        mutableUiState.update { it.copy(activatingAccountId = null) }
                    }

                    else -> {
                        mutableUiState.update { it.copy(activatingAccountId = null) }
                        toastSink.error("Impossibile attivare l’account.")
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.update {
                    it.copy(
                        activatingAccountId = null,
                        errorMessage = error.message ?: "Impossibile attivare l’account",
                    )
                }
                toastSink.error(error.message ?: "Impossibile attivare l’account.")
            }
        }
    }

    fun selectProfile(profileId: String) {
        val currentAccount =
            mutableUiState.value.accounts.firstOrNull {
                it.accountId == mutableUiState.value.activeAccountId
            } ?: return
        if (
            profileId == currentAccount.activeProfileId ||
            mutableUiState.value.activatingAccountId != null ||
            mutableUiState.value.activatingProfileId != null
        ) return

        viewModelScope.launch {
            mutableUiState.update { it.copy(activatingProfileId = profileId, errorMessage = null) }
            try {
                when (val state = sessionController.activateProfile(profileId)) {
                    is AppSessionState.Authenticated -> {
                        mutableUiState.update {
                            it.copy(
                                accounts = it.accounts.map { account ->
                                    if (account.accountId == state.account.accountId) state.account else account
                                },
                                activatingProfileId = null,
                            )
                        }
                        toastSink.success("Profilo universitario attivato.")
                    }

                    else -> {
                        mutableUiState.update { it.copy(activatingProfileId = null) }
                        toastSink.error("Impossibile attivare il profilo.")
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.update {
                    it.copy(
                        activatingProfileId = null,
                        errorMessage = error.message ?: "Impossibile attivare il profilo",
                    )
                }
                toastSink.error(error.message ?: "Impossibile attivare il profilo.")
            }
        }
    }

    fun addAccount() {
        toastSink.info("Accedi con il nuovo account.")
        viewModelScope.launch { sessionController.signOut() }
    }
}
