package com.anto426.uniapp.account.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Role of an academic career/profile belonging to a university account. */
@Serializable
enum class UniProfileRole {
    @SerialName("STUDENT")
    STUDENT,

    @SerialName("PROFESSOR")
    PROFESSOR,
}

/** Career/profile choice presented during multi-career authentication. */
data class UniLoginProfileOption(
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
    val type: UniProfileRole = UniProfileRole.STUDENT,
) {
    val profileId: String
        get() =
            when (type) {
                UniProfileRole.PROFESSOR ->
                    listOfNotNull(
                        "prof",
                        teacherId?.trim()?.takeIf(String::isNotBlank),
                        dipId?.trim()?.takeIf(String::isNotBlank),
                        displayName.trim().lowercase().takeIf(String::isNotBlank),
                    ).joinToString(":")

                UniProfileRole.STUDENT ->
                    listOfNotNull(
                        "stu",
                        matId?.trim()?.takeIf(String::isNotBlank),
                        stuId?.trim()?.takeIf(String::isNotBlank),
                        matricola?.trim()?.takeIf(String::isNotBlank),
                        cdsId?.trim()?.takeIf(String::isNotBlank),
                        degreeName.trim().lowercase().takeIf(String::isNotBlank),
                    ).joinToString(":")
            }
}

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
    val activeProfileType: UniProfileRole = UniProfileRole.STUDENT,
) {
    val isProfessor: Boolean
        get() = activeProfileType == UniProfileRole.PROFESSOR
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
    val type: UniProfileRole = UniProfileRole.STUDENT,
)

data class UniAccountRegistrySnapshot(
    val installationId: String,
    val activeAccountId: String?,
    val accounts: List<UniAccountSummary>,
)

class MissingAccountCredentialsException(accountId: String) :
    IllegalStateException("Encrypted credentials are missing for account '$accountId'")
