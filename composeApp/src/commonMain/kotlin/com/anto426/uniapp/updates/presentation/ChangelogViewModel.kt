package com.anto426.uniapp.updates.presentation

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.anto426.uniapp.model.updates.ChangelogItemData
import com.anto426.uniapp.model.updates.ChangelogVersionData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uniapp.composeapp.generated.resources.*

data class ChangelogUiState(
    val versions: List<ChangelogVersionData> = emptyList(),
    val expandedVersion: String = "",
)

class ChangelogViewModel(update: AppUpdateUiState) : ViewModel() {
    private val versions = buildSingleVersion(update)

    private val mutableUiState = MutableStateFlow(
        ChangelogUiState(
            versions = versions,
            expandedVersion = versions.firstOrNull()?.version.orEmpty(),
        ),
    )
    val uiState: StateFlow<ChangelogUiState> = mutableUiState.asStateFlow()

    fun update(update: AppUpdateUiState) {
        val versions = buildSingleVersion(update)
        if (versions == mutableUiState.value.versions) return
        mutableUiState.value = ChangelogUiState(
            versions = versions,
            expandedVersion = versions.firstOrNull()?.version.orEmpty(),
        )
    }

    fun setExpanded(version: String, expanded: Boolean) {
        if (version !in mutableUiState.value.versions.map { it.version }) return
        mutableUiState.value = mutableUiState.value.copy(expandedVersion = if (expanded) version else "")
    }

    private companion object {
        fun buildSingleVersion(update: AppUpdateUiState): List<ChangelogVersionData> {
            val notes = update.releaseNotes?.trim()?.takeIf(String::isNotBlank) ?: return emptyList()
            val rawVer = update.displayedVersion.ifBlank { update.installedVersion }.removePrefix("v")
            if (rawVer.isBlank()) return emptyList()

            return listOf(
                ChangelogVersionData(
                    version = "v$rawVer",
                    rawDate = update.publishedAt.orEmpty(),
                    items = listOf(
                        ChangelogItemData(
                            tag = "UPDATE",
                            tagColor = Color(0xFF4A90D9),
                            titleRes = Res.string.nav_route_changelog_title,
                            rawDescription = notes,
                        ),
                    ),
                    channel = update.channel.orEmpty(),
                ),
            )
        }
    }
}
