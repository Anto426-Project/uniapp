package com.anto426.uniapp.ui.updates

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassColors
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.ui.components.items.formatCompactNewsDate
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.state.UniExpressiveStatusState
import com.anto426.uniapp.ui.document.UniDocumentScreen
import com.anto426.uniapp.ui.document.toMarkdownString
import com.anto426.uniapp.updates.presentation.AppUpdateUiState
import com.anto426.uniapp.updates.presentation.ChangelogUiState
import com.anto426.uniapp.updates.presentation.ChangelogViewModel
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun ChangelogRoute(updateUiState: AppUpdateUiState, viewModelKey: String) {
    val changelogViewModel = viewModel(key = viewModelKey) { ChangelogViewModel(updateUiState) }
    LaunchedEffect(
        changelogViewModel,
        updateUiState.releaseNotes,
        updateUiState.displayedVersion,
        updateUiState.installedVersion,
        updateUiState.publishedAt,
        updateUiState.channel,
    ) {
        changelogViewModel.update(updateUiState)
    }
    val changelogUiState by changelogViewModel.uiState.collectAsStateWithLifecycle()
    ChangelogScreen(
        uiState = changelogUiState,
        onExpansionChanged = changelogViewModel::setExpanded,
    )
}

@Composable
fun ChangelogScreen(
    uiState: ChangelogUiState,
    modifier: Modifier = Modifier,
    onExpansionChanged: ((String, Boolean) -> Unit)? = null,
    onBack: (() -> Unit)? = null,
) {
    val version = uiState.versions.firstOrNull()

    if (version == null || version.items.isEmpty()) {
        UniScreenColumn(modifier = modifier) {
            UniExpressiveStatusState(
                title = stringResource(Res.string.nav_route_changelog_title),
                description = stringResource(Res.string.msg_nessun_aggiornamento_o_nota_di_rilascio_disponibile_al),
                inline = true,
                mainIcon = LiquidIcons.Assignment,
            )
        }
        return
    }

    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val headerAccent = remember(version.channel, colorScheme, glassColors) {
        resolveReleaseChannelAccent(version.channel, colorScheme, glassColors)
    }
    val rawDate = version.date
    val formattedDate = remember(rawDate) {
        formatCompactNewsDate(rawDate, includeTime = false)
    }

    UniDocumentScreen(
        content = uiState.toMarkdownString(),
        modifier = modifier,
        title = "UniApp ${version.version}",
        subtitle = formattedDate.takeIf(String::isNotBlank) ?: stringResource(Res.string.ui_changelog_subtitle),
        headerIcon = LiquidIcons.Star,
        headerBadge = version.channel.takeIf(String::isNotBlank),
        headerAccentColor = headerAccent,
        sectionAccentResolver = { sectionTitle, index ->
            resolveChangelogSectionAccent(sectionTitle, index, colorScheme, glassColors)
        },
        onBack = onBack,
    )
}

internal fun resolveReleaseChannelAccent(
    channel: String,
    colorScheme: ColorScheme,
    glassColors: LiquidGlassColors,
): Color {
    val normalized = channel.trim().lowercase()
    return when {
        "alpha" in normalized || "dev" in normalized || "nightly" in normalized -> colorScheme.tertiary
        "beta" in normalized || "rc" in normalized || "preview" in normalized -> colorScheme.secondary
        else -> colorScheme.primary
    }
}

internal fun resolveChangelogSectionAccent(
    sectionTitle: String?,
    index: Int,
    colorScheme: ColorScheme,
    glassColors: LiquidGlassColors,
): Color {
    val normalized = sectionTitle?.trim()?.lowercase().orEmpty()
    return when {
        anyKeyword(normalized, "novit", "nuov", "feature", "new", "added", "aggiunt", "highlight", "what's changed") ->
            colorScheme.primary
        anyKeyword(normalized, "miglior", "improv", "ui", "ux", "design", "grafic", "polish", "restyl", "prestaz", "optim") ->
            colorScheme.secondary
        anyKeyword(normalized, "fix", "bug", "correz", "risol", "sicur", "secur", "patch", "stabil") ->
            colorScheme.tertiary
        anyKeyword(normalized, "atten", "break", "warn", "deprec", "remov", "rimoz", "note") ->
            colorScheme.error
        anyKeyword(normalized, "sdk", "dipend", "depend", "api", "build", "chore", "tech", "tecnic") ->
            colorScheme.secondary
        else -> {
            val palette = listOf(colorScheme.primary, colorScheme.secondary, colorScheme.tertiary)
            palette[index % palette.size]
        }
    }
}

private fun anyKeyword(text: String, vararg keywords: String): Boolean =
    keywords.any { keyword -> keyword in text }
