package com.anto426.uniapp.session.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.session.AppSessionController
import com.anto426.uniapp.session.model.AppSessionState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppSessionViewModel(
    private val sessionController: AppSessionController,
) : ViewModel() {
    val state: StateFlow<AppSessionState> = sessionController.state

    init {
        viewModelScope.launch { sessionController.initialize() }
    }

    fun signOut() {
        viewModelScope.launch { sessionController.signOut() }
    }
}
