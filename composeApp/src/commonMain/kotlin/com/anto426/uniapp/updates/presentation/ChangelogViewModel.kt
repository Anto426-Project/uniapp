package com.anto426.uniapp.updates.presentation

import androidx.lifecycle.ViewModel
import androidx.compose.ui.graphics.Color
import com.anto426.uniapp.model.updates.ChangelogItemData
import com.anto426.uniapp.model.updates.ChangelogVersionData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ChangelogUiState(
    val versions: List<ChangelogVersionData> = emptyList(),
    val expandedVersion: String = "",
)

class ChangelogViewModel(update: AppUpdateUiState) : ViewModel() {
    private val versions =
        update.releaseNotes?.takeIf(String::isNotBlank)?.let { notes ->
            listOf(
                ChangelogVersionData(
                    version = "v${update.displayedVersion}",
                    date = update.publishedAt.orEmpty(),
                    items = listOf(
                        ChangelogItemData(
                            tag = "UPDATE",
                            tagColor = Color(0xFF4A90D9),
                            title = "Note di rilascio",
                            description = notes,
                        ),
                    ),
                ),
            )
        }.orEmpty()
    private val mutableUiState =
        MutableStateFlow(
            ChangelogUiState(
                versions = versions,
                expandedVersion = versions.firstOrNull()?.version.orEmpty(),
            ),
        )
    val uiState: StateFlow<ChangelogUiState> = mutableUiState.asStateFlow()

    fun setExpanded(version: String, expanded: Boolean) {
        if (version !in mutableUiState.value.versions.map { it.version }) return
        mutableUiState.value = mutableUiState.value.copy(expandedVersion = if (expanded) version else "")
    }
}
