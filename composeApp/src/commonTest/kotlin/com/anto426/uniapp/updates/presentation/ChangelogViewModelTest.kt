package com.anto426.uniapp.updates.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChangelogViewModelTest {
    @Test
    fun notesComeOnlyFromTheUpdateSourceAndCanArriveAfterOpening() {
        val viewModel = ChangelogViewModel(AppUpdateUiState(displayedVersion = "2.1"))
        assertTrue(viewModel.uiState.value.versions.isEmpty())

        viewModel.update(
            AppUpdateUiState(
                displayedVersion = "2.1",
                releaseNotes = "Correzioni effettive",
                publishedAt = "2026-09-27",
                channel = "stable",
            ),
        )
        val version = viewModel.uiState.value.versions.single()
        assertEquals("v2.1", version.version)
        assertEquals("Correzioni effettive", version.items.single().rawDescription)
        assertEquals("stable", version.channel)
    }
}
