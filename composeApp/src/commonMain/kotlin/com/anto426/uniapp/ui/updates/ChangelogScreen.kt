package com.anto426.uniapp.ui.updates

import androidx.compose.runtime.Composable
import com.anto426.uniapp.updates.presentation.ChangelogUiState
import com.anto426.uniapp.ui.components.items.ChangelogVersion
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop

@Composable
fun ChangelogScreen(
    backdropState: Backdrop,
    uiState: ChangelogUiState,
    onExpansionChanged: (String, Boolean) -> Unit,
) {
    UniScreenColumn {
        uiState.versions.forEach { version ->
            ChangelogVersion(
                version,
                uiState.expandedVersion == version.version,
                { expanded -> onExpansionChanged(version.version, expanded) },
                backdropState,
            )
        }
    }
}
