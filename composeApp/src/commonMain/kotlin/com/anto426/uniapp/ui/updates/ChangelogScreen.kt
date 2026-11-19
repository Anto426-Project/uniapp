package com.anto426.uniapp.ui.updates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.document.UniDocumentScreen
import com.anto426.uniapp.ui.document.toMarkdownString
import com.anto426.uniapp.updates.presentation.ChangelogUiState
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

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
            LiquidCard(
                contentPadding = PaddingValues(28.dp),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedRectangle(24.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = LiquidIcons.Assignment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = 52.dp,
                            iconSize = 26.dp,
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                            shape = RoundedRectangle(16.dp),
                        ),
                    )
                    Text(
                        text = stringResource(Res.string.nav_route_changelog_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(Res.string.msg_nessun_aggiornamento_o_nota_di_rilascio_disponibile_al),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        return
    }

    UniDocumentScreen(
        content = uiState.toMarkdownString(),
        modifier = modifier,
        title = "UniApp ${version.version}",
        subtitle = version.date.takeIf(String::isNotBlank) ?: stringResource(Res.string.ui_changelog_subtitle),
        headerIcon = LiquidIcons.Star,
        headerBadge = version.channel.takeIf(String::isNotBlank),
        onBack = onBack,
    )
}
