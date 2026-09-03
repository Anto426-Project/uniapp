package com.anto426.uniapp.ui.didactics

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.charts.LiquidBarChart
import com.anto426.liquidmonet.components.charts.LiquidChartEntry
import com.anto426.liquidmonet.components.charts.LiquidDonutChart
import com.anto426.liquidmonet.components.charts.LiquidLineChart
import com.anto426.liquidmonet.components.charts.LiquidPieEntry
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.liquidmonet.components.feedback.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTabBar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.StatisticsUiState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.Capsule

@Composable
fun StatisticsScreen(
    backdropState: Backdrop,
    uiState: StatisticsUiState,
    onTabSelected: (Int) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    val tabs = listOf(
        LiquidNavigationItem(stringResource(Res.string.ui_stats_tab_trend), icon = LiquidIcons.Analytics),
        LiquidNavigationItem(stringResource(Res.string.ui_stats_tab_cfu), icon = LiquidIcons.Calendar),
        LiquidNavigationItem(stringResource(Res.string.ui_stats_tab_tiers), icon = LiquidIcons.Star),
    )

    val gradeEntries = uiState.gradeEntries.map { LiquidChartEntry(it.label, it.value, it.secondaryValue) }
    val weightedAverageEntries = uiState.weightedAverageEntries.map { LiquidChartEntry(it.label, it.value) }
    val arithmeticAverageEntries = uiState.arithmeticAverageEntries.map { LiquidChartEntry(it.label, it.value) }
    val cfuEntries = uiState.cfuEntries.map { LiquidChartEntry(it.label, it.value) }

    val tierColors = listOf(
        colorScheme.primary,
        colorScheme.tertiary,
        colorScheme.secondary,
        colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
    )

    val donutEntries = uiState.gradeTiers.mapIndexed { index, tier ->
        LiquidPieEntry(tier.label, tier.examCount, color = tierColors[index % tierColors.size])
    }

    UniScreenColumn {
        // =========================================================================
        // 1. TAB CONTROLS
        // =========================================================================
        LiquidTabBar(
            items = tabs,
            selectedIndex = uiState.selectedTabIndex,
            onTabSelected = onTabSelected,
            backdropState = backdropState,
            modifier = Modifier.padding(top = 4.dp),
        )

        // =========================================================================
        // 3. TAB DETAILED CONTENT SWITCHER
        // =========================================================================
        AnimatedContent(
            targetState = uiState.selectedTabIndex,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "StatisticsTabAnimation",
        ) { tabIndex ->
            when (tabIndex) {
                0 -> {
                    // TAB 0: Evoluzione Voti & Medie
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        LiquidSectionHeader(
                            title = stringResource(Res.string.ui_stats_evolution_title),
                            subtitle = stringResource(Res.string.ui_stats_evolution_subtitle),
                        )

                        // 1. Grafico Linee in primo piano
                        LiquidCard(
                            backdropState = backdropState,
                            shape = RoundedCornerShape(26.dp),
                            contentPadding = 18.dp,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                LiquidLineChart(
                                    entries = gradeEntries,
                                    weightedAverageEntries = weightedAverageEntries,
                                    arithmeticAverageEntries = arithmeticAverageEntries,
                                    height = 210.dp,
                                    backdropState = backdropState,
                                    minValue = uiState.gradeMin,
                                    maxValue = uiState.gradeMax,
                                    showLegend = true,
                                    primarySeriesLabel = stringResource(Res.string.ui_stats_series_grade),
                                )
                            }
                        }

                        // 2. Progresso Piano di Studi (Avanzamento subito dopo il grafico)
                        LiquidCard(
                            backdropState = backdropState,
                            shape = RoundedCornerShape(22.dp),
                            contentPadding = 16.dp,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = stringResource(Res.string.ui_stats_study_plan_progress),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = "${(uiState.careerProgress * 100).toInt()}% • ${uiState.totalCfu}/180 CFU",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary,
                                    )
                                }
                                LiquidLinearProgressIndicator(
                                    progress = uiState.careerProgress,
                                    backdropState = backdropState,
                                )
                            }
                        }

                        // 3. Riepilogo Performance Principali (Media Ponderata, Base Laurea, Esami)
                        LiquidCard(
                            backdropState = backdropState,
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = 18.dp,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column {
                                        Text(
                                            text = stringResource(Res.string.ui_stats_weighted_average_header),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.primary,
                                            letterSpacing = 0.6.sp,
                                            fontSize = 10.sp,
                                        )
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(
                                                text = if (uiState.weightedAverage > 0f) uiState.weightedAverage.toString() else "—",
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Black,
                                                color = colorScheme.onSurface,
                                            )
                                            Text(
                                                text = " / 30",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(bottom = 3.dp),
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = stringResource(Res.string.ui_stats_degree_projection_header),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.primary,
                                            letterSpacing = 0.6.sp,
                                            fontSize = 10.sp,
                                        )
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(
                                                text = if (uiState.degreeBase > 0f) uiState.degreeBase.toString() else "—",
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Black,
                                                color = colorScheme.onSurface,
                                            )
                                            Text(
                                                text = " / 110",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(bottom = 3.dp),
                                            )
                                        }
                                    }
                                }

                                LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column {
                                        Text(
                                            text = stringResource(Res.string.ui_stats_arithmetic_average),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            text = if (uiState.arithmeticAverage > 0f) uiState.arithmeticAverage.toString() else "—",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onSurface,
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = stringResource(Res.string.ui_stats_weighting_spread),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            text = if (uiState.weightingSpread >= 0f) "+${uiState.weightingSpread}" else "${uiState.weightingSpread}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.primary,
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = stringResource(Res.string.ui_stats_passed_exams),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            text = "${uiState.totalExams}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onSurface,
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Grid Approfondimenti: Voto Più Alto & Trend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            LiquidCard(
                                backdropState = backdropState,
                                shape = RoundedCornerShape(22.dp),
                                contentPadding = 14.dp,
                                modifier = Modifier.weight(1f),
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Icon(
                                            imageVector = LiquidIcons.Star,
                                            contentDescription = null,
                                            tint = colorScheme.primary,
                                            modifier = Modifier.size(15.dp),
                                        )
                                        Text(
                                            text = stringResource(Res.string.ui_stats_highest_grade),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Text(
                                        text = uiState.highestGradeLabel,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = colorScheme.primary,
                                    )
                                    Text(
                                        text = uiState.highestGradeCourses,
                                        fontSize = 11.sp,
                                        color = colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }

                            LiquidCard(
                                backdropState = backdropState,
                                shape = RoundedCornerShape(22.dp),
                                contentPadding = 14.dp,
                                modifier = Modifier.weight(1f),
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Icon(
                                            imageVector = LiquidIcons.Analytics,
                                            contentDescription = null,
                                            tint = colorScheme.primary,
                                            modifier = Modifier.size(15.dp),
                                        )
                                        Text(
                                            text = stringResource(Res.string.ui_stats_recent_trend),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Text(
                                        text = if (uiState.recentTrend >= 0f) {
                                            stringResource(Res.string.ui_stats_trend_points_positive, uiState.recentTrend)
                                        } else {
                                            stringResource(Res.string.ui_stats_trend_points_negative, uiState.recentTrend)
                                        },
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (uiState.recentTrend >= 0f) colorScheme.primary else colorScheme.error,
                                    )
                                    Text(
                                        text = stringResource(Res.string.ui_stats_recent_trend_desc),
                                        fontSize = 11.sp,
                                        color = colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: Crediti CFU
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        LiquidSectionHeader(
                            title = stringResource(Res.string.ui_stats_cfu_distribution_title),
                            subtitle = stringResource(Res.string.ui_stats_cfu_distribution_sub),
                        )

                        LiquidCard(
                            backdropState = backdropState,
                            shape = RoundedCornerShape(26.dp),
                            contentPadding = 20.dp,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                LiquidBarChart(
                                    entries = cfuEntries,
                                    height = 195.dp,
                                    backdropState = backdropState,
                                    maxValue = uiState.cfuMax,
                                    valueSuffix = "CFU",
                                    detailDescription = stringResource(Res.string.ui_stats_cfu_bar_detail),
                                )
                            }
                        }

                        // Summary Info Card
                        LiquidCard(
                            backdropState = backdropState,
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = 16.dp,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = stringResource(Res.string.ui_stats_cfu_summary_title),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onSurface,
                                    )
                                    Text(
                                        text = stringResource(Res.string.ui_stats_cfu_summary_sub, uiState.totalCfu),
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariant,
                                    )
                                }

                                LiquidBadge(
                                    text = if (uiState.totalCfu >= 60) stringResource(Res.string.ui_status_in_order) else stringResource(Res.string.ui_status_in_progress),
                                    containerColor = colorScheme.primaryContainer,
                                    contentColor = colorScheme.primary,
                                    backdropState = backdropState,
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: Fasce di Voto
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        LiquidSectionHeader(
                            title = stringResource(Res.string.ui_stats_grade_tiers_title),
                            subtitle = stringResource(Res.string.ui_stats_grade_tiers_sub),
                        )

                        LiquidCard(
                            backdropState = backdropState,
                            shape = RoundedCornerShape(26.dp),
                            contentPadding = 20.dp,
                        ) {
                            LiquidDonutChart(
                                entries = donutEntries,
                                size = 190.dp,
                                centerLabel = stringResource(Res.string.ui_exams),
                                centerValue = uiState.totalExams.toString(),
                                backdropState = backdropState,
                            )
                        }

                        // Detailed Tier Breakdown Cards with visual percentage bar
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            donutEntries.forEach { entry ->
                                val tierRatio = if (uiState.totalExams > 0) entry.value / uiState.totalExams else 0f
                                val tierPercentage = (tierRatio * 100).toInt()
                                val tierColor = entry.color ?: colorScheme.primary

                                LiquidCard(
                                    backdropState = backdropState,
                                    shape = RoundedCornerShape(18.dp),
                                    contentPadding = 14.dp,
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(Capsule())
                                                        .background(tierColor),
                                                )
                                                Text(
                                                    text = "Fascia ${entry.label}",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = colorScheme.onSurface,
                                                )
                                            }

                                            Text(
                                                text = "${entry.value.toInt()} esami • $tierPercentage%",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = tierColor,
                                            )
                                        }

                                        // Progress indicator for tier proportion
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .clip(Capsule())
                                                .background(colorScheme.onSurface.copy(alpha = 0.08f)),
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(tierRatio.coerceIn(0f, 1f))
                                                    .height(4.dp)
                                                    .clip(Capsule())
                                                    .background(tierColor),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
