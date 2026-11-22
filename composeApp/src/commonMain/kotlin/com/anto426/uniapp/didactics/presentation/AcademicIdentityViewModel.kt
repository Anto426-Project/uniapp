package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.identityContract
import com.anto426.uniapp.data.observeIn
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
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
            val identity = snapshot.identityContract(account, requireStudent = account?.isProfessor != true)
            mutableUiState.value = mutableUiState.value.copy(
                isProfessor = identity.isProfessor,
                fullName = identity.fullName,
                username = identity.username,
                teacherId = identity.teacherId,
                matricola = identity.matricola,
                degreeName = if (identity.isProfessor) "" else identity.degreeName,
                departmentName = identity.departmentName,
                departmentId = identity.departmentId,
                badgeCode = identity.badgeCode,
                badgeQrValue = identity.badgeQrValue,
                badgeBarcodeValue = identity.badgeBarcodeValue,
                loadState = FeatureLoadState.Content,
                errorMessage = null,
            )
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage { getString(Res.string.msg_impossibile_caricare_il_badge_studente) },
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
}
