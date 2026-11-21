package com.anto426.uniapp.session

import com.anto426.securestorage.SecureStorageManager
import com.anto426.uniapp.account.model.UniAccountCredentials
import com.anto426.uniapp.account.storage.MemorySecureStorageFactory
import com.anto426.uniapp.account.storage.UniAccountStore
import com.anto426.uniapp.data.UniSessionController
import com.anto426.uniapp.data.local.EncryptedUniLocalDataStore
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

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
        var notificationOwner: String? = null
            private set
        val sessions = sessionManager()

        val accountId: String
            get() = assertIs<AppSessionState.Authenticated>(sessions.state.value).account.accountId

        fun sessionManager() = UniSessionController(backend, store, EncryptedUniLocalDataStore(store)) { owner ->
            notificationOwner = owner
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
            error("An inactive session ticket must not be restored")

        override suspend fun closeSession(session: UniSession) {
            closedSessions += session
        }
    }
}
