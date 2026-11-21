package com.anto426.uniapp.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.interaction.liquidClickable
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.project.presentation.ProjectInfoUiState
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.display.UniRemoteAvatar
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun CreatorCreditsScreen(
    state: ProjectInfoUiState,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val author = state.data?.author
    val creatorName = author?.name?.takeIf(String::isNotBlank) ?: state.authorLogin
    val creatorHandle = "@${author?.login ?: state.authorLogin}"
    val creator = state.data?.contributors.orEmpty().firstOrNull { it.login.equals(state.authorLogin, true) }
    val contributors = state.data?.contributors.orEmpty().filterNot { it.login.equals(state.authorLogin, true) }
    val rowShape = remember { RoundedRectangle(16.dp) }

    UniScreenColumn(modifier = modifier) {
        if (state.loading || state.stale || state.error) {
            val statusIcon = if (state.loading) LiquidIcons.Refresh else LiquidIcons.Info
            val statusText = when {
                state.loading -> stringResource(Res.string.ui_project_syncing)
                state.stale -> stringResource(Res.string.ui_project_cached)
                else -> stringResource(Res.string.ui_project_load_failed)
            }
            val statusTint = if (state.loading) colorScheme.primary else colorScheme.onSurfaceVariant
            LiquidCard(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedRectangle(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusTint,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusTint,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        // 1. Creatore & Lead Developer Hero Card
        LiquidCard(
            contentPadding = PaddingValues(22.dp),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedRectangle(28.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(2.dp, colorScheme.primary.copy(alpha = 0.28f), CircleShape),
                    ) {
                        UniRemoteAvatar(
                            imageUrl = author?.avatarUrl ?: creator?.avatarUrl,
                            name = creatorName,
                            size = 84.dp,
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        LiquidBadge(
                            text = stringResource(Res.string.ui_project_creator).uppercase(),
                            containerColor = colorScheme.primaryContainer.copy(alpha = 0.55f),
                            contentColor = colorScheme.primary,
                        )
                        Text(
                            text = creatorName,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.4).sp,
                            ),
                            color = colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = creatorHandle,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    val bioText = author?.bio?.takeIf(String::isNotBlank)
                        ?: stringResource(Res.string.msg_ideatore_e_sviluppatore_principale_dell_ecosistema_uniapp_per)
                    Text(
                        text = bioText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                    )
                }

                LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))

                // Unified Metrics Strip (senza card annidate)
                val repos = author?.publicRepositories?.takeIf { it > 0 }?.toString() ?: "12+"
                val contribs = creator?.contributions?.takeIf { it > 0 }?.toString() ?: "Core"
                val followers = author?.followers?.takeIf { it > 0 }?.toString() ?: "—"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    listOf(
                        "Progetti" to repos,
                        "Contributi" to contribs,
                        "Follower" to followers,
                    ).forEach { (label, value) ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = value,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFeatureSettings = "tnum",
                                ),
                                color = colorScheme.primary,
                                maxLines = 1,
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                }

                LiquidButton(
                    text = stringResource(Res.string.ui_project_github_profile),
                    onClick = { onOpenLink(state.authorUrl) },
                    containerColor = colorScheme.primaryContainer,
                    contentColor = colorScheme.onPrimaryContainer,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = LiquidIcons.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // 2. Crediti e Riconoscimenti
        UniInsetGroup(title = stringResource(Res.string.ui_credits_acknowledgements)) {
            Text(
                text = stringResource(Res.string.ui_credits_explanation),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
            )

            contributors.forEach { person ->
                LiquidHorizontalDivider(
                    modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                    color = colorScheme.outlineVariant.copy(alpha = 0.25f),
                )
                val name = person.login ?: person.name ?: stringResource(Res.string.ui_project_unlinked_contributor)
                val link = person.url
                val isBot = person.type == "Bot"
                val badgeTint = if (isBot) colorScheme.tertiary else colorScheme.primary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp)
                        .graphicsLayer(clip = false)
                        .then(
                            if (link != null) {
                                Modifier.liquidClickable(shape = rowShape, role = Role.Button) { onOpenLink(link) }
                            } else {
                                Modifier
                            },
                        )
                        .semantics(mergeDescendants = true) {}
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(1.dp, badgeTint.copy(alpha = 0.24f), CircleShape),
                    ) {
                        UniRemoteAvatar(
                            imageUrl = person.avatarUrl,
                            name = name,
                            size = 40.dp,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = stringResource(Res.string.ui_project_contributions, person.contributions),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    LiquidBadge(
                        text = if (isBot) "Bot" else "Contributor",
                        containerColor = badgeTint.copy(alpha = 0.14f),
                        contentColor = badgeTint,
                    )
                    if (link != null) {
                        Icon(
                            imageVector = LiquidIcons.ChevronRight,
                            contentDescription = null,
                            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.42f),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            if (contributors.isEmpty()) {
                LiquidHorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = colorScheme.outlineVariant.copy(alpha = 0.25f),
                )
                UniInsetRow(
                    title = stringResource(Res.string.msg_contributori_della_community),
                    subtitle = stringResource(Res.string.ui_credits_no_other_contributors),
                    icon = LiquidIcons.AccountCircle,
                    iconTint = colorScheme.primary,
                    showDivider = false,
                )
            }
        }
    }
}
