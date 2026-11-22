package com.anto426.uniapp.session

import androidx.lifecycle.ViewModelStore
import com.anto426.securestorage.SecureStorageManager
import com.anto426.uniapp.account.model.UniAccountCredentials
import com.anto426.uniapp.account.presentation.AccountSwitcherViewModel
import com.anto426.uniapp.account.storage.MemorySecureStorageFactory
import com.anto426.uniapp.account.storage.UniAccountStore
import com.anto426.uniapp.data.UniSessionController
import com.anto426.uniapp.data.local.EncryptedUniLocalDataStore
import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.data.local.UniAppDataKeys
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.unisdk.backend.RemoteUniBackendService
import com.anto426.unisdk.backend.UniBackendService
import com.anto426.unisdk.errors.UniSdkErrorCode
import com.anto426.unisdk.errors.UniSdkException
import com.anto426.unisdk.model.LoginCareerOption
import com.anto426.unisdk.session.AuthenticationResult
import com.anto426.unisdk.session.SessionResumeResult
import com.anto426.unisdk.session.UniCredentials
import com.anto426.unisdk.session.UniSession
import com.anto426.unisdk.session.UniSessionTicket
import com.anto426.unisdk.session.UniUserProfile
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class InactiveSessionTest {
    @Test
    fun confirmedInactiveSessionIsForgottenWithoutRemovingTheAccount() = runTest {
        withFixture {
            val expiredAccountId = accountId
            backend.validate = { false }

            requestFails(UniSdkErrorCode.SESSION_INVALID)

            assertNull(store.loadSessionTicket(expiredAccountId))
            assertEquals(expiredAccountId, store.snapshot().accounts.single().accountId)
            assertEquals(
                expiredAccountId,
                assertIs<AppSessionState.ReauthenticationRequired>(sessions.state.value).account.accountId,
            )
            assertNull(notificationOwner)
            assertEquals(1, backend.closedSessions.size)

            val restarted = sessionManager()
            restarted.initialize()
            assertIs<AppSessionState.ReauthenticationRequired>(restarted.state.value)
        }
    }

    @Test
    fun serverFailureAndUnconfirmedSessionRejectionKeepTheActiveSession() = runTest {
        withFixture {
            backend.validate = { throw UniSdkException(UniSdkErrorCode.APP_BACKEND_UNAVAILABLE) }
            requestFails(UniSdkErrorCode.APP_BACKEND_UNAVAILABLE)
            assertEquals(0, backend.validationCalls)

            requestFails(UniSdkErrorCode.SESSION_INVALID)
            assertEquals(1, backend.validationCalls)

            backend.validate = { true }
            requestFails(UniSdkErrorCode.SESSION_INVALID)
            assertEquals(2, backend.validationCalls)
            assertIs<AppSessionState.Authenticated>(sessions.state.value)
            assertEquals(accountId, notificationOwner)
            assertContentEquals(byteArrayOf(1), store.loadSessionTicket(accountId)?.export())
        }
    }

    @Test
    fun validationFromAnOldRequestCannotForgetANewLogin() = runTest {
        withFixture {
            val validationStarted = CompletableDeferred<Unit>()
            val finishValidation = CompletableDeferred<Unit>()
            backend.validate = {
                validationStarted.complete(Unit)
                finishValidation.await()
                false
            }

            val request = async { requestFails(UniSdkErrorCode.SESSION_INVALID) }
            validationStarted.await()
            sessions.authenticate(credentials, preferredAccountId = accountId)
            finishValidation.complete(Unit)
            request.await()

            assertIs<AppSessionState.Authenticated>(sessions.state.value)
            assertEquals(accountId, notificationOwner)
            assertContentEquals(byteArrayOf(2), store.loadSessionTicket(accountId)?.export())
            assertEquals(1, backend.closedSessions.size)
        }
    }

    @Test
    fun cancellingProtectedAccountSwitchRestoresThePreviousAuthenticatedAccount() = runTest {
        withFixture {
            val previous = assertIs<AppSessionState.Authenticated>(sessions.state.value)
            val target = saveSecondAccount()
            EncryptedUniLocalDataStore(store).write(LocalDataScope.Account(target), UniAppDataKeys.BiometricUnlock, true)

            val pending = assertIs<AppSessionState.UnlockRequired>(sessions.activate(target))
            assertEquals(target, pending.account.accountId)
            assertEquals(previous.account.accountId, pending.fallbackAccount?.accountId)
            sessions.cancelUnlock()

            assertEquals(previous, sessions.state.value)
            assertEquals(previous.account.accountId, store.snapshot().activeAccountId)
            assertEquals(previous.account.accountId, notificationOwner)
            assertEquals("ready", sessions.callAuthenticated(
                previous.account.accountId, previous.account.activeProfileId,
            ) { _, _ -> "ready" })
        }
    }

    @Test
    fun cancelledAccountSwitchClearsTheLoadingFlagAndAllowsRetry() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModels = ViewModelStore()
        try {
            withFixture {
                val previous = assertIs<AppSessionState.Authenticated>(sessions.state.value)
                val target = saveSecondAccount()
                backend.resume = { throw CancellationException("Backend resume cancelled") }
                val switcher = AccountSwitcherViewModel(sessions)
                viewModels.put("switcher", switcher)
                advanceUntilIdle()

                suspend fun waitForSwitch() = withContext(Dispatchers.Default) {
                    withTimeout(5_000L) { switcher.uiState.first { it.activatingAccountId == null } }
                }

                switcher.selectAccount(target)
                waitForSwitch()
                assertEquals(previous, sessions.state.value)
                assertEquals(previous.account.accountId, notificationOwner)
                assertNull(switcher.uiState.value.activatingAccountId)

                backend.resume = { SessionResumeResult.ReauthenticationRequired }
                switcher.selectAccount(target)
                waitForSwitch()
                val next = assertIs<AppSessionState.ReauthenticationRequired>(sessions.state.value)
                assertEquals(target, next.account.accountId)
                assertNull(switcher.uiState.value.activatingAccountId)
            }
        } finally {
            viewModels.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun authenticatingProtectedSavedAccountStillRequiresUnlock() = runTest {
        withFixture {
            val protectedAccountId = accountId
            localDataStore.write(LocalDataScope.Account(protectedAccountId), UniAppDataKeys.BiometricUnlock, true)

            sessions.signOut()
            assertIs<AppSessionState.SignedOut>(sessions.state.value)

            sessions.authenticate(credentials)

            val unlockRequired = assertIs<AppSessionState.UnlockRequired>(sessions.state.value)
            assertEquals(protectedAccountId, unlockRequired.account.accountId)
            assertNull(notificationOwner)
        }
    }

    private suspend fun withFixture(block: suspend Fixture.() -> Unit) {
        val client = HttpClient(MockEngine { error("Unexpected HTTP request") })
        val fixture = Fixture(TestBackend(RemoteUniBackendService(client)))
        try {
            fixture.sessions.authenticate(fixture.credentials)
            fixture.block()
        } finally {
            fixture.sessions.shutdown()
            client.close()
        }
    }

    private class Fixture(val backend: TestBackend) {
        val credentials = UniAccountCredentials("student", "test-password")
        val store = UniAccountStore(SecureStorageManager(MemorySecureStorageFactory(), "session-test"))
        val localDataStore = EncryptedUniLocalDataStore(store)
        var notificationOwner: String? = null
            private set
        val sessions = sessionManager()

        val accountId: String
            get() = assertIs<AppSessionState.Authenticated>(sessions.state.value).account.accountId

        fun sessionManager() = UniSessionController(backend, store, localDataStore) { owner ->
            notificationOwner = owner
        }

        suspend fun saveSecondAccount(): String {
            val previous = accountId
            val other = store.persistAuthenticatedAccount(
                UniAccountCredentials("another-student", "other-password"),
                UniUserProfile(
                    id = "another-student-id",
                    displayName = "Second student",
                    degreeName = "Informatica",
                    matricola = "9876",
                    email = null,
                    photoUrl = null,
                    isGuest = false,
                ),
                UniSessionTicket.restore(byteArrayOf(3)),
            )
            store.setActiveAccount(previous)
            return other.accountId
        }

        suspend fun requestFails(code: UniSdkErrorCode) {
            val account = assertIs<AppSessionState.Authenticated>(sessions.state.value).account
            val error = assertFailsWith<UniSdkException> {
                sessions.callAuthenticated<Unit>(account.accountId, account.activeProfileId) { _, _ ->
                    throw UniSdkException(code)
                }
            }
            assertEquals(code, error.errorCode)
        }
    }

    private class TestBackend(delegate: UniBackendService) : UniBackendService by delegate {
        var validate: suspend () -> Boolean = { error("Unexpected session validation") }
        var resume: suspend () -> SessionResumeResult = { error("An inactive session ticket must not be restored") }
        var validationCalls = 0
            private set
        val closedSessions = mutableListOf<UniSession>()
        private var logins = 0

        override suspend fun login(
            credentials: UniCredentials,
            selectedCareer: LoginCareerOption?,
        ): AuthenticationResult.Authenticated {
            logins += 1
            // The SDK owns the opaque session constructor. Supply a local capability for this JVM test.
            val session = UniSession::class.java.getDeclaredConstructor().newInstance()
            return AuthenticationResult.Authenticated(
                session = session,
                ticket = UniSessionTicket.restore(byteArrayOf(logins.toByte())),
                profile = UniUserProfile(
                    id = "student-id",
                    displayName = "Student",
                    degreeName = "Informatica",
                    matricola = "1234",
                    email = null,
                    photoUrl = null,
                    isGuest = false,
                ),
            )
        }

        override suspend fun isSessionValid(session: UniSession, credentials: UniCredentials): Boolean {
            validationCalls += 1
            return validate()
        }

        override suspend fun resumeSession(ticket: UniSessionTicket, credentials: UniCredentials): SessionResumeResult =
            resume()

        override suspend fun closeSession(session: UniSession) {
            closedSessions += session
        }
    }
}
