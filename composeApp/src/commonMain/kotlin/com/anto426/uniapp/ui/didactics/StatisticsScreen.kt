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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.charts.LiquidBarChart
import com.anto426.liquidmonet.components.charts.LiquidChartEntry
import com.anto426.liquidmonet.components.charts.LiquidDonutChart
import com.anto426.liquidmonet.components.charts.LiquidLineChart
import com.anto426.liquidmonet.components.charts.LiquidPieEntry
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
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
        LiquidNavigationItem("Voti", icon = LiquidIcons.Star),
        LiquidNavigationItem("Crediti", icon = LiquidIcons.Calendar),
        LiquidNavigationItem("Fasce", icon = LiquidIcons.Info)
    )

    val gradeEntries = uiState.gradeEntries.map { LiquidChartEntry(it.label, it.value, it.secondaryValue) }
    val weightedAverageEntries = uiState.weightedAverageEntries.map { LiquidChartEntry(it.label, it.value) }
    val arithmeticAverageEntries = uiState.arithmeticAverageEntries.map { LiquidChartEntry(it.label, it.value) }
    val cfuEntries = uiState.cfuEntries.map { LiquidChartEntry(it.label, it.value) }
    val tierColors = listOf(colorScheme.primary, colorScheme.tertiary, colorScheme.secondary)
    val donutEntries = uiState.gradeTiers.mapIndexed { index, tier ->
        LiquidPieEntry(tier.label, tier.examCount, color = tierColors[index % tierColors.size])
    }

    UniScreenColumn {
        // 1. KPI Overview Header Cards (Media Ponderata, Media Aritmetica, Base Laurea)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Media Ponderata
            LiquidCard(
                backdropState = backdropState,
                shape = RoundedCornerShape(20.dp),
                contentPadding = 14.dp,
                modifier = Modifier.weight(1f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ponderata",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = LiquidIcons.Star,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = uiState.weightedAverage.toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.primary
                    )
                    Text(
                        text = "Media / 30",
                        fontSize = 10.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            // Media Aritmetica
            LiquidCard(
                backdropState = backdropState,
                shape = RoundedCornerShape(20.dp),
                contentPadding = 14.dp,
                modifier = Modifier.weight(1f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Aritmetica",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = LiquidIcons.Edit,
                            contentDescription = null,
                            tint = colorScheme.tertiary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = uiState.arithmeticAverage.toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "Media / 30",
                        fontSize = 10.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            // Base di Laurea
            LiquidCard(
                backdropState = backdropState,
                shape = RoundedCornerShape(20.dp),
                contentPadding = 14.dp,
                modifier = Modifier.weight(1.05f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Base Laurea",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = LiquidIcons.Check,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = uiState.degreeBase.toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "Prevista / 110",
                        fontSize = 10.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Tab Bar
        LiquidTabBar(
            items = tabs,
            selectedIndex = uiState.selectedTabIndex,
            onTabSelected = onTabSelected,
            backdropState = backdropState,
            modifier = Modifier.padding(top = 6.dp)
        )

        // 3. Tab Content
        AnimatedContent(
            targetState = uiState.selectedTabIndex,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "StatisticsTabAnimation"
        ) { tabIndex ->
            when (tabIndex) {
                0 -> {
                    // TAB 1: Multi-Line Chart (Voti + Media Ponderata + Media Aritmetica nello stesso grafico)
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        LiquidSectionHeader(
                            title = "Evoluzione Voti e Medie",
                            subtitle = "Confronto simultaneo tra singoli voti, media ponderata e aritmetica"
                        )

                        LiquidCard(
                            backdropState = backdropState,
                            shape = RoundedCornerShape(26.dp),
                            contentPadding = 18.dp
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                LiquidLineChart(
                                    entries = gradeEntries,
                                    weightedAverageEntries = weightedAverageEntries,
                                    arithmeticAverageEntries = arithmeticAverageEntries,
                                    height = 200.dp,
                                    backdropState = backdropState,
                                    minValue = 24f,
                                    maxValue = 31.5f,
                                    showLegend = true
                                )
                            }
                        }

                        // Statistiche Dettagliate
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Voto Più Alto",
                                        fontSize = 11.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "30 e Lode",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary
                                    )
                                    Text(
                                        text = "Prog I & Ing. Software",
                                        fontSize = 10.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Trend Recente",
                                        fontSize = 11.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "+0.4 Media",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary
                                    )
                                    Text(
                                        text = "Negli ultimi 3 esami",
                                        fontSize = 10.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 2: Bar Chart
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        LiquidSectionHeader(
                            title = "Acquisizione Crediti per Semestre",
                            subtitle = "Distribuzione dei 120 CFU superati nel triennio"
                        )

                        LiquidCard(
                            backdropState = backdropState,
                            shape = RoundedCornerShape(26.dp),
                            contentPadding = 20.dp
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                LiquidBarChart(
                                    entries = cfuEntries,
                                    height = 190.dp,
                                    backdropState = backdropState,
                                    maxValue = 35f
                                )
                            }
                        }

                        // Summary Info
                        LiquidCard(
                            backdropState = backdropState,
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = 16.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Media Crediti per Semestre",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onSurface
                                    )
                                    Text(
                                        text = "30.0 CFU / semestre nei primi 2 anni",
                                        fontSize = 11.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }

                                LiquidBadge(
                                    text = "In Regola",
                                    containerColor = colorScheme.primaryContainer,
                                    contentColor = colorScheme.primary,
                                    backdropState = backdropState
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 3: Donut Chart
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        LiquidSectionHeader(
                            title = "Ripartizione per Fasce di Voto",
                            subtitle = "Tocca uno spicchio per visualizzare il dettaglio"
                        )

                        LiquidCard(
                            backdropState = backdropState,
                            shape = RoundedCornerShape(26.dp),
                            contentPadding = 20.dp
                        ) {
                            LiquidDonutChart(
                                entries = donutEntries,
                                size = 180.dp,
                                centerLabel = "Esami",
                                centerValue = uiState.totalExams.toString(),
                                backdropState = backdropState
                            )
                        }

                        // Detailed list
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            donutEntries.forEach { entry ->
                                LiquidCard(
                                    backdropState = backdropState,
                                    shape = RoundedCornerShape(18.dp),
                                    contentPadding = 14.dp
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(Capsule())
                                                    .background(entry.color ?: colorScheme.primary)
                                            )
                                            Text(
                                                text = "Fascia ${entry.label}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colorScheme.onSurface
                                            )
                                        }

                                        Text(
                                            text = "${entry.value.toInt()} esami (${((entry.value / uiState.totalExams.coerceAtLeast(1)) * 100).toInt()}%)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = entry.color ?: colorScheme.primary
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
