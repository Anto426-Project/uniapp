package com.anto426.uniapp.data.contract

import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.account.model.UniProfileRole
import com.anto426.unisdk.model.StudentDetails

internal fun buildIdentityContract(
    account: UniAccountSummary? = null,
    student: StudentDetails? = null,
    photoData: ByteArray? = null,
): UniIdentityContract {
    if (account?.isProfessor == true) {
        val profile = account.profiles.firstOrNull { it.profileId == account.activeProfileId }
            ?: account.profiles.firstOrNull { it.type == UniProfileRole.PROFESSOR }
        val fullName = account.displayName.trim()
        return UniIdentityContract(
            isProfessor = true,
            fullName = fullName,
            initials = fullName.computeInitials(),
            username = account.serverUserId,
            matricola = account.serverUserId,
            email = account.email.orEmpty(),
            degreeName = profile?.departmentName.orEmpty(),
            departmentName = profile?.departmentName.orEmpty(),
            departmentId = profile?.dipId.orEmpty(),
            teacherId = profile?.teacherId.orEmpty(),
            degreeSummary = profile?.departmentName.orEmpty(),
            photoUrl = account.photoUrl,
            photoData = photoData,
        )
    }

    val activeProfile = account?.profiles?.firstOrNull { it.profileId == account.activeProfileId }
    val fullName = student?.fullName?.trim()?.ifBlank { null }
        ?: activeProfile?.displayName?.trim()?.ifBlank { null }
        ?: account?.displayName?.trim().orEmpty()
    val matricola = student?.matricola?.trim()?.ifBlank { null }
        ?: activeProfile?.matricola?.trim()?.ifBlank { null }
        ?: account?.matricola?.trim().orEmpty()
    val degreeName = student?.degreeName?.trim()?.ifBlank { null }
        ?: activeProfile?.degreeName?.trim()?.ifBlank { null }
        ?: account?.degreeName?.trim().orEmpty()
    val departmentName = student?.departmentName?.trim()?.ifBlank { null }
        ?: activeProfile?.departmentName?.trim().orEmpty()
    val badgeCode = student?.badgeCode?.trim().orEmpty()
    val qrValue = student?.badgeQrValue?.trim()?.takeIf(String::isNotBlank) ?: run {
        val parts = fullName.split(' ').filter(String::isNotBlank)
        val firstName = parts.firstOrNull()?.uppercase().orEmpty()
        val lastName = parts.drop(1).joinToString(" ").uppercase()
        listOf(matricola, lastName, firstName, degreeName)
            .filter(String::isNotBlank)
            .joinToString("|")
    }
    val barcodeValue = student?.badgeBarcodeValue?.trim()?.takeIf(String::isNotBlank)
        ?: badgeCode.takeIf(String::isNotBlank)
        ?: matricola
    val primaryStudentId = listOfNotNull(
        student?.stuId,
        student?.matId,
        student?.matricola,
        activeProfile?.stuId,
        activeProfile?.matId,
        activeProfile?.matricola,
        account?.matricola,
    ).firstOrNull { it.isNotBlank() }

    return UniIdentityContract(
        isProfessor = false,
        fullName = fullName,
        initials = fullName.computeInitials(),
        username = account?.serverUserId.orEmpty(),
        matricola = matricola,
        email = student?.email?.trim()?.ifBlank { null } ?: account?.email.orEmpty(),
        degreeName = degreeName,
        departmentName = departmentName,
        departmentId = student?.dipId?.trim()?.ifBlank { null } ?: activeProfile?.dipId.orEmpty(),
        degreeSummary = listOfNotNull(
            departmentName.takeIf(String::isNotBlank),
            matricola.takeIf(String::isNotBlank)?.let { "Matricola $it" },
        ).joinToString(" • "),
        primaryStudentId = primaryStudentId,
        badgeCode = badgeCode,
        badgeQrValue = qrValue,
        badgeBarcodeValue = barcodeValue,
        photoUrl = student?.photoUrl?.takeIf(String::isNotBlank) ?: account?.photoUrl,
        photoData = photoData,
    )
}

internal fun String.computeInitials(): String =
    trim().split(' ').filter(String::isNotBlank).take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")
