package com.anto426.uniapp.ui.updates

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.document.UniDocumentScreen
import com.anto426.uniapp.ui.document.toMarkdownString
import com.anto426.uniapp.updates.presentation.ChangelogUiState
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

/**
 * Schermata Note di Rilascio UniApp.
 *
 * Collegata direttamente al componente originario UniDocumentScreen,
 * presenta il changelog di una sola versione organizzato per categorie e con formattazione curata.
 */
@Composable
fun ChangelogScreen(
    uiState: ChangelogUiState,
    modifier: Modifier = Modifier,
    onExpansionChanged: ((String, Boolean) -> Unit)? = null,
    onBack: (() -> Unit)? = null,
) {
    val version = uiState.versions.firstOrNull()

    val content = if (version == null || version.items.isEmpty()) {
        stringResource(Res.string.msg_nessun_aggiornamento_o_nota_di_rilascio_disponibile_al)
    } else {
        uiState.toMarkdownString()
    }

    UniDocumentScreen(
        content = content,
        modifier = modifier,
        title = version?.let { "UniApp ${it.version}" } ?: stringResource(Res.string.nav_route_changelog_title),
        subtitle = version?.date?.takeIf(String::isNotBlank) ?: stringResource(Res.string.ui_changelog_subtitle),
        headerIcon = LiquidIcons.Assignment,
        headerBadge = version?.channel?.takeIf(String::isNotBlank),
        onBack = onBack,
    )
}
