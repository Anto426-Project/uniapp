package com.anto426.uniapp.account.session

import com.anto426.unisdk.backend.UniBackendService
import com.anto426.unisdk.backend.model.LoginCareerOption
import com.anto426.uniapp.account.model.UniAccountCredentials
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.account.storage.UniAccountStore
import com.anto426.unisdk.session.AuthenticationResult
import com.anto426.unisdk.session.SessionResumeResult
import com.anto426.unisdk.session.UniCredentials
import com.anto426.unisdk.session.UniSession
import com.anto426.unisdk.session.UniUserProfile
import com.anto426.unisdk.session.UniCareerProfile
import com.anto426.unisdk.transport.TransportService
import com.anto426.unisdk.transport.TransportSession
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ActiveUniSession internal constructor(
    val account: UniAccountSummary,
    internal val session: UniSession,
    val profile: UniUserProfile,
)

sealed interface ManagedAuthenticationResult {
    data class Authenticated(val active: ActiveUniSession) : ManagedAuthenticationResult

    data class CareerSelectionRequired(val careers: List<LoginCareerOption>) :
        ManagedAuthenticationResult
}

sealed interface ManagedSessionResult {
    data class Active(val value: ActiveUniSession) : ManagedSessionResult

    data object NoActiveAccount : ManagedSessionResult

    data class ReauthenticationRequired(val account: UniAccountSummary) : ManagedSessionResult
}

/**
 * UniApp's single entry point for persistent accounts and RAM-only SDK sessions.
 *
 * Network authentication and resume operations are single-flight. The SDK receives credentials
 * only inside the individual call that requires them; its runtime vault retains token state only.
 */
