package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.feedback.progressbar.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.didactics.presentation.GradesUiState
import com.anto426.uniapp.ui.components.display.UniCircularProgressRing
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniContentHost
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.layout.UniTabContentHost
import com.anto426.uniapp.ui.motion.UniMotion
import com.anto426.uniapp.ui.didactics.components.GradesActions
import com.anto426.uniapp.ui.didactics.components.GraduationTargetTab
import com.anto426.uniapp.ui.didactics.components.SimulationChartTab
import com.anto426.uniapp.ui.didactics.components.SimulationTab
import com.anto426.uniapp.ui.didactics.components.gradeMetric
import com.anto426.uniapp.ui.didactics.components.signedGradeMetric
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun GradesScreen(
    uiState: GradesUiState,
    onTabSelected: (Int) -> Unit,
    onToggleSimulationItem: (String) -> Unit,
    onSimulatedGradeChanged: (String, Int) -> Unit,
    onSimulatedCfuChanged: (String, Int) -> Unit = { _, _ -> },
    onAddCustomExam: (String, Int, Int) -> Unit = { _, _, _ -> },
    onRemoveCustomExam: (String) -> Unit = {},
    onSetAllGrades: (Int) -> Unit = {},
    onApplyCurrentAverage: () -> Unit = {},
    onResetSimulation: () -> Unit = {},
    onTargetDegreeChanged: (Int) -> Unit = {},
    onThesisPointsChanged: (Int) -> Unit = {},
    onBonusPointsChanged: (Int) -> Unit = {},
) {
    var includeSimulatedAnalytics by rememberSaveable { mutableStateOf(false) }

    val activeTabIndex = when (uiState.selectedTab) {
        1 -> 1
        2 -> 2
        else -> 0
    }
    val showProjectedAnalytics = uiState.activeSimulatedCount > 0 &&
        (uiState.selectedTab == 3 || includeSimulatedAnalytics)

    val tabs = listOf(
        LiquidNavigationItem(stringResource(Res.string.ui_grades_real)),
        LiquidNavigationItem(
            label = stringResource(Res.string.ui_grades_tab_simula),
            badge = uiState.activeSimulatedCount.takeIf { it > 0 }?.toString(),
        ),
        LiquidNavigationItem(stringResource(Res.string.ui_grades_tab_obiettivo)),
    )

    UniScreenColumn {
        LiquidTabBar(
            items = tabs,
            selectedIndex = activeTabIndex,
            onTabSelected = { index ->
                if (index == 0) includeSimulatedAnalytics = false
                onTabSelected(index)
            },
        )

        UniTabContentHost(
            selectedTab = activeTabIndex,
            tabCount = tabs.size,
            onTabSelected = { index ->
                if (index == 0) includeSimulatedAnalytics = false
                onTabSelected(index)
            },
            swipeEnabled = false,
            label = "GradesTabs",
        ) { tab ->
            when (tab) {
                1 -> SimulationTab(
                    uiState = uiState,
                    onToggleItem = onToggleSimulationItem,
                    onGradeChange = onSimulatedGradeChanged,
                    onCfuChange = onSimulatedCfuChanged,
                    onAddCustomExam = onAddCustomExam,
                    onRemoveCustomExam = onRemoveCustomExam,
                    onSetAllGrades = onSetAllGrades,
                    onApplyCurrentAverage = onApplyCurrentAverage,
                    onResetSimulation = onResetSimulation,
                )

                2 -> GraduationTargetTab(
                    uiState = uiState,
                    onTargetDegreeChange = onTargetDegreeChanged,
                    onThesisPointsChange = onThesisPointsChanged,
                    onBonusPointsChange = onBonusPointsChanged,
                )

                else -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (uiState.activeSimulatedCount > 0) {
                        val scopeOptions = listOf(
                            false to stringResource(Res.string.ui_grades_scope_real),
                            true to "${stringResource(Res.string.ui_grades_projection)} (${uiState.activeSimulatedCount})",
                        )
                        GradesActions {
                            scopeOptions.forEach { (isProjected, label) ->
                                key(isProjected) {
                                    val selected = showProjectedAnalytics == isProjected
                                    LiquidChip(
                                        label = label,
                                        selected = selected,
                                        leadingIcon = if (selected) LiquidIcons.Check else null,
                                        onClick = {
                                            includeSimulatedAnalytics = isProjected
                                            if (uiState.selectedTab == 3 && !isProjected) {
                                                onTabSelected(0)
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }

                    UniContentHost(
                        targetState = showProjectedAnalytics,
                        modifier = Modifier.fillMaxWidth(),
                        transition = UniMotion.inlineContentTransition,
                        label = "GradesAnalyticsScope",
                    ) { projected ->
                        val chart = uiState.chartState(projected)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            UniAnimatedEntrance(order = 0) {
                                GradesAnalyticsHeroCard(
                                    chart = chart,
                                    projected = projected,
                                    deltaAverage = uiState.deltaAverage,
                                )
                            }

                            UniAnimatedEntrance(order = 1) {
                                SimulationChartTab(uiState = chart)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GradesAnalyticsHeroCard(
    chart: GradesUiState,
    projected: Boolean,
    deltaAverage: Double,
) {
    val colors = MaterialTheme.colorScheme
    val glass = LiquidGlassTheme.colors
    val cardShape = remember { RoundedRectangle(24.dp) }
    val trend = chart.recentTrend.toDouble()
    val weightingSpread = if (chart.currentExams.isNotEmpty() && chart.currentAverage > 0.0 && chart.currentArithmeticAverage > 0.0) {
        chart.currentAverage - chart.currentArithmeticAverage
    } else {
        null
    }
    val accent = when {
        !projected -> colors.primary
        deltaAverage >= 0.0 -> glass.success
        else -> colors.error
    }
    val degreeProgress = (chart.currentGraduationBase / 110.0).toFloat().coerceIn(0f, 1f)
    val cfuPercent = (chart.careerProgress * 100f).roundToInt()

    LiquidCard(
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape,
        contentPadding = PaddingValues(18.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(
                            if (projected) Res.string.ui_grades_projection else Res.string.ui_grades_scope_real,
                        ).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = gradeMetric(chart.currentAverage),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Black,
                                fontFeatureSettings = "tnum",
                            ),
                            color = colors.onSurface,
                            maxLines = 1,
                        )
                        if (chart.currentAverage > 0.0) {
                            Text(
                                text = "/30",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (projected) {
                            LiquidBadge(
                                text = stringResource(Res.string.ui_grades_simulation_delta, signedGradeMetric(deltaAverage)),
                                containerColor = accent.copy(alpha = 0.14f),
                                contentColor = accent,
                            )
                        } else {
                            LiquidBadge(
                                text = stringResource(Res.string.ui_career_exams_count, chart.totalPassedExamsCount),
                                containerColor = colors.primary.copy(alpha = 0.13f),
                                contentColor = colors.primary,
                            )
                        }
                        if (chart.lodeCount > 0) {
                            LiquidBadge(
                                text = stringResource(Res.string.ui_career_lodi_count, chart.lodeCount),
                                containerColor = colors.tertiary.copy(alpha = 0.14f),
                                contentColor = colors.tertiary,
                            )
                        }
                    }
                }

                UniCircularProgressRing(
                    progress = degreeProgress,
                    centerText = gradeMetric(chart.currentGraduationBase),
                    subtitleText = "/110",
                    size = 78.dp,
                    strokeWidth = 7.dp,
                    primaryColor = accent,
                    secondaryColor = colors.tertiary,
                )
            }

            LiquidHorizontalDivider(color = colors.onSurface.copy(alpha = 0.08f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HeroCompactMetric(
                    label = stringResource(Res.string.ui_stats_arithmetic_average),
                    value = gradeMetric(chart.currentArithmeticAverage),
                    valueColor = colors.onSurface,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                HeroCompactMetric(
                    label = stringResource(Res.string.ui_stats_weighting_spread),
                    value = weightingSpread?.let { signedGradeMetric(it) } ?: "—",
                    valueColor = when {
                        weightingSpread == null -> colors.onSurface
                        weightingSpread >= 0.0 -> glass.success
                        else -> colors.error
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                HeroCompactMetric(
                    label = stringResource(Res.string.ui_stats_recent_trend),
                    value = if (chart.currentExams.size < 2) "—" else signedGradeMetric(trend),
                    valueColor = when {
                        chart.currentExams.size < 2 -> colors.onSurface
                        trend >= 0.0 -> glass.success
                        else -> colors.error
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }

            LiquidHorizontalDivider(color = colors.onSurface.copy(alpha = 0.08f))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(
                            if (projected) Res.string.ui_grades_cfu_progress_projected
                            else Res.string.ui_stats_study_plan_progress,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${chart.totalProjectedCfu}/${chart.degreeCfuTarget} CFU · $cfuPercent%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum",
                        ),
                        color = accent,
                        maxLines = 1,
                    )
                }
                LiquidLinearProgressIndicator(
                    progress = chart.careerProgress,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (chart.remainingDegreeCfu > 0) {
                    Text(
                        text = stringResource(Res.string.ui_grades_cfu_remaining_format, chart.remainingDegreeCfu),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCompactMetric(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum",
            ),
            color = valueColor,
            maxLines = 1,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
