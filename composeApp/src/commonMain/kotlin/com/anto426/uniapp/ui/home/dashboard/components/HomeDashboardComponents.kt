package com.anto426.uniapp.ui.home.dashboard.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidStatusCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidIconBox
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.liquidmonet.components.feedback.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.components.layout.LiquidAnimatedSwitcher
import com.anto426.liquidmonet.components.layout.LiquidSwitcherTransition
import com.anto426.liquidmonet.components.selection.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.home.presentation.HomeDashboardUiState
import com.anto426.uniapp.model.news.NewsItem
import com.anto426.uniapp.ui.components.account.UniAccountAvatar
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider

@Composable
fun HomeAcademicProfileHeroCard(
    uiState: HomeDashboardUiState,
    backdropState: Backdrop,
    onOpenBadge: () -> Unit,
    onOpenStatistics: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    LiquidCard(
        backdropState = backdropState,
        shape = RoundedCornerShape(26.dp),
        contentPadding = 18.dp,
        interactiveGelatin = true,
        onClick = if (uiState.isProfessor) onOpenBadge else onOpenStatistics,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 1. Header: Profilo Studente + Pulsante Tessera ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                UniAccountAvatar(
                    imageData = uiState.profilePhotoData,
                    initials = uiState.profileInitials.ifBlank { "ST" },
                    size = 48.dp,
                    contentDescription = "Foto profilo",
                    backdropState = backdropState,
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = uiState.profileName.ifBlank {
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = if (uiState.isProfessor) {
                                stringResource(Res.string.ui_professor_role)
                            } else if (uiState.matricola.isNotBlank()) {
                                stringResource(Res.string.ui_matricola_prefix, uiState.matricola)
                            } else {
                                stringResource(Res.string.ui_student_status)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.primary,
                        )
                        val secondaryProfileLabel =
                            if (uiState.isProfessor) uiState.matricola else uiState.academicYear
                        if (secondaryProfileLabel.isNotBlank()) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            )
                            Text(
                                text = secondaryProfileLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                }

                // Interactive Badge Button
                LiquidButton(
                    text = stringResource(Res.string.ui_badge),
                    onClick = onOpenBadge,
                    variant = LiquidButtonVariant.Tonal,
                    size = LiquidButtonSize.Small,
                    backdropState = backdropState,
                )
            }

            // Divider orizzontale soft
            LiquidHorizontalDivider(
                color = colorScheme.onSurface.copy(alpha = 0.08f),
            )

            if (uiState.isProfessor) {
                ProfessorHomeIdentitySummary(uiState, backdropState)
                return@Column
            }

            // 2. Carriera: Corso di Laurea e Badge CFU
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.degreeName.ifBlank { uiState.departmentName.ifBlank { stringResource(Res.string.ui_degree_label) } },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                    )
                }

                LiquidBadge(
                    text = "${uiState.acquiredCfu} / ${uiState.targetCfu.takeIf { it > 0 } ?: "—"} CFU",
                    containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                    contentColor = colorScheme.primary,
                    backdropState = backdropState,
                )
            }

            // 3. Statistiche Chiave: Proiezione Laurea e Media Esami
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
                        letterSpacing = 0.5.sp,
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = uiState.degreeBase,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onSurface,
                        )
                        Text(
                            text = " / 110",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = uiState.average,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.primary,
                    )
                    Text(
                        text = stringResource(Res.string.ui_home_average_label),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }

            // 4. Barra Progresso Carriera e Dettagli
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LiquidLinearProgressIndicator(
                    progress = uiState.progress,
                    backdropState = backdropState,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.ui_career_completion, (uiState.progress * 100).toInt()),
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

