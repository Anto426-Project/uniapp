package com.anto426.uniapp.services.presentation

import androidx.lifecycle.ViewModel
import com.anto426.uniapp.model.services.ServiceData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ServicesUiState(
    val studentServices: List<ServiceData> = emptyList(),
    val universityPortals: List<ServiceData> = emptyList(),
)

class ServicesViewModel(
    studentServices: List<ServiceData>,
    universityPortals: List<ServiceData>,
) : ViewModel() {
    private val mutableUiState =
        MutableStateFlow(
            ServicesUiState(
                studentServices = studentServices,
                universityPortals = universityPortals,
            ),
        )
    val uiState: StateFlow<ServicesUiState> = mutableUiState.asStateFlow()
}
