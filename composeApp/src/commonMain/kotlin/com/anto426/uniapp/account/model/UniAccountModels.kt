package com.anto426.uniapp.account.model

import com.anto426.unisdk.backend.model.BackendCareerType

/** Credentials owned by UniApp. They are never included in the SDK session ticket. */
class UniAccountCredentials(
    username: String,
    password: String,
) {
    internal val username = username
    internal val password = password

    init {
        require(username.isNotBlank()) { "The username cannot be blank" }
        require(password.isNotEmpty()) { "The password cannot be empty" }
    }

    override fun toString(): String = "UniAccountCredentials([REDACTED])"
}

data class UniAccountSummary(
    val accountId: String,
    val serverUserId: String,
    val displayName: String,
    val degreeName: String,
    val matricola: String?,
    val email: String?,
    val photoUrl: String?,
    val isGuest: Boolean,
    val activeProfileId: String? = null,
    val profiles: List<UniAccountProfileSummary> = emptyList(),
    val activeProfileType: BackendCareerType = BackendCareerType.STUDENT,
) {
    val isProfessor: Boolean
        get() = activeProfileType == BackendCareerType.PROFESSOR
}

data class UniAccountProfileSummary(
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
)

data class UniAccountRegistrySnapshot(
    val installationId: String,
    val activeAccountId: String?,
    val accounts: List<UniAccountSummary>,
)

class MissingAccountCredentialsException(accountId: String) :
    IllegalStateException("Encrypted credentials are missing for account '$accountId'")