class UniSessionCoordinator(
    private val backend: UniBackendService,
    private val accounts: UniAccountStore,
) {
    private val lock = Mutex()
    private val runtimeSessions = mutableMapOf<String, ActiveUniSession>()
    private var pendingProfileCatalog: List<LoginCareerOption> = emptyList()

    suspend fun authenticate(
        credentials: UniAccountCredentials,
        selectedCareer: LoginCareerOption? = null,
        preferredAccountId: String? = null,
    ): ManagedAuthenticationResult =
        lock.withLock {
            when (
                val result =
                    backend.login(
                        credentials = UniCredentials(credentials.username, credentials.password),
                        selectedCareer = selectedCareer,
                    )
            ) {
                is AuthenticationResult.CareerSelectionRequired ->
                    ManagedAuthenticationResult.CareerSelectionRequired(result.careers).also {
                        pendingProfileCatalog = result.careers
                    }

                is AuthenticationResult.Authenticated -> {
                    val resolvedProfile =
                        result.profile.withProfileCatalog(
                            catalog = pendingProfileCatalog,
                            selected = selectedCareer,
                        )
                    pendingProfileCatalog = emptyList()
                    val account =
                        try {
                            accounts.persistAuthenticatedAccount(
                                credentials = credentials,
                                profile = resolvedProfile,
                                ticket = result.ticket,
                                preferredAccountId = preferredAccountId,
                            )
                        } catch (error: Throwable) {
                            runCatching { backend.closeSession(result.session) }
                            throw error
                        }
                    runtimeSessions.remove(account.accountId)?.let { previous ->
                        runCatching { backend.closeSession(previous.session) }
                    }
                    val active = ActiveUniSession(account, result.session, resolvedProfile)
                    runtimeSessions[account.accountId] = active
                    ManagedAuthenticationResult.Authenticated(active)
                }
            }
        }

    suspend fun resumeActiveAccount(): ManagedSessionResult =
        lock.withLock {
            val snapshot = accounts.snapshot()
            val accountId = snapshot.activeAccountId ?: return@withLock ManagedSessionResult.NoActiveAccount
            resumeLocked(snapshot.accounts.first { it.accountId == accountId })
        }

    suspend fun activate(accountId: String): ManagedSessionResult =
        lock.withLock {
            val account =
                accounts.snapshot().accounts.firstOrNull { it.accountId == accountId }
                    ?: throw IllegalArgumentException("Unknown account")
            when (val result = resumeLocked(account)) {
                is ManagedSessionResult.Active -> {
                    accounts.setActiveAccount(accountId)
                    result
                }

                else -> result
            }
        }

    /**
     * Re-authenticates the same university identity into one of its student careers or professor
     * profiles. The persistent account remains the same; only its active profile and RAM session
     * change.
     */
    suspend fun activateProfile(
        accountId: String,
        profileId: String,
    ): ManagedSessionResult =
        lock.withLock {
            val account =
                accounts.snapshot().accounts.firstOrNull { it.accountId == accountId }
                    ?: throw IllegalArgumentException("Unknown account")
            val selected =
                account.profiles.firstOrNull { it.profileId == profileId }
                    ?.toLoginCareerOption()
                    ?: throw IllegalArgumentException("Unknown account profile")

            accounts.withCredentials(accountId) { credentials ->
                val result = backend.login(credentials, selected)
                val authenticated =
                    result as? AuthenticationResult.Authenticated
                        ?: error("Il portale non ha attivato il profilo selezionato.")
                val catalog = account.profiles.map { it.toLoginCareerOption() }
                val resolvedProfile =
                    authenticated.profile.withProfileCatalog(
                        catalog = catalog,
                        selected = selected,
                    )
                val updatedAccount =
                    try {
                        accounts.updateSession(
                            accountId = accountId,
                            profile = resolvedProfile,
                            ticket = authenticated.ticket,
                        )
                    } catch (error: Throwable) {
                        runCatching { backend.closeSession(authenticated.session) }
                        throw error
                    }
                runtimeSessions.remove(accountId)?.let { previous ->
                    runCatching { backend.closeSession(previous.session) }
                }
                accounts.setActiveAccount(accountId)
                val active = ActiveUniSession(updatedAccount, authenticated.session, resolvedProfile)
                runtimeSessions[accountId] = active
                ManagedSessionResult.Active(active)
            }
        }

    suspend fun currentRuntimeSession(accountId: String): ActiveUniSession? =
        lock.withLock { runtimeSessions[accountId] }

    fun accountClient(accountId: String): UniAccountClient = UniAccountClient(accountId, this)

    internal suspend fun <T> callAuthenticated(
        accountId: String,
        block: suspend UniBackendService.(UniSession, UniCredentials) -> T,
    ): T {
        val primarySession =
            lock.withLock {
                runtimeSessions[accountId]?.session
                    ?: throw InactiveAccountSessionException(accountId)
            }
        return accounts.withCredentials(accountId) { credentials ->
            backend.block(primarySession, credentials)
        }
    }

    /**
     * Opens one short-lived bus portal cycle. Its cookies/PHPSESSID remain inside Uni SDK RAM and
     * the session is closed in [finally], after any number of operations performed by [block].
     */
    suspend fun <T> withTransportSession(
        accountId: String,
        block: suspend TransportService.(TransportSession) -> T,
    ): T {
        val primarySession =
            lock.withLock {
                runtimeSessions[accountId]?.session
                    ?: throw InactiveAccountSessionException(accountId)
            }
        return accounts.withCredentials(accountId) { credentials ->
            val transportSession = backend.openTransportSession(primarySession, credentials)
            try {
                backend.block(transportSession)
            } finally {
                backend.closeTransportSession(transportSession)
            }
        }
    }

    suspend fun closeRuntimeSession(accountId: String) {
        lock.withLock {
            runtimeSessions.remove(accountId)?.let { active -> backend.closeSession(active.session) }
        }
    }

    suspend fun forgetAccount(accountId: String) {
        lock.withLock {
            runtimeSessions.remove(accountId)?.let { active ->
                runCatching { backend.closeSession(active.session) }
            }
            accounts.forgetAccount(accountId)
        }
    }

    suspend fun shutdown() {
        lock.withLock {
            pendingProfileCatalog = emptyList()
            val sessions = runtimeSessions.values.toList()
            runtimeSessions.clear()
            sessions.forEach { active -> runCatching { backend.closeSession(active.session) } }
        }
    }

    private suspend fun resumeLocked(account: UniAccountSummary): ManagedSessionResult {
        runtimeSessions[account.accountId]?.let { active ->
            return ManagedSessionResult.Active(active)
        }
        val ticket = accounts.loadSessionTicket(account.accountId)
            ?: return ManagedSessionResult.ReauthenticationRequired(account)
        return accounts.withCredentials(account.accountId) { credentials ->
            when (val result = backend.resumeSession(ticket, credentials)) {
                SessionResumeResult.ReauthenticationRequired -> {
                    accounts.clearSessionTicket(account.accountId)
                    ManagedSessionResult.ReauthenticationRequired(account)
                }

                is SessionResumeResult.Resumed -> {
                    val updatedAccount =
                        try {
                            accounts.updateSession(
                                accountId = account.accountId,
                                profile = result.profile,
                                ticket = result.ticket,
                            )
                        } catch (error: Throwable) {
                            runCatching { backend.closeSession(result.session) }
                            throw error
                        }
                    val active = ActiveUniSession(updatedAccount, result.session, result.profile)
                    runtimeSessions[account.accountId] = active
                    ManagedSessionResult.Active(active)
                }
            }
        }
    }
}

private fun UniUserProfile.withProfileCatalog(
    catalog: List<LoginCareerOption>,
    selected: LoginCareerOption?,
): UniUserProfile {
    if (catalog.isEmpty()) return this
    val mapped =
        catalog.map { option ->
            UniCareerProfile(
                profileId = option.profileId,
                displayName = option.displayName,
                degreeName = option.degreeName,
                matricola = option.matricola,
                matId = option.matId,
                stuId = option.stuId,
                anaId = option.anaId,
                cdsId = option.cdsId,
                dipId = option.dipId,
                departmentName = option.departmentName,
                teacherId = option.teacherId,
                type = option.type,
            )
        }
    val active = selected?.profileId ?: activeProfileId
    val activeType = mapped.firstOrNull { it.profileId == active }?.type ?: activeProfileType
    return copy(
        activeProfileId = active,
        profiles = mapped,
        activeProfileType = activeType,
    )
}

private fun com.anto426.uniapp.account.model.UniAccountProfileSummary.toLoginCareerOption(): LoginCareerOption =
    LoginCareerOption(
        displayName = displayName,
        degreeName = degreeName,
        matricola = matricola,
        matId = matId,
        stuId = stuId,
        anaId = anaId,
        cdsId = cdsId,
        dipId = dipId,
        departmentName = departmentName,
        teacherId = teacherId,
        type = type,
    )

class InactiveAccountSessionException(accountId: String) :
    IllegalStateException("Account '$accountId' does not have an active RAM session")
