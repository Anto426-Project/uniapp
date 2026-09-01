package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AcademicIdentityUiState(
    val isProfessor: Boolean = false,
    val fullName: String = "",
    val username: String = "",
    val teacherId: String = "",
    val matricola: String = "",
    val degreeName: String = "",
    val departmentName: String = "",
    val departmentId: String = "",
    val badgeCode: String = "",
    val badgeQrValue: String = "",
    val photoData: ByteArray? = null,
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
) {
    val initials: String
        get() = fullName.split(' ').filter(String::isNotBlank).take(2).map { it.first() }.joinToString("")

    val badgeDisplayValue: String
        get() = badgeQrValue.ifBlank { badgeCode.ifBlank { matricola } }
}

class AcademicIdentityViewModel(
    private val dataSource: UniAppDataSource,
    private val account: UniAccountSummary? = null,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(AcademicIdentityUiState())
    val uiState: StateFlow<AcademicIdentityUiState> = mutableUiState.asStateFlow()

    init { refresh() }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            try {
                if (account?.isProfessor == true) {
                    loadProfessorIdentity(force)
                    return@launch
                }
                val details = dataSource.loadStudentDetails(force)
                mutableUiState.value = AcademicIdentityUiState(
                    fullName = details.fullName,
                    matricola = details.matricola.orEmpty(),
                    degreeName = details.degreeName.orEmpty(),
                    departmentName = details.departmentName.orEmpty(),
                    badgeCode = details.badgeCode.orEmpty(),
                    badgeQrValue = details.badgeQrValue.orEmpty(),
                    loadState = FeatureLoadState.Content,
                )
                details.photoUrl?.takeIf(String::isNotBlank)?.let { source ->
                    runCatching { dataSource.loadProfileImage(source, force) }
                        .getOrNull()
                        ?.let { image ->
                            mutableUiState.value = mutableUiState.value.copy(photoData = image)
                        }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = AcademicIdentityUiState(
                    loadState = FeatureLoadState.Error,
                    errorMessage = error.userMessage(
                        if (account?.isProfessor == true) "Impossibile caricare l’identità docente."
                        else "Impossibile caricare il badge studente.",
                    ),
                )
            }
        }
    }

    private suspend fun loadProfessorIdentity(force: Boolean) {
        val professor = checkNotNull(account)
        val profile =
            professor.profiles.firstOrNull { it.profileId == professor.activeProfileId }
                ?: professor.profiles.firstOrNull {
                    it.type == com.anto426.unisdk.backend.model.BackendCareerType.PROFESSOR
                }
        mutableUiState.value =
            AcademicIdentityUiState(
                isProfessor = true,
                fullName = professor.displayName,
                username = professor.serverUserId,
                teacherId = profile?.teacherId.orEmpty(),
                degreeName = "",
                departmentName = profile?.departmentName.orEmpty(),
                departmentId = profile?.dipId.orEmpty(),
                loadState = FeatureLoadState.Content,
            )
        professor.photoUrl?.takeIf(String::isNotBlank)?.let { source ->
            runCatching { dataSource.loadProfileImage(source, force) }
                .getOrNull()
                ?.let { image -> mutableUiState.value = mutableUiState.value.copy(photoData = image) }
        }
    }
}
