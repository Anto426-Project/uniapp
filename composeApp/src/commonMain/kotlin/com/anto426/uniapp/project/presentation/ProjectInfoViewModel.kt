package com.anto426.uniapp.project.presentation

import androidx.lifecycle.ViewModel
import com.anto426.uniapp.data.ProjectDataState
import com.anto426.uniapp.data.UniProjectManager

internal typealias ProjectInfoUiState = ProjectDataState

/** This screen observes application-owned content; its lifetime never owns the network cache. */
internal class ProjectInfoViewModel(private val data: UniProjectManager) : ViewModel() {
    val uiState = data.uiState
    fun refresh(force: Boolean = false) = data.refresh(force)
}
