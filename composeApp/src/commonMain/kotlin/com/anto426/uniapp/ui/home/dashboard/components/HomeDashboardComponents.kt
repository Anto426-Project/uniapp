package com.anto426.uniapp.ui.home.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.anto426.uniapp.ui.components.layout.UniContentHost
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.emptystate.LiquidEmptyState
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.home.presentation.HomeDashboardUiState
import com.anto426.uniapp.model.home.QuickActionItem
import com.anto426.uniapp.model.news.NewsItem
import com.anto426.uniapp.ui.components.account.UniAccountAvatar
import com.anto426.uniapp.ui.components.account.accountDisplayIdentity
import com.anto426.uniapp.ui.components.display.UniCircularProgressRing
import com.anto426.uniapp.ui.components.items.UniNewsCard
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun HomeErrorRetryCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors

    LiquidCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedRectangle(20.dp),
        contentPadding = 16.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = LiquidIcons.Warning,
                contentDescription = null,
                tint = glassColors.warning,
                modifier = Modifier.liquidIconContainer(
                    containerSize = 36.dp,
                    iconSize = 18.dp,
                    containerColor = glassColors.warning.copy(alpha = 0.14f),
                    shape = RoundedRectangle(10.dp),
                ),
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            LiquidButton(
                text = stringResource(Res.string.ui_retry),
                onClick = onRetry,
                variant = LiquidButtonVariant.Tonal,
                size = LiquidButtonSize.Small,
            )
        }
    }
}

/**
 * Sezione 1 — Card unificata Profilo + Riepilogo Carriera (stile Apple Fitness / Salute,
 * senza aloni o box interni superflui e con nome/cognome integrati nella card).
 */
