package com.anto426.uniapp.ui.didactics

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.LiquidVerticalDivider
import com.anto426.liquidmonet.components.feedback.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTabBar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.GradesUiState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.didactics.components.GraduationTargetTab
import com.anto426.uniapp.ui.didactics.components.SimulationChartTab
import com.anto426.uniapp.ui.didactics.components.SimulationTab
import com.anto426.uniapp.ui.didactics.components.toFixedTwoDecimals
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import kotlin.math.abs

@Composable
fun GradesScreen(
    backdropState: Backdrop,
    uiState: GradesUiState,
    onTabSelected: (Int) -> Unit,
    onSimulatedGradeChanged: (Int, Int) -> Unit,
    onSetAllGrades: (Int) -> Unit = {},
    onResetSimulation: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme

    val tabs = listOf(
        LiquidNavigationItem("Simulatore", icon = LiquidIcons.Edit),
        LiquidNavigationItem("Grafici", icon = LiquidIcons.Analytics),
        LiquidNavigationItem("Target Laurea", icon = LiquidIcons.Star),
    )

    // Derived statistics calculations
    val currentAverage = uiState.currentAverage
    val projectedAverage = uiState.projectedAverage
    val totalCfu = uiState.totalCurrentCfu
    val arithmeticAverage = if (uiState.currentExams.isEmpty()) 0.0 else uiState.currentExams.map { it.grade }.average()
    val currentGradBase = uiState.currentGraduationBase
    val projectedGradBase = uiState.projectedGraduationBase
    val deltaAverage = uiState.deltaAverage
    val hasActiveSimulation = uiState.activeSimulatedCount > 0

    UniScreenColumn {
        // ==========================================
        // 1. HERO SIMULATION & PERFORMANCE SHOWCASE
        // ==========================================
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
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "SIMULATORE MEDIA & CARRIERA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        letterSpacing = 1.sp,
                    )
                    LiquidBadge(
                        text = if (hasActiveSimulation) "${uiState.activeSimulatedCount} esami simulati" else "${uiState.currentExams.size} esami verbalizzati",
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.6f),
                        contentColor = colorScheme.primary,
                        backdropState = backdropState,
                    )
                }

                // Hero KPI: Media Simulata vs Attuale + Base Laurea + Delta
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = if (hasActiveSimulation) "Media Proiettata (Simulazione)" else "Media Ponderata Attuale",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = if (projectedAverage > 0.0) projectedAverage.toFixedTwoDecimals() else "—",
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
                        // Base Laurea Indicator
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
                                    text = if (projectedGradBase > 0.0) "${projectedGradBase.toFixedTwoDecimals()} / 110" else "—",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                )
                            }
                        }

                        // Delta Badge if simulation active
                        if (hasActiveSimulation && abs(deltaAverage) > 0.001) {
                            val isPositive = deltaAverage >= 0.0
                            Text(
                                text = if (isPositive) "Delta: +${deltaAverage.toFixedTwoDecimals()}" else "Delta: ${deltaAverage.toFixedTwoDecimals()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPositive) colorScheme.primary else colorScheme.error,
                            )
                        }
                    }
                }

                LiquidHorizontalDivider()

                // Bento Secondary KPI Tiles (3 Colonne trasparenti in puro vetro)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GradesKpiTile(
                        label = "Media Attuale",
                        value = if (currentAverage > 0.0) currentAverage.toFixedTwoDecimals() else "—",
                        subtitle = "Verbalizzata",
                        icon = LiquidIcons.Analytics,
                        accentColor = colorScheme.tertiary,
                        modifier = Modifier.weight(1f),
                    )

                    LiquidVerticalDivider(modifier = Modifier.height(44.dp))

                    GradesKpiTile(
                        label = "CFU Simulati",
                        value = "+${uiState.activeSimulatedCfu}",
                        subtitle = "${uiState.activeSimulatedCount} materie",
                        icon = LiquidIcons.Calendar,
                        accentColor = colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                    )

                    LiquidVerticalDivider(modifier = Modifier.height(44.dp))

                    GradesKpiTile(
                        label = "Totale CFU",
                        value = "${uiState.totalProjectedCfu}",
                        subtitle = "su 180 CFU",
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
                            text = "Avanzamento Carriera con Simulazione",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${(cfuProgress * 100).toInt()}% (${uiState.totalProjectedCfu}/180 CFU)",
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

        Spacer(modifier = Modifier.height(6.dp))

        // ==========================================
        // 2. TAB CONTROLS & CONTENT SWITCHER
        // ==========================================
        LiquidTabBar(
            items = tabs,
            selectedIndex = uiState.selectedTab,
            onTabSelected = onTabSelected,
            backdropState = backdropState,
        )

        Spacer(modifier = Modifier.height(4.dp))

        AnimatedContent(
            targetState = uiState.selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "gradesTabContent",
        ) { currentTab ->
            when (currentTab) {
                0 -> SimulationTab(
                    currentAverage = currentAverage,
                    projectedAverage = projectedAverage,
                    currentCfu = totalCfu,
                    simulationPresets = uiState.simulationPresets,
                    simulatedGrades = uiState.simulatedGrades,
                    onGradeChange = onSimulatedGradeChanged,
                    onSetAllGrades = onSetAllGrades,
                    onResetSimulation = onResetSimulation,
                    backdropState = backdropState,
                )
                1 -> SimulationChartTab(
                    uiState = uiState,
                    backdropState = backdropState,
                )
                else -> GraduationTargetTab(
                    currentAverage = if (hasActiveSimulation) projectedAverage else currentAverage,
                    totalCurrentCfu = uiState.totalProjectedCfu,
                    backdropState = backdropState,
                )
            }
        }
    }
}

@Composable
private fun GradesKpiTile(
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