@Composable
fun HomeQuickIndicatorsRow(
    uiState: HomeDashboardUiState,
    backdropState: Backdrop,
    onOpenExams: () -> Unit,
    onOpenTaxes: () -> Unit,
    onOpenTheses: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Card Appelli
        LiquidCard(
            modifier = Modifier.weight(1f),
            backdropState = backdropState,
            shape = RoundedCornerShape(22.dp),
            contentPadding = 16.dp,
            onClick = onOpenExams,
            interactiveGelatin = true,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LiquidIconBox(
                        icon = LiquidIcons.Calendar,
                        size = 40.dp,
                        iconSize = 20.dp,
                        containerColor = colorScheme.primary.copy(alpha = 0.12f),
                        iconTint = colorScheme.primary,
                        shape = RoundedCornerShape(12.dp),
                    )
                    LiquidBadge(
                        text = uiState.openExamRounds.toString(),
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                        contentColor = colorScheme.primary,
                        backdropState = backdropState,
                    )
                }
                Column {
                    Text(
                        text = stringResource(Res.string.ui_exams),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = if (uiState.isProfessor) {
                            stringResource(Res.string.ui_professor_rounds_filtered)
                        } else uiState.nextExamLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }

        // Secondo indicatore: prenotazioni docente oppure tasse studente.
        LiquidCard(
            modifier = Modifier.weight(1f),
            backdropState = backdropState,
            shape = RoundedCornerShape(22.dp),
            contentPadding = 16.dp,
            onClick = if (uiState.isProfessor) onOpenTheses else onOpenTaxes,
            interactiveGelatin = true,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LiquidIconBox(
                        icon = if (uiState.isProfessor) LiquidIcons.Assignment else LiquidIcons.CreditCard,
                        size = 40.dp,
                        iconSize = 20.dp,
                        containerColor = colorScheme.primary.copy(alpha = 0.12f),
                        iconTint = colorScheme.primary,
                        shape = RoundedCornerShape(12.dp),
                    )
                    LiquidBadge(
                        text = if (uiState.isProfessor) uiState.thesisCount.toString() else uiState.dueAmount,
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                        contentColor = colorScheme.primary,
                        backdropState = backdropState,
                    )
                }
                Column {
                    Text(
                        text = stringResource(
                            if (uiState.isProfessor) Res.string.ui_professor_theses
                            else Res.string.ui_taxes,
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = if (uiState.isProfessor) {
                            stringResource(Res.string.ui_professor_theses_subtitle)
                        } else uiState.nextTaxLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfessorHomeIdentitySummary(
    uiState: HomeDashboardUiState,
    backdropState: Backdrop,
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
                backdropState = backdropState,
            )
            LiquidBadge(
                text = stringResource(Res.string.ui_professor_rounds_count, uiState.openExamRounds),
                containerColor = colors.primaryContainer.copy(alpha = .5f),
                contentColor = colors.primary,
                backdropState = backdropState,
            )
        }
    }
}


@Composable
fun HomeNewsSection(
    homeNews: List<NewsItem>,
    activeNewsIndex: Int,
    backdropState: Backdrop,
    onOpenNews: () -> Unit,
    onShowNews: (NewsItem) -> Unit,
    onNextNews: () -> Unit,
    onPreviousNews: () -> Unit,
) {
    if (homeNews.isEmpty()) return

    val safeActiveIndex = activeNewsIndex.coerceIn(0, homeNews.lastIndex)
    val currentNews = homeNews[safeActiveIndex]

    Column(
        modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_home_news_eyebrow),
            subtitle = stringResource(Res.string.ui_home_news_page_notice, safeActiveIndex + 1, homeNews.size),
            trailingContent = {
                LiquidButton(
                    text = stringResource(Res.string.ui_home_news_all),
                    onClick = onOpenNews,
                    variant = LiquidButtonVariant.Text,
                    size = LiquidButtonSize.Small,
                    backdropState = backdropState,
                )
            },
        )

        LiquidAnimatedSwitcher(
            targetState = safeActiveIndex,
            transition = LiquidSwitcherTransition.LiquidMorph,
            onSwipeForward = onNextNews,
            onSwipeBackward = onPreviousNews,
            modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
            label = "homeNewsSwitcher",
        ) { index ->
            val newsItem = homeNews.getOrNull(index) ?: currentNews
            LiquidStatusCard(
                modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
                title = newsItem.title,
                description = newsItem.description,
                statusType = newsItem.type,
                backdropState = backdropState,
                titleMaxLines = 1,
                descriptionMaxLines = 2,
                onClick = { onShowNews(newsItem) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeQuickAccessSection(
    uiState: HomeDashboardUiState,
    backdropState: Backdrop,
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
                    backdropState = backdropState,
                ) {
                    AnimatedContent(
                        targetState = uiState.isCustomizing,
                        transitionSpec = {
                            (fadeIn() + scaleIn(initialScale = 0.85f)) togetherWith
                            (fadeOut() + scaleOut(targetScale = 0.85f))
                        },
                        label = "customizeBtn",
                    ) { customizing ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = if (customizing) LiquidIcons.Check else LiquidIcons.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = if (customizing) stringResource(Res.string.ui_done) else stringResource(Res.string.ui_customize),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            },
        )

        AnimatedContent(
            targetState = uiState.isCustomizing,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.95f))
                    .togetherWith(fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.95f))
                    .using(SizeTransform(clip = false))
            },
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(clip = false),
            label = "quickAccessContent",
        ) { isCustomizing ->
            if (isCustomizing) {
                LiquidCard(
                    modifier = Modifier.graphicsLayer(clip = false),
                    backdropState = backdropState,
                    shape = RoundedCornerShape(24.dp),
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
                                    backdropState = backdropState,
                                )
                            }
                        }

                        LiquidButton(
                            text = stringResource(Res.string.ui_save),
                            onClick = onFinishCustomization,
                            modifier = Modifier.fillMaxWidth(),
                            variant = LiquidButtonVariant.Primary,
                            backdropState = backdropState,
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
                                    backdropState = backdropState,
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = 16.dp,
                                    onClick = { onQuickActionClick(action.id) },
                                    interactiveGelatin = true,
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
}
