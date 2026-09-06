package com.anto426.uniapp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.project.presentation.ProjectInfoUiState
import com.anto426.uniapp.ui.components.display.UniRemoteAvatar
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.unisdk.platform.ProjectInfo
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun CreatorCreditsScreen(
    state: ProjectInfoUiState,
    onRefresh: () -> Unit,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val author = state.data?.author
    val creatorName = author?.name?.takeIf(String::isNotBlank) ?: ProjectInfo.authorLogin
    val creator = state.data?.contributors.orEmpty().firstOrNull { it.login.equals(ProjectInfo.authorLogin, true) }
    val contributors = state.data?.contributors.orEmpty().filterNot { it.login.equals(ProjectInfo.authorLogin, true) }

    UniScreenColumn(modifier = modifier) {
        // Stato di sincronizzazione discreto
        if (state.loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = LiquidIcons.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Sincronizzazione profilo e crediti da GitHub…",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        } else if (state.stale || state.error) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.40f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = LiquidIcons.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(if (state.stale) Res.string.ui_project_cached else Res.string.ui_project_load_failed),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }

        // 1. Creatore & Lead Developer Hero Card (senza cerchio/bordo attorno all'avatar)
        LiquidCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 22.dp,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Avatar diretto e pulito, senza cerchi/bordi esterni
                    UniRemoteAvatar(
                        imageUrl = author?.avatarUrl ?: creator?.avatarUrl,
                        name = creatorName,
                        size = 72.dp,
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.ui_project_creator).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.8.sp,
                            )
                        }

                        Text(
                            text = creatorName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Text(
                            text = "@${author?.login ?: ProjectInfo.authorLogin}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                val bioText = author?.bio?.takeIf(String::isNotBlank)
                    ?: "Ideatore e sviluppatore principale dell'ecosistema UniApp per gli studenti universitari."
                Text(
                    text = bioText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp,
                )

                // Metriche dell'autore su GitHub
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val repos = author?.publicRepositories?.takeIf { it > 0 }?.toString() ?: "12+"
                    val contribs = creator?.contributions?.takeIf { it > 0 }?.toString() ?: "Core"
                    val followers = author?.followers?.takeIf { it > 0 }?.toString() ?: "—"

                    listOf(
                        "Progetti" to repos,
                        "Contributi" to contribs,
                        "Follower" to followers,
                    ).forEach { (label, value) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(14.dp),
                                )
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                LiquidButton(
                    text = stringResource(Res.string.ui_project_github_profile),
                    onClick = { onOpenLink(ProjectInfo.authorUrl) },
                    variant = LiquidButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // 2. Crediti e Riconoscimenti alle Persone (Contributori GitHub & Collaboratori)
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_credits_acknowledgements)) {
            Text(
                text = stringResource(Res.string.ui_credits_explanation),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
            )

            contributors.forEachIndexed { index, person ->
                LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                val name = person.login ?: person.name ?: stringResource(Res.string.ui_project_unlinked_contributor)
                LiquidPreferenceItem(
                    title = name,
                    subtitle = stringResource(Res.string.ui_project_contributions, person.contributions) +
                        if (person.type == "Bot") " · Bot automatizzato" else " · Contributore",
                    leadingContent = { UniRemoteAvatar(person.avatarUrl, name, size = 38.dp) },
                    onClick = person.url?.let { link -> { onOpenLink(link) } },
                )
            }

            if (contributors.isEmpty()) {
                LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                LiquidPreferenceItem(
                    title = "Contributori della community",
                    subtitle = stringResource(Res.string.ui_credits_no_other_contributors),
                    icon = LiquidIcons.AccountCircle,
                )
            }
        }
    }
}
