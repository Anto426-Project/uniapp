package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.account.model.UniProfileRole
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.observeIn
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.anto426.uniapp.localization.readAppString
import uniapp.composeapp.generated.resources.*

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
    val badgeBarcodeValue: String = "",
    val photoData: ByteArray? = null,
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
) {
    val initials: String
        get() = fullName.split(' ').filter(String::isNotBlank).take(2).map { it.first() }.joinToString("")

    val badgeDisplayValue: String
        get() = badgeCode.ifBlank { matricola }
}

class AcademicIdentityViewModel(
    private val dataSource: UniGateway,
    private val account: UniAccountSummary? = null,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(AcademicIdentityUiState())
    val uiState: StateFlow<AcademicIdentityUiState> = mutableUiState.asStateFlow()

    private val repository = dataSource.asUniRepository(viewModelScope)
    private val sharedData = repository.engine
    private val dataRequests = if (account?.isProfessor == true) emptyList() else listOf(UniDatasets.Student)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        try {
            if (account?.isProfessor == true) {
                loadProfessorIdentity()
            } else {
                val details = snapshot.require(UniDatasets.Student)
                mutableUiState.value = mutableUiState.value.copy(
                    fullName = details.fullName,
                    matricola = details.matricola.orEmpty(),
                    degreeName = details.degreeName.orEmpty(),
                    departmentName = details.departmentName.orEmpty(),
                    badgeCode = details.badgeCode.orEmpty(),
                    badgeQrValue = details.resolvedBadgeQrValue(
                        fallbackFullName = account?.displayName,
                        fallbackMatricola = account?.matricola,
                        fallbackDegreeName = account?.degreeName,
                    ),
                    badgeBarcodeValue = details.resolvedBadgeBarcodeValue(
                        fallbackMatricola = account?.matricola,
                    ),
                    loadState = FeatureLoadState.Content,
                    errorMessage = null,
                )
            }
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage { readAppString(Res.string.msg_impossibile_caricare_il_badge_studente) },
            )
        }
    }

    init {
        repository.didactics.startPortrait(account)
        viewModelScope.launch {
            repository.didactics.portrait.collect { image -> mutableUiState.value = mutableUiState.value.copy(photoData = image) }
        }
    }

    fun refresh(force: Boolean = false) {
        repository.didactics.refresh(dataRequests, force)
        if (force) repository.didactics.refreshPortrait()
    }

    private fun loadProfessorIdentity() {
        val professor = checkNotNull(account)
        val profile =
            professor.profiles.firstOrNull { it.profileId == professor.activeProfileId }
                ?: professor.profiles.firstOrNull {
                    it.type == UniProfileRole.PROFESSOR
                }
        mutableUiState.value =
            mutableUiState.value.copy(
                isProfessor = true,
                fullName = professor.displayName,
                username = professor.serverUserId,
                teacherId = profile?.teacherId.orEmpty(),
                degreeName = "",
                departmentName = profile?.departmentName.orEmpty(),
                departmentId = profile?.dipId.orEmpty(),
                loadState = FeatureLoadState.Content,
            )
    }
}
