package com.anto426.uniapp.account.storage

import com.anto426.securestorage.SecureStorageManager
import com.anto426.securestorage.getObject
import com.anto426.securestorage.getString
import com.anto426.securestorage.putObject
import com.anto426.securestorage.putString
import com.anto426.uniapp.account.model.MissingAccountCredentialsException
import com.anto426.uniapp.account.model.UniAccountCredentials
import com.anto426.uniapp.account.model.UniAccountRegistrySnapshot
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.account.model.UniAccountProfileSummary
import com.anto426.unisdk.backend.model.BackendCareerType
import com.anto426.uniapp.account.platform.generateAccountStorageIdentifier
import com.anto426.unisdk.session.UniCredentials
import com.anto426.unisdk.session.UniSessionTicket
import com.anto426.unisdk.session.UniUserProfile
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

/**
 * UniApp-owned encrypted account registry.
 *
 * The registry contains only account metadata. Credentials and the opaque primary-session ticket
 * live in the account's independently erasable encrypted vault.
 */
class UniAccountStore(
    private val storageManager: SecureStorageManager,
    private val generateIdentifier: () -> String = ::generateAccountStorageIdentifier,
) {
    private val lock = Mutex()
    private var cachedRegistry: StoredAccountRegistry? = null

    suspend fun snapshot(): UniAccountRegistrySnapshot =
        lock.withLock { loadRegistry().toSnapshot() }

    internal suspend fun persistAuthenticatedAccount(
        credentials: UniAccountCredentials,
        profile: UniUserProfile,
        ticket: UniSessionTicket,
        preferredAccountId: String? = null,
    ): UniAccountSummary =
        lock.withLock {
            val registry = loadRegistry()
            val identityAccounts = registry.accounts.filter { it.serverUserId == profile.id }
            val existing =
                if (preferredAccountId != null) {
                    registry.accounts.firstOrNull { it.accountId == preferredAccountId }
                        ?: throw IllegalArgumentException("Unknown preferred account")
                } else {
                    identityAccounts.firstOrNull { it.accountId == registry.activeAccountId }
                        ?: identityAccounts.firstOrNull()
                }
            val accountId = existing?.accountId ?: newUniqueAccountId(registry)
            val account = profile.toStoredAccount(accountId, identityAccounts)
            val accountStorage = storageManager.vault(accountId)
            val exportedTicket = ticket.export()

            try {
                accountStorage.putString(USERNAME_KEY, credentials.username)
                accountStorage.putString(PASSWORD_KEY, credentials.password)
                accountStorage.putBytes(SESSION_TICKET_KEY, exportedTicket)
                persistRegistry(
                    registry.copy(
                        activeAccountId = accountId,
                        accounts =
                            registry.accounts
                                .filterNot { it.serverUserId == profile.id }
                                .upsert(account),
                    ),
                )
                identityAccounts
                    .asSequence()
                    .map(StoredAccount::accountId)
                    .filterNot { it == accountId }
                    .forEach { duplicateId -> runCatching { storageManager.destroyVault(duplicateId) } }
            } finally {
                exportedTicket.fill(0)
            }

            account.toSummary()
        }

    internal suspend fun updateSession(
        accountId: String,
        profile: UniUserProfile,
        ticket: UniSessionTicket,
    ): UniAccountSummary =
        lock.withLock {
            val registry = loadRegistry()
            require(registry.accounts.any { it.accountId == accountId }) { "Unknown account" }
            val updated = profile.toStoredAccount(accountId, listOf(registry.accounts.first { it.accountId == accountId }))
            val exportedTicket = ticket.export()
            try {
                storageManager.vault(accountId).putBytes(SESSION_TICKET_KEY, exportedTicket)
                persistRegistry(
                    registry.copy(accounts = registry.accounts.upsert(updated)),
                )
            } finally {
                exportedTicket.fill(0)
            }
            updated.toSummary()
        }

    internal suspend fun loadSessionTicket(accountId: String): UniSessionTicket? =
        lock.withLock {
            requireKnownAccount(loadRegistry(), accountId)
            val bytes = storageManager.vault(accountId).getBytes(SESSION_TICKET_KEY) ?: return@withLock null
            try {
                UniSessionTicket.restore(bytes)
            } finally {
                bytes.fill(0)
            }
        }

    internal suspend fun clearSessionTicket(accountId: String) {
        lock.withLock {
            requireKnownAccount(loadRegistry(), accountId)
            storageManager.vault(accountId).remove(SESSION_TICKET_KEY)
        }
    }

    internal suspend fun readCachedData(
        accountId: String,
        key: String,
    ): ByteArray? =
        lock.withLock {
            requireKnownAccount(loadRegistry(), accountId)
            storageManager.vault(accountId).getBytes(cacheKey(key))
        }

    internal suspend fun writeCachedData(
        accountId: String,
        key: String,
        value: ByteArray,
    ) {
        lock.withLock {
            requireKnownAccount(loadRegistry(), accountId)
            storageManager.vault(accountId).putBytes(cacheKey(key), value)
        }
    }

    internal suspend fun removeCachedData(
        accountId: String,
        key: String,
    ) {
        lock.withLock {
            requireKnownAccount(loadRegistry(), accountId)
            storageManager.vault(accountId).remove(cacheKey(key))
        }
    }

    internal suspend fun readPreference(accountId: String, key: String): String? {
        val bytes = readCachedData(accountId, preferenceCacheKey(key)) ?: return null
        return try {
            bytes.decodeToString(throwOnInvalidSequence = true)
        } finally {
            bytes.fill(0)
        }
    }

    internal suspend fun writePreference(accountId: String, key: String, value: String) {
        val bytes = value.encodeToByteArray()
        try {
            writeCachedData(accountId, preferenceCacheKey(key), bytes)
        } finally {
            bytes.fill(0)
        }
    }

    internal suspend fun readProfileImage(
        accountId: String,
        expectedSource: String,
    ): CachedProfileImage? =
        lock.withLock {
            requireKnownAccount(loadRegistry(), accountId)
            val vault = storageManager.vault(accountId)
            val storedSource = vault.getString(PROFILE_IMAGE_SOURCE_KEY) ?: return@withLock null
            if (storedSource != expectedSource) return@withLock null
            val savedAt = vault.getString(PROFILE_IMAGE_SAVED_AT_KEY)?.toLongOrNull() ?: return@withLock null
            val bytes = vault.getBytes(PROFILE_IMAGE_BYTES_KEY) ?: return@withLock null
            CachedProfileImage(savedAtMillis = savedAt, bytes = bytes)
        }

    internal suspend fun writeProfileImage(
        accountId: String,
        source: String,
        savedAtMillis: Long,
        bytes: ByteArray,
    ) {
        lock.withLock {
            requireKnownAccount(loadRegistry(), accountId)
            val vault = storageManager.vault(accountId)
            vault.putBytes(PROFILE_IMAGE_BYTES_KEY, bytes)
            vault.putString(PROFILE_IMAGE_SOURCE_KEY, source)
            vault.putString(PROFILE_IMAGE_SAVED_AT_KEY, savedAtMillis.toString())
        }
    }

    internal suspend fun <T> withCredentials(
        accountId: String,
        block: suspend (UniCredentials) -> T,
    ): T {
        val credentials =
            lock.withLock {
                requireKnownAccount(loadRegistry(), accountId)
                val accountStorage = storageManager.vault(accountId)
                val username = accountStorage.getString(USERNAME_KEY)
                    ?: throw MissingAccountCredentialsException(accountId)
                val password = accountStorage.getString(PASSWORD_KEY)
                    ?: throw MissingAccountCredentialsException(accountId)
                UniCredentials(username, password)
            }
        return block(credentials)
    }

    suspend fun setActiveAccount(accountId: String?) {
        lock.withLock {
            val registry = loadRegistry()
            if (accountId != null) requireKnownAccount(registry, accountId)
            persistRegistry(registry.copy(activeAccountId = accountId))
        }
    }

    suspend fun forgetAccount(accountId: String) {
        lock.withLock {
            val registry = loadRegistry()
            requireKnownAccount(registry, accountId)

            // Destroy the per-account key before removing the registry reference. A failed registry
            // update can leave a visible signed-out account, never recoverable secret material.
            storageManager.destroyVault(accountId)
            persistRegistry(
                registry.copy(
                    activeAccountId = registry.activeAccountId.takeUnless { it == accountId },
                    accounts = registry.accounts.filterNot { it.accountId == accountId },
                ),
            )
        }
    }

    suspend fun destroyAll() {
        lock.withLock {
            val registry = loadRegistry()
            storageManager.destroyAll(registry.accounts.map(StoredAccount::accountId))
            cachedRegistry = null
        }
    }

    private suspend fun loadRegistry(): StoredAccountRegistry {
        cachedRegistry?.let { return it }
        val storage = storageManager.registry()
        val existing = storage.getObject<StoredAccountRegistry>(REGISTRY_KEY)
        if (existing != null) {
            check(existing.schemaVersion == REGISTRY_SCHEMA_VERSION) {
                "Unsupported account registry version ${existing.schemaVersion}"
            }
            return existing.normalized().also { cachedRegistry = it }
        }

        val created =
            StoredAccountRegistry(
                schemaVersion = REGISTRY_SCHEMA_VERSION,
                installationId = newIdentifier("installation"),
        )
        storage.putObject(REGISTRY_KEY, created)
        cachedRegistry = created
        return created
    }

    private suspend fun persistRegistry(registry: StoredAccountRegistry) {
        val normalized = registry.normalized()
        storageManager.registry().putObject(REGISTRY_KEY, normalized)
        cachedRegistry = normalized
    }

    private fun newIdentifier(label: String): String {
        val id = generateIdentifier().trim()
        require(id.isNotEmpty()) { "$label identifier cannot be blank" }
        require(id.length <= 160) { "$label identifier is too long" }
        require(id.all { it.isLetterOrDigit() || it == '.' || it == '_' || it == '-' }) {
            "$label identifier contains unsupported characters"
        }
        return id
    }

    private fun newUniqueAccountId(registry: StoredAccountRegistry): String {
        repeat(MAX_IDENTIFIER_ATTEMPTS) {
            val candidate = newIdentifier("account")
            if (registry.accounts.none { it.accountId == candidate }) return candidate
        }
        error("Unable to generate a unique account identifier")
    }

    private fun requireKnownAccount(registry: StoredAccountRegistry, accountId: String) {
        require(registry.accounts.any { it.accountId == accountId }) { "Unknown account" }
    }

    private fun cacheKey(key: String): String {
        val normalized = key.trim()
        require(normalized.isNotEmpty()) { "Cache key cannot be blank" }
        require(normalized.length <= 180) { "Cache key is too long" }
        require(normalized.all { it.isLetterOrDigit() || it == '.' || it == '_' || it == '-' }) {
            "Cache key contains unsupported characters"
        }
        return "cache.v1.$normalized"
    }

    private fun preferenceCacheKey(key: String): String = "preference-$key"

    private companion object {
        const val REGISTRY_SCHEMA_VERSION = 1
        const val REGISTRY_KEY = "account-registry"
        const val USERNAME_KEY = "credentials.username"
        const val PASSWORD_KEY = "credentials.password"
        const val SESSION_TICKET_KEY = "session.primary.ticket"
        const val PROFILE_IMAGE_BYTES_KEY = "profile.image.bytes"
        const val PROFILE_IMAGE_SOURCE_KEY = "profile.image.source"
        const val PROFILE_IMAGE_SAVED_AT_KEY = "profile.image.saved-at"
        const val MAX_IDENTIFIER_ATTEMPTS = 8
    }
}

