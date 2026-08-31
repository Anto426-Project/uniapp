package com.anto426.uniapp.ui.home.dashboard


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidStatusCard
import com.anto426.liquidmonet.components.cards.LiquidStatusType
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.layout.LiquidAnimatedSwitcher
import com.anto426.liquidmonet.components.layout.LiquidSwitcherTransition
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.liquidmonet.components.feedback.LiquidCircularProgressIndicator
import com.anto426.liquidmonet.components.feedback.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.components.feedback.LiquidSheet
import com.anto426.liquidmonet.components.selection.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.home.presentation.HomeDashboardUiState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn

import com.kyant.backdrop.Backdrop


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    backdropState: Backdrop,
    uiState: HomeDashboardUiState,
    onOpenStatistics: () -> Unit = {},
    onOpenTaxes: () -> Unit = {},
    onOpenExams: () -> Unit = {},
    onOpenNews: () -> Unit = {},
    onShowNews: (com.anto426.uniapp.model.news.NewsItem) -> Unit,
    onDismissNews: () -> Unit,
    onNextNews: () -> Unit,
    onPreviousNews: () -> Unit,
    onToggleCustomization: () -> Unit,
    onFinishCustomization: () -> Unit,
    onToggleQuickAction: (String) -> Unit,
    onQuickActionClick: (String) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val homeNews = uiState.news
    val allActions = uiState.quickActions

    UniScreenColumn {
        // 1. Hero Card Carriera (Scenografica, a tema Liquid Monet & Ricca di Informazioni)
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(26.dp),
            contentPadding = 18.dp,
            onClick = onOpenStatistics,
            interactiveGelatin = true
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header con Tile Icona Vetro & Badge Monet
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(colorScheme.primaryContainer.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LiquidIcons.Star,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = "Ingegneria Informatica",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "Laurea Triennale • 3° Anno",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    LiquidBadge(
                        text = "120 / 180 CFU",
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.primary,
                        backdropState = backdropState
                    )
                }

                // Sezione Scenografica con Proiezione Laurea & Pill Media
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "PROIEZIONE LAUREA",
                            color = colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "103.4",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = colorScheme.onSurface,
                                letterSpacing = (-1).sp,
                                lineHeight = 32.sp
                            )
                            Text(
                                text = " / 110",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                        Text(
                            text = "Base stimata su 14 esami",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(colorScheme.primaryContainer.copy(alpha = 0.35f))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "28.2",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = colorScheme.primary
                            )
                            Text(
                                text = "Media • 67%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Barra fluida Liquid Monet
                LiquidLinearProgressIndicator(
                    progress = 0.67f,
                    backdropState = backdropState
                )

                // Footer Scenografico con Prossimo Appello
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = LiquidIcons.Calendar,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Prossimo appello: Ing. Software (15 Set)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = LiquidIcons.ArrowForward,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // 2. Indicatori Rapidi (Morbidi, dettagliati e informativi)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(clip = false),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Card Appelli
            LiquidCard(
                modifier = Modifier.weight(1f),
                backdropState = backdropState,
                shape = RoundedCornerShape(22.dp),
                contentPadding = 14.dp,
                onClick = onOpenExams,
                interactiveGelatin = true
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(colorScheme.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LiquidIcons.Calendar,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        LiquidBadge(
                            text = "2 Aperti",
                            containerColor = colorScheme.primaryContainer,
                            contentColor = colorScheme.primary,
                            backdropState = backdropState
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Appelli Esami",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = "Ing. Software • 15 Set",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Card Tasse
            LiquidCard(
                modifier = Modifier.weight(1f),
                backdropState = backdropState,
                shape = RoundedCornerShape(22.dp),
                contentPadding = 14.dp,
                onClick = onOpenTaxes,
                interactiveGelatin = true
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(colorScheme.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LiquidIcons.Warning,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        LiquidBadge(
                            text = "€ 156,00",
                            containerColor = colorScheme.primaryContainer,
                            contentColor = colorScheme.primary,
                            backdropState = backdropState
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Rata Tasse",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = "2ª Rata • Scade 31 Ago",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }



        // 3. Notizie — visualizzazione singola con LiquidAnimatedSwitcher (LiquidMorph, senza paginazione)
        if (homeNews.isNotEmpty()) {
            val safeActiveIndex = uiState.activeNewsIndex.coerceIn(0, homeNews.lastIndex)
            val currentNews = homeNews[safeActiveIndex]

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiquidSectionHeader(
                    title = stringResource(Res.string.ui_home_news_eyebrow),
                    subtitle = "Avviso ${safeActiveIndex + 1} di ${homeNews.size} • Scorri per sfogliare",
                    trailingContent = {
                        LiquidButton(
                            text = stringResource(Res.string.ui_home_news_all),
                            onClick = onOpenNews,
                            variant = LiquidButtonVariant.Text,
                            size = LiquidButtonSize.Small,
                            backdropState = backdropState
                        )
                    }
                )

                LiquidAnimatedSwitcher(
                    targetState = safeActiveIndex,
                    transition = LiquidSwitcherTransition.LiquidMorph,
                    onSwipeForward = {
                        onNextNews()
                    },
                    onSwipeBackward = {
                        onPreviousNews()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = "homeNewsSwitcher"
                ) { index ->
                    val newsItem = homeNews.getOrNull(index) ?: currentNews
                    LiquidStatusCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = newsItem.title,
                        description = newsItem.description,
                        statusType = newsItem.type,
                        backdropState = backdropState,
                        onClick = { onShowNews(newsItem) }
                    )
                }
            }
        }

        uiState.selectedNews?.let { news ->
            LiquidSheet(
                onDismissRequest = onDismissNews,
                title = news.title,
                subtitle = news.description,
                backdropState = backdropState
            ) {
                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                    Text(
                        text = news.fullContent,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )
                }
            }
        }

        // 4. Accesso Rapido — griglia di scorciatoie
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(clip = false)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_home_quick_access_eyebrow),
                subtitle = "I tuoi servizi e strumenti preferiti",
                trailingContent = {
                    LiquidButton(
                        onClick = onToggleCustomization,
                        variant = if (uiState.isCustomizing) LiquidButtonVariant.Tonal else LiquidButtonVariant.Glass,
                        size = LiquidButtonSize.Small,
                        backdropState = backdropState
                    ) {
                        AnimatedContent(
                            targetState = uiState.isCustomizing,
                            transitionSpec = {
                                (fadeIn() + scaleIn(initialScale = 0.85f)) togetherWith
                                (fadeOut() + scaleOut(targetScale = 0.85f))
                            },
                            label = "customizeBtn"
                        ) { customizing ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (customizing) LiquidIcons.Check else LiquidIcons.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (customizing) stringResource(Res.string.ui_done) else stringResource(Res.string.ui_customize),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            )

            if (uiState.isCustomizing) {
                // Pannello selezione
                LiquidCard(
                    modifier = Modifier.graphicsLayer(clip = false),
                    backdropState = backdropState,
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = 20.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer(clip = false),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.ui_home_customize_title),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            color = colorScheme.primary
                        )

                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer(clip = false),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            allActions.forEach { action ->
                                val isSelected = action.id in uiState.selectedActionIds
                                LiquidChip(
                                    label = action.title,
                                    selected = isSelected,
                                    onClick = {
                                        onToggleQuickAction(action.id)
                                    },
                                    leadingIcon = action.icon,
                                    trailingIcon = if (isSelected) LiquidIcons.Check else null,
                                    backdropState = backdropState
                                )
                            }
                        }



                        LiquidButton(
                            text = stringResource(Res.string.ui_save),
                            onClick = onFinishCustomization,
                            modifier = Modifier.fillMaxWidth(),
                            variant = LiquidButtonVariant.Primary,
                            backdropState = backdropState
                        )
                    }
                }
            } else {
                val currentSelectedActions = uiState.visibleQuickActions
                val chunkedActions = currentSelectedActions.chunked(2)
                chunkedActions.forEach { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer(clip = false),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { action ->
                            LiquidCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .graphicsLayer(clip = false),
                                backdropState = backdropState,
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = 16.dp,
                                onClick = { onQuickActionClick(action.id) }
                            ) {
                                Row(
                                    modifier = Modifier.graphicsLayer(clip = false),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = action.icon,
                                        contentDescription = null,
                                        tint = colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = action.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colorScheme.onSurface
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
