package com.anto426.uniapp.ui.updates

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

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
        stringResource(Res.string.msg_nessun_aggiornamento_o_nota_di_rilascio_disponibile_al)
    } else {
        uiState.toMarkdownString()
    }

    UniDocumentScreen(
        content = content,
        modifier = modifier,
        onBack = onBack,
    )
}
