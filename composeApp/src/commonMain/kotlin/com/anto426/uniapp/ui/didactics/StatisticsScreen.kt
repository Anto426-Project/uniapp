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
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.charts.LiquidBarChart
import com.anto426.liquidmonet.components.charts.LiquidChartEntry
import com.anto426.liquidmonet.components.charts.LiquidDonutChart
import com.anto426.liquidmonet.components.charts.LiquidLineChart
import com.anto426.liquidmonet.components.charts.LiquidPieEntry
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.LiquidVerticalDivider
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
        LiquidNavigationItem("Andamento", icon = LiquidIcons.Analytics),
        LiquidNavigationItem("Crediti CFU", icon = LiquidIcons.Calendar),
        LiquidNavigationItem("Fasce Voto", icon = LiquidIcons.Star),
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
        // 1. HERO BENTO GLASS ACADEMIC PERFORMANCE SHOWCASE
        // =========================================================================
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(26.dp),
            contentPadding = 20.dp,
            interactiveGelatin = false,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header badge row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "PANORAMICA RENDIMENTO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        letterSpacing = 1.sp,
                    )
                    LiquidBadge(
                        text = "${uiState.totalExams} esami superati",
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.6f),
                        contentColor = colorScheme.primary,
                        backdropState = backdropState,
                    )
                }

                // Hero KPI: Media Ponderata + Base Laurea + Trend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Media Ponderata",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = if (uiState.weightedAverage > 0f) uiState.weightedAverage.toString() else "—",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = colorScheme.primary,
                            )
                            Text(
                                text = "/ 30",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        // Base di Laurea Indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 2.dp),
                        ) {
                            Icon(
                                imageVector = LiquidIcons.MenuBook,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Base Laurea",
                                    fontSize = 10.sp,
                                    color = colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = if (uiState.degreeBase > 0f) "${uiState.degreeBase} / 110" else "—",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                )
                            }
                        }

                        // Trend Badge
                        if (uiState.recentTrend != 0f) {
                            val isPositive = uiState.recentTrend > 0f
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = if (isPositive) "Trend: +${uiState.recentTrend}" else "Trend: ${uiState.recentTrend}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isPositive) colorScheme.primary else colorScheme.error,
                                )
                            }
                        }
                    }
                }

                LiquidHorizontalDivider()

                // Bento Secondary KPI Tiles (3 Colonne trasparenti in puro Liquid Glass con divisori verticali)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatsKpiBentoTile(
                        label = "Aritmetica",
                        value = if (uiState.arithmeticAverage > 0f) uiState.arithmeticAverage.toString() else "—",
                        subtitle = if (uiState.weightingSpread >= 0f) "+${uiState.weightingSpread} peso" else "${uiState.weightingSpread} peso",
                        icon = LiquidIcons.Analytics,
                        accentColor = colorScheme.tertiary,
                        modifier = Modifier.weight(1f),
                    )

                    LiquidVerticalDivider(modifier = Modifier.height(44.dp))

                    StatsKpiBentoTile(
                        label = "Crediti",
                        value = "${uiState.totalCfu}",
                        subtitle = "${uiState.averageCfuPerExam} CFU/esame",
                        icon = LiquidIcons.Calendar,
                        accentColor = colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                    )

                    LiquidVerticalDivider(modifier = Modifier.height(44.dp))

                    StatsKpiBentoTile(
                        label = "Top Voto",
                        value = uiState.highestGradeLabel,
                        subtitle = uiState.dominantTier?.label ?: "Eccellente",
                        icon = LiquidIcons.Star,
                        accentColor = colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                }

                // Career CFU Linear Progress
                val cfuProgress = uiState.careerProgress
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Avanzamento Piano di Studi",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${(cfuProgress * 100).toInt()}% (${uiState.totalCfu}/180 CFU)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                        )
                    }
                    LiquidLinearProgressIndicator(
                        progress = cfuProgress,
                        backdropState = backdropState,
                    )
                }
            }
        }

        // =========================================================================
        // 2. TAB CONTROLS
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
                            title = "Evoluzione Voti e Medie",
                            subtitle = "Confronto cronologico tra voti singoli, media ponderata e aritmetica",
                        )

                        LiquidCard(
                            backdropState = backdropState,
                            shape = RoundedCornerShape(26.dp),
                            contentPadding = 18.dp,
                            interactiveGelatin = false,
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
                                    primarySeriesLabel = "Voto",
                                )
                            }
                        }

                        // Insights Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            LiquidCard(
                                backdropState = backdropState,
                                shape = RoundedCornerShape(22.dp),
                                contentPadding = 14.dp,
                                interactiveGelatin = false,
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
                                            text = "Voto Più Alto",
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
                                interactiveGelatin = false,
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
                                            text = "Trend Recente",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Text(
                                        text = "${if (uiState.recentTrend >= 0f) "+" else ""}${uiState.recentTrend} Punti",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (uiState.recentTrend >= 0f) colorScheme.primary else colorScheme.error,
                                    )
                                    Text(
                                        text = "Variazione negli ultimi 3 esami sostenuti",
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
                            title = "Distribuzione Crediti per Esame",
                            subtitle = "Peso formativo e CFU verbalizzati per ogni materia",
                        )

                        LiquidCard(
                            backdropState = backdropState,
                            shape = RoundedCornerShape(26.dp),
                            contentPadding = 20.dp,
                            interactiveGelatin = false,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                LiquidBarChart(
                                    entries = cfuEntries,
                                    height = 195.dp,
                                    backdropState = backdropState,
                                    maxValue = uiState.cfuMax,
                                    valueSuffix = "CFU",
                                    detailDescription = "Crediti verbalizzati per questo esame",
                                )
                            }
                        }

                        // Summary Info Card
                        LiquidCard(
                            backdropState = backdropState,
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = 16.dp,
                            interactiveGelatin = false,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = "Riepilogo Crediti Formativi",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "${uiState.totalCfu} CFU totali acquisiti su 180 previsti",
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariant,
                                    )
                                }

                                LiquidBadge(
                                    text = if (uiState.totalCfu >= 60) "In Regola" else "In Corso",
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
                            title = "Ripartizione per Fasce di Voto",
                            subtitle = "Distribuzione analitica del rendimento accademico",
                        )

                        LiquidCard(
                            backdropState = backdropState,
                            shape = RoundedCornerShape(26.dp),
                            contentPadding = 20.dp,
                            interactiveGelatin = false,
                        ) {
                            LiquidDonutChart(
                                entries = donutEntries,
                                size = 190.dp,
                                centerLabel = "Esami",
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
                                    interactiveGelatin = true,
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

@Composable
private fun StatsKpiBentoTile(
    label: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier.padding(vertical = 2.dp, horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = accentColor.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(13.dp),
            )
        }

        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = accentColor,
        )

        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
