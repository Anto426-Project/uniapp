package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.toAttendanceData
import com.anto426.uniapp.model.didactics.AttendanceData
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AttendanceUiState(
    val records: List<AttendanceData> = emptyList(),
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
    val isRegistering: Boolean = false,
    val registrationSuccessMessage: String? = null,
    val registrationErrorMessage: String? = null,
) {
    val totalCoursesCount: Int get() = records.size
    val totalAttendedLectures: Int get() = records.sumOf { it.records.size.takeIf { s -> s > 0 } ?: 1 }
    val averageAttendancePercent: Int get() {
        val validPercentages = records.mapNotNull {
            it.percentage.replace("%", "").trim().toIntOrNull()
        }
        return if (validPercentages.isNotEmpty()) validPercentages.average().toInt() else 0
    }
}

class AttendanceViewModel(private val dataSource: UniAppDataSource) : ViewModel() {
    private val mutableUiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = mutableUiState.asStateFlow()

    init { refresh() }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(
                loadState = if (mutableUiState.value.records.isEmpty()) FeatureLoadState.Loading else mutableUiState.value.loadState,
                errorMessage = null
            )
            try {
                val records = dataSource.loadAttendanceHistory(force).toAttendanceData()
                mutableUiState.value = mutableUiState.value.copy(
                    records = records,
                    loadState = if (records.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
                    errorMessage = null,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(
                    loadState = if (mutableUiState.value.records.isEmpty()) FeatureLoadState.Error else FeatureLoadState.Content,
                    errorMessage = error.userMessage("Impossibile caricare le presenze."),
                )
            }
        }
    }

    fun registerAttendance(
        qrCode: String,
        latitude: Double? = null,
        longitude: Double? = null,
        accuracy: Double? = null,
        onSuccess: (() -> Unit)? = null,
    ) {
        val trimmedCode = qrCode.trim()
        if (trimmedCode.isBlank()) {
            mutableUiState.value = mutableUiState.value.copy(
                registrationErrorMessage = "Inserisci o inquadra un codice QR valido."
            )
            return
        }

        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(
                isRegistering = true,
                registrationSuccessMessage = null,
                registrationErrorMessage = null,
            )
            try {
                val result = dataSource.registerAttendance(
                    qrCode = trimmedCode,
                    deviceLatitude = latitude,
                    deviceLongitude = longitude,
                    deviceAccuracyMeters = accuracy,
                )
                mutableUiState.value = mutableUiState.value.copy(
                    isRegistering = false,
                    registrationSuccessMessage = result.ifBlank { "Presenza registrata con successo!" },
                    registrationErrorMessage = null,
                )
                refresh(force = true)
                onSuccess?.invoke()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(
                    isRegistering = false,
                    registrationErrorMessage = error.userMessage("Errore durante la registrazione della presenza."),
                )
            }
        }
    }

    fun clearRegistrationStatus() {
        mutableUiState.value = mutableUiState.value.copy(
            registrationSuccessMessage = null,
            registrationErrorMessage = null,
        )
    }
}
