package com.anto426.uniapp.ui.home.dashboard.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.kyant.shapes.RoundedRectangle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.cards.card.LiquidCardDefaults
import com.anto426.uniapp.ui.components.items.UniNewsCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.feedback.progressbar.LiquidLinearProgressIndicator

import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.data.UniAppInitialData
import com.anto426.uniapp.home.presentation.HomeDashboardUiState
import com.anto426.uniapp.model.news.NewsItem
import com.anto426.uniapp.ui.components.account.UniAccountAvatar
import com.anto426.uniapp.ui.components.cards.UniHeroFluidBackground
import com.anto426.uniapp.ui.components.cards.UniHeroGlassLenses
import com.anto426.uniapp.ui.components.cards.rememberUniHeroCardPalette
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import kotlin.math.abs

@Composable
fun HomeAcademicProfileHeroCard(
    uiState: HomeDashboardUiState,
    onOpenBadge: () -> Unit,
    onOpenStatistics: () -> Unit,
) {
    val identity = com.anto426.uniapp.ui.components.account.accountDisplayIdentity(
        uiState.profileName, uiState.profileInitials, uiState.profilePhotoData,
    )
    val colorScheme = MaterialTheme.colorScheme

    LiquidCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedRectangle(26.dp),
        contentPadding = 20.dp,
        onClick = if (uiState.isProfessor) onOpenBadge else onOpenStatistics,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Profilo Header (Avatar + Nome/Matricola + Pulsante Badge Tonal)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                UniAccountAvatar(
                    imageData = identity.photo,
                    initials = identity.initials.ifBlank { if (uiState.isProfessor) "DO" else "ST" },
                    size = 46.dp,
                    contentDescription = stringResource(Res.string.ui_profile_picture),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = identity.name.ifBlank {
                            stringResource(
                                if (uiState.isProfessor) Res.string.ui_professor_role
                                else Res.string.ui_student_name_fallback,
                            )
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                    )

                    Text(
                        text = if (uiState.isProfessor) {
                            stringResource(Res.string.ui_professor_role)
                        } else if (uiState.matricola.isNotBlank()) {
                            stringResource(Res.string.ui_matricola_prefix, uiState.matricola)
                        } else {
                            stringResource(Res.string.ui_student_status)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.primary,
                        maxLines = 1,
                    )
                }

                // Pulsante Badge pulito
                LiquidButton(
                    text = stringResource(Res.string.ui_badge),
                    onClick = onOpenBadge,
                    variant = LiquidButtonVariant.Tonal,
                    size = LiquidButtonSize.Small,
                )
            }

            // Separatore orizzontale pulito
            LiquidHorizontalDivider(
                color = colorScheme.onSurface.copy(alpha = 0.08f),
            )

            if (uiState.isProfessor) {
                ProfessorHomeIdentitySummary(uiState)
            } else {
                // 2. Corso di Laurea e CFU
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = uiState.degreeName.ifBlank {
                            uiState.departmentName.ifBlank { stringResource(Res.string.ui_degree_label) }
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    LiquidBadge(
                        text = "${uiState.acquiredCfu} / ${uiState.targetCfu.takeIf { it > 0 } ?: "—"} CFU",
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                        contentColor = colorScheme.primary,
                    )
                }

                // 3. Statistiche Chiave Pulite (Base Laurea & Media)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = stringResource(Res.string.ui_graduation_base).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                            letterSpacing = 0.6.sp,
                            fontSize = 10.sp,
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = uiState.degreeBase,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = colorScheme.onSurface,
                            )
                            Text(
                                text = " / 110",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 3.dp),
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = uiState.average,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.primary,
                        )
                        Text(
                            text = stringResource(Res.string.ui_home_average_label).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurfaceVariant,
                            letterSpacing = 0.6.sp,
                            fontSize = 10.sp,
                        )
                    }
                }

                // 4. Progresso e Link Dettagli
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LiquidLinearProgressIndicator(
                        progress = uiState.progress,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(
                                Res.string.ui_career_completion,
                                (uiState.progress * 100).toInt(),
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.ui_details),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.primary,
                            )
                            Icon(
                                imageVector = LiquidIcons.ChevronRight,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeQuickIndicatorsRow(
    uiState: HomeDashboardUiState,
    onOpenExams: () -> Unit,
    onOpenTaxes: () -> Unit,
    onOpenTheses: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Card Appelli
        LiquidCard(
            modifier = Modifier.weight(1f).graphicsLayer(clip = false),
            shape = RoundedRectangle(22.dp),
            contentPadding = 16.dp,
            onClick = onOpenExams,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LiquidBadge(
                        text = "${uiState.openExamRounds}",
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                        contentColor = colorScheme.primary,
                    )
                    Icon(
                        imageVector = LiquidIcons.Calendar,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = 40.dp,
                            iconSize = 20.dp,
                            containerColor = colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedRectangle(12.dp),
                        ),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(Res.string.ui_exams),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (uiState.isProfessor) {
                            stringResource(Res.string.ui_professor_rounds_filtered)
                        } else uiState.nextExamLabel.ifBlank { stringResource(Res.string.msg_nessun_appello_disponibile) },
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        // Secondo indicatore: prenotazioni docente oppure tasse studente.
        LiquidCard(
            modifier = Modifier.weight(1f).graphicsLayer(clip = false),
            shape = RoundedRectangle(22.dp),
            contentPadding = 16.dp,
            onClick = if (uiState.isProfessor) onOpenTheses else onOpenTaxes,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LiquidBadge(
                        text = if (uiState.isProfessor) "${uiState.thesisCount}" else uiState.dueAmount,
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                        contentColor = colorScheme.primary,
                    )
                    Icon(
                        imageVector = if (uiState.isProfessor) LiquidIcons.Assignment else LiquidIcons.CreditCard,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = 40.dp,
                            iconSize = 20.dp,
                            containerColor = colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedRectangle(12.dp),
                        ),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(
                            if (uiState.isProfessor) Res.string.ui_professor_theses
                            else Res.string.ui_taxes,
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (uiState.isProfessor) {
                            stringResource(Res.string.ui_professor_theses_subtitle)
                        } else uiState.nextTaxLabel.ifBlank { stringResource(Res.string.ui_home_no_due_taxes) },
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfessorHomeIdentitySummary(
    uiState: HomeDashboardUiState,
) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = uiState.departmentName.ifBlank { stringResource(Res.string.ui_university) },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = colors.onSurface,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LiquidBadge(
                text = stringResource(Res.string.ui_professor_courses_count, uiState.teachingCount),
                containerColor = colors.primaryContainer.copy(alpha = .5f),
                contentColor = colors.primary,
                modifier = Modifier.weight(1f),
            )
            LiquidBadge(
                text = stringResource(Res.string.ui_professor_rounds_count, uiState.openExamRounds),
                containerColor = colors.primaryContainer.copy(alpha = .5f),
                contentColor = colors.primary,
                modifier = Modifier.weight(1f),
            )
            LiquidBadge(
                text = stringResource(Res.string.ui_professor_theses_count, uiState.thesisCount),
                containerColor = colors.primaryContainer.copy(alpha = .5f),
                contentColor = colors.primary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}


@Composable
fun HomeNewsSection(
    homeNews: List<NewsItem>,
    activeNewsIndex: Int,
    onOpenNews: () -> Unit,
    onShowNews: (NewsItem) -> Unit,
    onNextNews: () -> Unit,
    onPreviousNews: () -> Unit,
) {
    val safeActiveIndex = activeNewsIndex.coerceIn(0, homeNews.lastIndex.coerceAtLeast(0))
    val currentNews = homeNews.getOrNull(safeActiveIndex)
    val nextNews by rememberUpdatedState(onNextNews)
    val previousNews by rememberUpdatedState(onPreviousNews)
    val thresholdPx = with(LocalDensity.current) { 48.dp.toPx() }
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr

    Column(
        modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LiquidSectionHeader(
            title = stringResource(Res.string.nav_route_news_title),
            subtitle = if (currentNews != null) {
                stringResource(Res.string.ui_home_news_page_notice, safeActiveIndex + 1, homeNews.size)
            } else {
                stringResource(Res.string.nav_route_news_subtitle)
            },
            trailingContent = {
                LiquidButton(
                    text = stringResource(Res.string.ui_home_news_all),
                    onClick = onOpenNews,
                    variant = LiquidButtonVariant.Text,
                    size = LiquidButtonSize.Small,
                )
            },
        )

        if (currentNews == null) {
            com.anto426.liquidmonet.components.display.emptystate.LiquidEmptyState(
                title = stringResource(Res.string.ui_news_empty_title),
                description = stringResource(Res.string.ui_news_empty_desc),
            )
        } else {
            val newsItem = homeNews.getOrNull(safeActiveIndex) ?: currentNews
            UniNewsCard(
                news = newsItem,
                onClick = { onShowNews(newsItem) },
                modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false)
                    .pointerInput(thresholdPx, isLtr) {
                        var dragDistance = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { dragDistance = 0f },
                            onDragCancel = { dragDistance = 0f },
                            onDragEnd = {
                                if (abs(dragDistance) >= thresholdPx) {
                                    val forward = if (isLtr) dragDistance < 0f else dragDistance > 0f
                                    if (forward) nextNews() else previousNews()
                                }
                                dragDistance = 0f
                            },
                            onHorizontalDrag = { change, amount ->
                                change.consume()
                                dragDistance += amount
                            },
                        )
                    },
                homeCard = true,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeQuickAccessSection(
    uiState: HomeDashboardUiState,
    onToggleCustomization: () -> Unit,
    onFinishCustomization: () -> Unit,
    onToggleQuickAction: (String) -> Unit,
    onQuickActionClick: (String) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val allActions = uiState.quickActions

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(clip = false),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_home_quick_access_eyebrow),
            subtitle = stringResource(Res.string.ui_home_quick_access_sub),
            trailingContent = {
                LiquidButton(
                    onClick = onToggleCustomization,
                    variant = if (uiState.isCustomizing) LiquidButtonVariant.Tonal else LiquidButtonVariant.Glass,
                    size = LiquidButtonSize.Small,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = if (uiState.isCustomizing) LiquidIcons.Check else LiquidIcons.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = if (uiState.isCustomizing) stringResource(Res.string.ui_done) else stringResource(Res.string.ui_customize),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            },
        )

        if (uiState.isCustomizing) {
                LiquidCard(
                    modifier = Modifier.graphicsLayer(clip = false),
                    shape = RoundedRectangle(24.dp),
                    contentPadding = 20.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer(clip = false),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.ui_home_customize_title),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            color = colorScheme.primary,
                        )

                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer(clip = false),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            allActions.forEach { action ->
                                val isSelected = action.id in uiState.selectedActionIds
                                LiquidChip(
                                    label = action.title,
                                    selected = isSelected,
                                    onClick = { onToggleQuickAction(action.id) },
                                    leadingIcon = action.icon,
                                    trailingIcon = if (isSelected) LiquidIcons.Check else null,
                                )
                            }
                        }

                        LiquidButton(
                            text = stringResource(Res.string.ui_save),
                            onClick = onFinishCustomization,
                            modifier = Modifier.fillMaxWidth(),
                            variant = LiquidButtonVariant.Primary,
                        )
                    }
                }
            } else {
                val currentSelectedActions = uiState.visibleQuickActions
                val chunkedActions = currentSelectedActions.chunked(2)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(clip = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    chunkedActions.forEach { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer(clip = false),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            rowItems.forEach { action ->
                                LiquidCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .graphicsLayer(clip = false),
                                    shape = RoundedRectangle(20.dp),
                                    contentPadding = 16.dp,
                                    onClick = { onQuickActionClick(action.id) },
                                ) {
                                    Row(
                                        modifier = Modifier.graphicsLayer(clip = false),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Icon(
                                            imageVector = action.icon,
                                            contentDescription = null,
                                            tint = colorScheme.primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                        Text(
                                            text = action.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onSurface,
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                            if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                        }
                }
            }
        }
    }
}
