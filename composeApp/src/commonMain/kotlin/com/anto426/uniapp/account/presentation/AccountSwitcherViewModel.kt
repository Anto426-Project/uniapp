package com.anto426.uniapp.account.presentation

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.session.AppSessionController
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.info
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.uniapp.security.biometric.BiometricAuthenticator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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
    private var refreshJob: Job? = null

    init {
        viewModelScope.launch {
            sessionController.avatars.images.collect { images ->
                mutableUiState.update { it.copy(profileImages = images.mapValues { (_, image) -> image.bytes }) }
            }
        }
        viewModelScope.launch {
            sessionController.accountsRevision.collect { refresh() }
        }
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
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            mutableUiState.update { it.copy(isLoading = true, errorMessage = null) }
            mutableUiState.value =
                try {
                    val snapshot = sessionController.accounts()
                    val activeId =
                        (sessionController.state.value as? AppSessionState.Authenticated)
                            ?.account
                            ?.accountId
                    sessionController.avatars.restore(snapshot)
                    mutableUiState.value.copy(
                        accounts = snapshot,
                        activeAccountId = activeId,
                        profileImages = sessionController.avatars.images.value.mapValues { (_, image) -> image.bytes },
                        isLoading = false,
                    )
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    mutableUiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: getString(Res.string.msg_impossibile_caricare_gli_account),
                    )
                }
        }
    }

    fun selectAccount(accountId: String) {
        val current = mutableUiState.value
        if (current.isRemovingAccount || current.pendingRemovalAccountId != null) return
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
                        toastSink.error(getString(Res.string.msg_impossibile_attivare_laccount))
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.update {
                    it.copy(
                        activatingAccountId = null,
                        errorMessage = error.message ?: getString(Res.string.msg_impossibile_attivare_laccount_2),
                    )
                }
                toastSink.error(error.message ?: getString(Res.string.msg_impossibile_attivare_laccount))
            }
        }
    }

    fun selectProfile(profileId: String) {
        if (mutableUiState.value.isRemovingAccount || mutableUiState.value.pendingRemovalAccountId != null) return
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
                        toastSink.error(getString(Res.string.msg_impossibile_attivare_il_profilo))
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.update {
                    it.copy(
                        activatingProfileId = null,
                        errorMessage = error.message ?: getString(Res.string.msg_impossibile_attivare_il_profilo_2),
                    )
                }
                toastSink.error(error.message ?: getString(Res.string.msg_impossibile_attivare_il_profilo))
            }
        }
    }

    fun addAccount() {
        if (mutableUiState.value.isRemovingAccount) return
        viewModelScope.launch {
            toastSink.info(getString(Res.string.msg_accedi_con_il_nuovo_account))
            sessionController.signOut()
        }
    }

    fun requestAccountRemoval(accountId: String) {
        val current = mutableUiState.value
        if (current.isRemovingAccount || current.activatingAccountId != null || current.activatingProfileId != null) return
        if (current.accounts.none { it.accountId == accountId }) return
        mutableUiState.update { it.copy(pendingRemovalAccountId = accountId) }
    }

    fun dismissAccountRemoval() {
        if (!mutableUiState.value.isRemovingAccount) {
            mutableUiState.update { it.copy(pendingRemovalAccountId = null) }
        }
    }

    fun confirmAccountRemoval(authenticator: BiometricAuthenticator) {
        val accountId = mutableUiState.value.pendingRemovalAccountId ?: return
        if (mutableUiState.value.isRemovingAccount) return
        mutableUiState.update { it.copy(isRemovingAccount = true) }
        viewModelScope.launch {
            try {
                if (sessionController.removeAccount(accountId, authenticator)) {
                    mutableUiState.update {
                        it.copy(
                            accounts = it.accounts.filterNot { account -> account.accountId == accountId },
                            profileImages = it.profileImages - accountId,
                            pendingRemovalAccountId = null,
                        )
                    }
                    toastSink.success(getString(Res.string.msg_account_rimosso_dal_dispositivo))
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                toastSink.error(getString(Res.string.msg_impossibile_completare_la_rimozione_dellaccount))
            } finally {
                mutableUiState.update { it.copy(isRemovingAccount = false) }
            }
        }
    }
}
