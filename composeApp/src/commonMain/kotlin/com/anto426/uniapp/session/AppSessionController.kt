package com.anto426.uniapp.session

import com.anto426.uniapp.account.model.UniAccountCredentials
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.account.session.ManagedAuthenticationResult
import com.anto426.uniapp.account.session.ManagedSessionResult
import com.anto426.uniapp.account.session.UniAccountClient
import com.anto426.uniapp.account.session.UniSessionCoordinator
import com.anto426.uniapp.account.storage.UniAccountStore
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.unisdk.backend.model.LoginCareerOption
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AppSessionController internal constructor(
    private val coordinator: UniSessionCoordinator,
    private val accountStore: UniAccountStore,
) {
    private val lock = Mutex()
    private val mutableState = MutableStateFlow<AppSessionState>(AppSessionState.Initializing)

    val state: StateFlow<AppSessionState> = mutableState.asStateFlow()

    suspend fun initialize() {
        lock.withLock {
            if (mutableState.value !is AppSessionState.Initializing) return
            mutableState.value = coordinator.resumeActiveAccount().toAppState()
        }
    }

    suspend fun authenticate(
        credentials: UniAccountCredentials,
        selectedCareer: LoginCareerOption? = null,
        preferredAccountId: String? = null,
    ) {
        lock.withLock {
            mutableState.value = AppSessionState.Authenticating
            mutableState.value =
                try {
                    when (
                        val result =
                            coordinator.authenticate(
                                credentials = credentials,
                                selectedCareer = selectedCareer,
                                preferredAccountId = preferredAccountId,
                            )
                    ) {
                        is ManagedAuthenticationResult.Authenticated ->
                            AppSessionState.Authenticated(result.active.account)

                        is ManagedAuthenticationResult.CareerSelectionRequired ->
                            AppSessionState.CareerSelectionRequired(result.careers)
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    AppSessionState.SignedOut(error.message ?: "Accesso non riuscito")
                }
        }
    }

    suspend fun cancelAuthentication() {
        lock.withLock {
            if (mutableState.value is AppSessionState.CareerSelectionRequired) {
                mutableState.value = AppSessionState.SignedOut()
            }
        }
    }

    suspend fun activate(accountId: String) {
        lock.withLock {
            val previous = mutableState.value
            mutableState.value =
                try {
                    coordinator.activate(accountId).toAppState()
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    previous
                }
        }
    }

    suspend fun signOut() {
        lock.withLock {
            (mutableState.value as? AppSessionState.Authenticated)?.account?.accountId?.let { accountId ->
                coordinator.closeRuntimeSession(accountId)
            }
            accountStore.setActiveAccount(null)
            mutableState.value = AppSessionState.SignedOut()
        }
    }

    fun currentAccountClient(): UniAccountClient? =
        (mutableState.value as? AppSessionState.Authenticated)
            ?.account
            ?.accountId
            ?.let(coordinator::accountClient)

    suspend fun accounts(): List<UniAccountSummary> = accountStore.snapshot().accounts

    private fun ManagedSessionResult.toAppState(): AppSessionState =
        when (this) {
            is ManagedSessionResult.Active -> AppSessionState.Authenticated(value.account)
            ManagedSessionResult.NoActiveAccount -> AppSessionState.SignedOut()
            is ManagedSessionResult.ReauthenticationRequired ->
                AppSessionState.ReauthenticationRequired(account)
        }
}