internal data class CachedProfileImage(
    val savedAtMillis: Long,
    val bytes: ByteArray,
)

@Serializable
private data class StoredAccountRegistry(
    val schemaVersion: Int,
    val installationId: String,
    val activeAccountId: String? = null,
    val accounts: List<StoredAccount> = emptyList(),
) {
    fun normalized(): StoredAccountRegistry {
        val uniqueAccounts =
            accounts
                .distinctBy(StoredAccount::accountId)
                .map(StoredAccount::withNormalizedProfiles)
        return copy(
            activeAccountId = activeAccountId?.takeIf { active ->
                uniqueAccounts.any { it.accountId == active }
            },
            accounts = uniqueAccounts,
        )
    }

    fun toSnapshot(): UniAccountRegistrySnapshot =
        UniAccountRegistrySnapshot(
            installationId = installationId,
            activeAccountId = activeAccountId,
            accounts = accounts.map(StoredAccount::toSummary),
        )
}

@Serializable
private data class StoredAccount(
    val accountId: String,
    val serverUserId: String,
    val displayName: String,
    val degreeName: String,
    val matricola: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val isGuest: Boolean,
    val activeProfileId: String? = null,
    val profiles: List<StoredAccountProfile> = emptyList(),
    val activeProfileType: BackendCareerType = BackendCareerType.STUDENT,
) {
    fun toSummary(): UniAccountSummary =
        UniAccountSummary(
            accountId = accountId,
            serverUserId = serverUserId,
            displayName = displayName,
            degreeName = degreeName,
            matricola = matricola,
            email = email,
            photoUrl = photoUrl,
            isGuest = isGuest,
            activeProfileId = activeProfileId,
            profiles = profiles.map(StoredAccountProfile::toSummary),
            activeProfileType = activeProfileType,
        )
}