@Composable
fun HomeSummarySection(
    uiState: HomeDashboardUiState,
    onOpenBadge: () -> Unit,
    onOpenStatistics: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val identity = accountDisplayIdentity(
        uiState.profileName,
        uiState.profileInitials,
        uiState.profilePhotoData,
    )
    val rawDisplayName = identity.name.ifBlank {
        stringResource(
            if (uiState.isProfessor) Res.string.ui_professor_role
            else Res.string.ui_student_name_fallback,
        )
    }
    val displayName = remember(rawDisplayName) { formatSmartDisplayName(rawDisplayName) }
    val secondaryLine = if (uiState.isProfessor) {
        uiState.departmentName.ifBlank { stringResource(Res.string.ui_professor_role) }
    } else {
        if (uiState.matricola.isNotBlank()) {
            stringResource(Res.string.ui_matricola_prefix, uiState.matricola)
        } else {
            stringResource(Res.string.ui_student_status)
        }
    }

    LiquidCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedRectangle(24.dp),
        contentPadding = 0.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Identità cliccabile per aprire il Badge (niente bottone Badge separato)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .clickable(role = Role.Button, onClick = onOpenBadge)
                    .semantics(mergeDescendants = true) {}
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    UniAccountAvatar(
                        imageData = identity.photo,
                        initials = identity.initials.ifBlank { if (uiState.isProfessor) "DO" else "ST" },
                        size = 44.dp,
                        contentDescription = stringResource(Res.string.ui_profile_picture),
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(
                                width = 1.5.dp,
                                color = colorScheme.primary.copy(alpha = 0.40f),
                                shape = CircleShape,
                            )
                            .padding(2.dp),
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = secondaryLine,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = LiquidIcons.QrCode,
                        contentDescription = stringResource(Res.string.ui_badge),
                        tint = colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Icon(
                        imageVector = LiquidIcons.ChevronRight,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            LiquidHorizontalDivider(
                modifier = Modifier.padding(horizontal = 18.dp),
                color = colorScheme.onSurface.copy(alpha = 0.08f),
            )

            if (uiState.isProfessor) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClick = onOpenBadge)
                        .semantics(mergeDescendants = true) {}
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = uiState.departmentName.ifBlank { stringResource(Res.string.ui_university) },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(8.dp))
                        LiquidBadge(
                            text = stringResource(Res.string.ui_professor_area_title),
                            containerColor = colorScheme.primaryContainer.copy(alpha = 0.55f),
                            contentColor = colorScheme.primary,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SummaryInlineStat(
                            value = "${uiState.teachingCount}",
                            label = stringResource(Res.string.ui_professor_teachings),
                            accentColor = colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(colorScheme.onSurface.copy(alpha = 0.10f)),
                        )
                        SummaryInlineStat(
                            value = "${uiState.openExamRounds}",
                            label = stringResource(Res.string.ui_exams),
                            accentColor = colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(colorScheme.onSurface.copy(alpha = 0.10f)),
                        )
                        SummaryInlineStat(
                            value = "${uiState.thesisCount}",
                            label = stringResource(Res.string.ui_professor_theses),
                            accentColor = colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClick = onOpenStatistics)
                        .semantics(mergeDescendants = true) {}
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = uiState.degreeName.ifBlank {
                                    uiState.departmentName.ifBlank { stringResource(Res.string.ui_home_career_eyebrow) }
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            if (uiState.completedExams > 0) {
                                Text(
                                    text = "• ${stringResource(Res.string.ui_didactics_exams_badge, uiState.completedExams).lowercase()}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = uiState.average,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1.0).sp,
                                color = colorScheme.onSurface,
                                maxLines = 1,
                            )
                            Text(
                                text = stringResource(Res.string.ui_home_average_label).lowercase(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 5.dp),
                            )
                        }

                        Text(
                            text = "${stringResource(Res.string.ui_graduation_base)} ${uiState.degreeBase}/110",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            UniCircularProgressRing(
                                progress = uiState.progress,
                                centerText = "${(uiState.progress * 100).toInt()}%",
                                subtitleText = "CFU",
                                size = 68.dp,
                                strokeWidth = 6.5.dp,
                                primaryColor = colorScheme.primary,
                                secondaryColor = colorScheme.tertiary,
                            )
                            Icon(
                                imageVector = LiquidIcons.ChevronRight,
                                contentDescription = stringResource(Res.string.ui_details),
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        Text(
                            text = "${uiState.acquiredCfu}/${uiState.targetCfu.takeIf { it > 0 } ?: "—"} CFU",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier.padding(end = 22.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryInlineStat(
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = accentColor,
            maxLines = 1,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Sezione 2 — Due widget compatti stile Apple Casa senza IntrinsicSize costosi.
 */
@Composable
fun HomeSpotlightRow(
    uiState: HomeDashboardUiState,
    onOpenExams: () -> Unit,
    onOpenTaxes: () -> Unit,
    onOpenTheses: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val hasDueTaxes = !uiState.isProfessor && (
        uiState.nextTaxLabel.isNotBlank() ||
            uiState.dueAmount.any { it in '1'..'9' }
        )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(clip = false),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppleHomeWidgetTile(
            title = stringResource(Res.string.ui_exams),
            value = "${uiState.openExamRounds}",
            subtitle = if (uiState.isProfessor) {
                stringResource(Res.string.ui_professor_rounds_filtered)
            } else {
                uiState.nextExamLabel.ifBlank { stringResource(Res.string.msg_nessun_appello_disponibile) }
            },
            icon = LiquidIcons.Calendar,
            accentColor = colorScheme.primary,
            onClick = onOpenExams,
            modifier = Modifier.weight(1f),
        )

        AppleHomeWidgetTile(
            title = stringResource(
                if (uiState.isProfessor) Res.string.ui_professor_theses
                else Res.string.ui_taxes,
            ),
            value = if (uiState.isProfessor) "${uiState.thesisCount}" else uiState.dueAmount,
            subtitle = if (uiState.isProfessor) {
                stringResource(Res.string.ui_professor_theses_subtitle)
            } else {
                uiState.nextTaxLabel.ifBlank { stringResource(Res.string.ui_home_no_due_taxes) }
            },
            icon = when {
                uiState.isProfessor -> LiquidIcons.Assignment
                hasDueTaxes -> LiquidIcons.Warning
                else -> LiquidIcons.CreditCard
            },
            accentColor = if (hasDueTaxes) glassColors.warning else glassColors.success,
            valueColor = if (hasDueTaxes) glassColors.warning else colorScheme.onSurface,
            onClick = if (uiState.isProfessor) onOpenTheses else onOpenTaxes,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AppleHomeWidgetTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    LiquidCard(
        modifier = modifier
            .graphicsLayer(clip = false)
            .semantics(mergeDescendants = true) {},
        shape = RoundedRectangle(20.dp),
        contentPadding = 14.dp,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 96.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = 28.dp,
                            iconSize = 15.dp,
                            containerColor = accentColor.copy(alpha = 0.14f),
                            shape = RoundedRectangle(8.dp),
                        ),
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    imageVector = LiquidIcons.ChevronRight,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }

            Spacer(Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = value,
                    style = if (value.length > 6) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = valueColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Sezione 3 — "Notizie >" in stile Apple News / App Store:
 * Target touch >= 48dp sull'intestazione cliccabile, contatore discreto (niente stati nascosti),
 * e carosello edge-to-edge con peek naturale.
 */
@Composable
fun HomeNewsSection(
    homeNews: List<NewsItem>,
    activeNewsIndex: Int,
    onOpenNews: () -> Unit,
    onShowNews: (NewsItem) -> Unit,
    onSelectNews: (Int) -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val safeActiveIndex = activeNewsIndex.coerceIn(0, homeNews.lastIndex.coerceAtLeast(0))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(clip = false),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = onOpenNews,
                )
                .semantics(mergeDescendants = true) {}
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(Res.string.nav_route_news_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )
                Icon(
                    imageVector = LiquidIcons.ChevronRight,
                    contentDescription = stringResource(Res.string.ui_home_news_all),
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }

            if (homeNews.size > 1) {
                Text(
                    text = "${safeActiveIndex + 1} / ${homeNews.size}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }

        if (homeNews.isEmpty()) {
            LiquidEmptyState(
                title = stringResource(Res.string.ui_news_empty_title),
                description = stringResource(Res.string.ui_news_empty_desc),
                modifier = Modifier.padding(horizontal = 18.dp),
            )
        } else {
            val pagerState = rememberPagerState(
                initialPage = safeActiveIndex,
                pageCount = { homeNews.size },
            )

            LaunchedEffect(safeActiveIndex, homeNews.size) {
                if (pagerState.currentPage != safeActiveIndex && !pagerState.isScrollInProgress) {
                    pagerState.animateScrollToPage(safeActiveIndex)
                }
            }

            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.settledPage }
                    .distinctUntilChanged()
                    .collect { page ->
                        if (page != safeActiveIndex) {
                            onSelectNews(page)
                        }
                    }
            }

            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(
                    start = 18.dp,
                    end = if (homeNews.size > 1) 44.dp else 18.dp,
                ),
                pageSpacing = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(clip = false),
            ) { page ->
                val newsItem = homeNews[page]
                UniNewsCard(
                    news = newsItem,
                    onClick = { onShowNews(newsItem) },
                    homeCard = true,
                )
            }
        }
    }
}

/**
 * Sezione "Accesso rapido" con griglia Bento compatta a 2 colonne e modifica inline in-place.
 */
@Composable
fun HomeQuickAccessGroup(
    uiState: HomeDashboardUiState,
    onToggleCustomization: () -> Unit,
    onFinishCustomization: () -> Unit,
    onToggleQuickAction: (String) -> Unit,
    onQuickActionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val visibleActions = uiState.visibleQuickActions
    val allActions = uiState.quickActions
    val isCustomizing = uiState.isCustomizing
    val selectedCount = uiState.selectedActionIds.size
    val displayActions = if (isCustomizing) allActions else visibleActions

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(clip = false),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(Res.string.ui_home_quick_access_eyebrow),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )
                if (isCustomizing) {
                    Text(
                        text = stringResource(Res.string.ui_home_customize_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isCustomizing) {
                    LiquidBadge(
                        text = "$selectedCount/${allActions.size}",
                        containerColor = colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        contentColor = colorScheme.onSurfaceVariant,
                    )
                }

                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .clip(Capsule())
                        .clickable(
                            role = Role.Button,
                            onClick = if (isCustomizing) onFinishCustomization else onToggleCustomization,
                        ),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    if (isCustomizing) {
                        Row(
                            modifier = Modifier
                                .clip(Capsule())
                                .background(colorScheme.primary)
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = LiquidIcons.Check,
                                contentDescription = null,
                                tint = colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = stringResource(Res.string.ui_done),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onPrimary,
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(Res.string.ui_customize),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                    }
                }
            }
        }

        if (displayActions.isNotEmpty()) {
            UniContentHost(
                targetState = isCustomizing,
                contentAlignment = Alignment.TopStart,
                label = "QuickAccessModeTransition",
            ) { editing ->
                val items = if (editing) allActions else visibleActions
                val chunkedActions = items.chunked(2)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(clip = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    chunkedActions.forEachIndexed { rowIndex, rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer(clip = false),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            rowItems.forEachIndexed { colIndex, action ->
                                val itemIndex = rowIndex * 2 + colIndex
                                val iconAccent = when (itemIndex % 3) {
                                    0 -> colorScheme.primary
                                    1 -> colorScheme.tertiary
                                    else -> colorScheme.secondary
                                }
                                val isSelected = action.id in uiState.selectedActionIds
                                val isLockedMin = editing && isSelected && selectedCount <= 2
                                QuickAccessBentoTile(
                                    action = action,
                                    accent = iconAccent,
                                    editing = editing,
                                    selected = isSelected,
                                    lockedMin = isLockedMin,
                                    onClick = {
                                        if (editing) {
                                            onToggleQuickAction(action.id)
                                        } else {
                                            onQuickActionClick(action.id)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAccessBentoTile(
    action: QuickActionItem,
    accent: Color,
    editing: Boolean,
    selected: Boolean,
    lockedMin: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val tileShape = RoundedRectangle(18.dp)
    val isActive = !editing || selected
    val tileModifier = modifier
        .semantics(mergeDescendants = true) {
            if (editing) this.selected = selected
        }
        .let { base ->
            when {
                editing && selected -> base.border(1.2.dp, colorScheme.primary.copy(alpha = 0.6f), tileShape)
                editing && !selected -> base.border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.45f), tileShape)
                else -> base
            }
        }
        .graphicsLayer(
            clip = false,
            alpha = if (isActive) 1f else 0.62f,
        )

    LiquidCard(
        modifier = tileModifier,
        shape = tileShape,
        contentPadding = 11.dp,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 40.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            val resolvedAccent = if (isActive) accent else colorScheme.onSurfaceVariant
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                tint = resolvedAccent,
                modifier = Modifier.liquidIconContainer(
                    containerSize = 32.dp,
                    iconSize = 17.dp,
                    containerColor = resolvedAccent.copy(alpha = if (isActive) 0.14f else 0.10f),
                    shape = RoundedRectangle(10.dp),
                ),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitle = action.subtitle
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (editing) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .then(
                            if (selected) {
                                Modifier.background(
                                    colorScheme.primary.copy(alpha = if (lockedMin) 0.65f else 1f),
                                )
                            } else {
                                Modifier.border(
                                    width = 1.25.dp,
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                    shape = CircleShape,
                                )
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (selected) LiquidIcons.Check else LiquidIcons.Add,
                        contentDescription = null,
                        tint = if (selected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp),
                    )
                }
            }
        }
    }
}

private fun formatSmartDisplayName(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return trimmed
    val letters = trimmed.filter { it.isLetter() }
    if (letters.isEmpty() || letters.any { it.isLowerCase() }) return trimmed
    return trimmed.split(Regex("\\s+")).joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { ch ->
            if (ch.isLowerCase()) ch.titlecase() else ch.toString()
        }
    }
}
