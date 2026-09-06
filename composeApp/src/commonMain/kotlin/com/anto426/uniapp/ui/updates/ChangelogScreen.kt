package com.anto426.uniapp.ui.updates

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anto426.uniapp.ui.document.UniDocumentScreen
import com.anto426.uniapp.ui.document.toMarkdownString
import com.anto426.uniapp.updates.presentation.ChangelogUiState

@Composable
fun ChangelogScreen(
    uiState: ChangelogUiState,
    modifier: Modifier = Modifier,
    onExpansionChanged: ((String, Boolean) -> Unit)? = null,
    onBack: (() -> Unit)? = null,
) {
    val content = if (uiState.versions.isEmpty()) {
        "Nessun aggiornamento o nota di rilascio disponibile al momento."
    } else {
        uiState.toMarkdownString()
    }

    UniDocumentScreen(
        content = content,
        modifier = modifier,
        onBack = onBack,
    )
}