@Serializable
private data class StoredAccountProfile(
    val profileId: String,
    val displayName: String,
    val degreeName: String,
    val matricola: String? = null,
    val matId: String? = null,
    val stuId: String? = null,
    val anaId: String? = null,
    val cdsId: String? = null,
    val dipId: String? = null,
    val departmentName: String? = null,
    val teacherId: String? = null,
    val type: BackendCareerType = BackendCareerType.STUDENT,
) {
    fun toSummary(): UniAccountProfileSummary =
        UniAccountProfileSummary(
            profileId = profileId,
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
}

private fun UniUserProfile.toStoredAccount(
    accountId: String,
    previousAccounts: List<StoredAccount>,
): StoredAccount {
    val incomingProfiles =
        profiles.map { profile ->
            StoredAccountProfile(
                profileId = profile.profileId,
                displayName = profile.displayName,
                degreeName = profile.degreeName,
                matricola = profile.matricola,
                matId = profile.matId,
                stuId = profile.stuId,
                anaId = profile.anaId,
                cdsId = profile.cdsId,
                dipId = profile.dipId,
                departmentName = profile.departmentName,
                teacherId = profile.teacherId,
                type = profile.type,
            )
        }
    val mergedProfiles =
        coalesceStoredProfiles(
            profiles = incomingProfiles + previousAccounts.flatMap(StoredAccount::profiles),
            activeProfileId = activeProfileId,
        )
    return StoredAccount(
        accountId = accountId,
        serverUserId = id,
        displayName = displayName,
        degreeName = degreeName,
        matricola = matricola,
        email = email,
        photoUrl = photoUrl,
        isGuest = isGuest,
        activeProfileId = activeProfileId,
        profiles = mergedProfiles,
        activeProfileType = activeProfileType,
    )
}

private fun List<StoredAccount>.upsert(account: StoredAccount): List<StoredAccount> =
    filterNot { it.accountId == account.accountId } + account

private fun StoredAccount.withNormalizedProfiles(): StoredAccount =
    copy(
        profiles = coalesceStoredProfiles(profiles, activeProfileId),
    )

private fun coalesceStoredProfiles(
    profiles: List<StoredAccountProfile>,
    activeProfileId: String?,
): List<StoredAccountProfile> {
    val result = mutableListOf<StoredAccountProfile>()
    profiles
        .distinctBy(StoredAccountProfile::profileId)
        .sortedByDescending { it.profileId == activeProfileId }
        .forEach { candidate ->
            val existingIndex = result.indexOfFirst { existing -> existing.sameCareerAs(candidate) }
            if (existingIndex < 0) {
                result += candidate
            } else {
                result[existingIndex] = result[existingIndex].mergeMetadata(candidate)
            }
        }
    return result
}

private fun StoredAccountProfile.sameCareerAs(other: StoredAccountProfile): Boolean {
    if (profileId == other.profileId) return true
    if (type != other.type) return false

    if (type == BackendCareerType.PROFESSOR) {
        if (teacherId.matchesNonBlank(other.teacherId)) return true
        if (
            dipId.matchesNonBlank(other.dipId) &&
            degreeName.normalizedProfilePart() == other.degreeName.normalizedProfilePart()
        ) return true
        return degreeName.normalizedProfilePart().isNotBlank() &&
            degreeName.normalizedProfilePart() == other.degreeName.normalizedProfilePart() &&
            displayName.normalizedProfilePart() == other.displayName.normalizedProfilePart()
    }

    if (!matId.isNullOrBlank() && !other.matId.isNullOrBlank()) return matId == other.matId
    if (!matricola.isNullOrBlank() && !other.matricola.isNullOrBlank()) {
        if (matricola != other.matricola) return false
        return cdsId.isNullOrBlank() || other.cdsId.isNullOrBlank() || cdsId == other.cdsId
    }
    if (stuId.matchesNonBlank(other.stuId) && cdsId.matchesNonBlank(other.cdsId)) return true
    return degreeName.normalizedProfilePart().isNotBlank() &&
        degreeName.normalizedProfilePart() == other.degreeName.normalizedProfilePart() &&
        displayName.normalizedProfilePart() == other.displayName.normalizedProfilePart()
}

private fun StoredAccountProfile.mergeMetadata(other: StoredAccountProfile): StoredAccountProfile =
    copy(
        displayName = other.displayName.ifBlank { displayName },
        degreeName = other.degreeName.ifBlank { degreeName },
        matricola = other.matricola.takeUnless { it.isNullOrBlank() } ?: matricola,
        matId = other.matId.takeUnless { it.isNullOrBlank() } ?: matId,
        stuId = other.stuId.takeUnless { it.isNullOrBlank() } ?: stuId,
        anaId = other.anaId.takeUnless { it.isNullOrBlank() } ?: anaId,
        cdsId = other.cdsId.takeUnless { it.isNullOrBlank() } ?: cdsId,
        dipId = other.dipId.takeUnless { it.isNullOrBlank() } ?: dipId,
        departmentName = other.departmentName.takeUnless { it.isNullOrBlank() } ?: departmentName,
        teacherId = other.teacherId.takeUnless { it.isNullOrBlank() } ?: teacherId,
    )

private fun String?.matchesNonBlank(other: String?): Boolean =
    !isNullOrBlank() && !other.isNullOrBlank() && trim().equals(other.trim(), ignoreCase = true)

private fun String.normalizedProfilePart(): String = trim().lowercase()
