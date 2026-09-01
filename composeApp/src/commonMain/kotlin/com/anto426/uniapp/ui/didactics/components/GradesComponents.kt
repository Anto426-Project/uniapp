package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.charts.LiquidBarChart
import com.anto426.liquidmonet.components.charts.LiquidLineChart
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.liquidmonet.components.selection.LiquidStepper
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.GradesUiState
import com.anto426.uniapp.model.didactics.GradeExam
import com.anto426.uniapp.model.didactics.GradeSimulationPreset
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import kotlin.math.abs
import kotlin.math.round

/**
 * Tab 0: Lista Esami Verbalizzati con formattazione grafica completa.
 */
@Composable
fun CurrentExamsTab(
    exams: List<GradeExam>,
    totalCfu: Int,
    backdropState: Backdrop,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_grades_verbalized_title),
            subtitle = stringResource(Res.string.ui_grades_verbalized_subtitle, exams.size, totalCfu),
        )

        if (exams.isEmpty()) {
            LiquidCard(
                backdropState = backdropState,
                shape = RoundedCornerShape(20.dp),
                contentPadding = 24.dp,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = LiquidIcons.Info,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp),
                    )
                    Text(
                        text = stringResource(Res.string.ui_grades_verbalized_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            exams.forEach { exam ->
                val examWeightPct = if (totalCfu > 0) ((exam.cfu.toDouble() / totalCfu) * 100).toInt() else 0

                LiquidCard(
                    backdropState = backdropState,
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = 14.dp,
                    interactiveGelatin = false,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Grade Circle Badge
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (exam.grade >= 30) colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    else colorScheme.surface.copy(alpha = 0.65f)
                                )
                                .border(
                                    1.dp,
                                    if (exam.grade >= 30) colorScheme.primary.copy(alpha = 0.4f)
                                    else colorScheme.outlineVariant.copy(alpha = 0.2f),
                                    RoundedCornerShape(14.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (exam.grade == 31) "30L" else exam.grade.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (exam.grade >= 30) colorScheme.primary else colorScheme.onSurface,
                            )
                        }

                        // Exam Info
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = exam.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface,
                                maxLines = 2,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "${exam.cfu} CFU",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = colorScheme.primary,
                                )
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                )
                                Text(
                                    text = "Peso $examWeightPct% sulla media",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 1: Simulatore di Media Interattivo con calcolo previsionale in tempo reale.
 */
@Composable
fun SimulationTab(
    currentAverage: Double,
    projectedAverage: Double,
    currentCfu: Int,
    simulationPresets: List<GradeSimulationPreset>,
    simulatedGrades: List<Int>,
    onGradeChange: (Int, Int) -> Unit,
    onSetAllGrades: (Int) -> Unit,
    onResetSimulation: () -> Unit,
    backdropState: Backdrop,
) {
    val colorScheme = MaterialTheme.colorScheme

    val delta = projectedAverage - currentAverage
    val isPositive = delta >= 0.0
    val deltaString = if (isPositive) "+${delta.toFixedTwoDecimals()}" else delta.toFixedTwoDecimals()

    val currentGradBase = (currentAverage * 110.0) / 30.0
    val projectedGradBase = (projectedAverage * 110.0) / 30.0

    val activeCount = simulatedGrades.count { it >= 18 }
    val simulatedCfu = simulationPresets.mapIndexedNotNull { index, preset ->
        if (simulatedGrades.getOrElse(index) { 0 } >= 18) preset.cfu else null
    }.sum()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Preset Actions Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LiquidButton(
                onClick = { onSetAllGrades(30) },
                variant = LiquidButtonVariant.Tonal,
                size = LiquidButtonSize.Small,
                backdropState = backdropState,
                modifier = Modifier.weight(1f),
            ) {
                Text("Tutti 30", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            LiquidButton(
                onClick = { onSetAllGrades(28) },
                variant = LiquidButtonVariant.Glass,
                size = LiquidButtonSize.Small,
                backdropState = backdropState,
                modifier = Modifier.weight(1f),
            ) {
                Text("Tutti 28", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            LiquidButton(
                onClick = { onSetAllGrades(24) },
                variant = LiquidButtonVariant.Glass,
                size = LiquidButtonSize.Small,
                backdropState = backdropState,
                modifier = Modifier.weight(1f),
            ) {
                Text("Tutti 24", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            LiquidButton(
                onClick = onResetSimulation,
                variant = LiquidButtonVariant.Glass,
                size = LiquidButtonSize.Small,
                backdropState = backdropState,
                modifier = Modifier.weight(1f),
            ) {
                Text("Azzera", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Section header for simulation items
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_grades_simulation_to_simulate),
            subtitle = stringResource(Res.string.ui_grades_simulation_to_simulate_sub),
        )

        if (simulationPresets.isEmpty()) {
            LiquidCard(
                backdropState = backdropState,
                shape = RoundedCornerShape(18.dp),
                contentPadding = 20.dp,
            ) {
                Text(
                    text = stringResource(Res.string.ui_grades_simulation_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            simulationPresets.forEachIndexed { index, preset ->
                val currentGrade = simulatedGrades.getOrElse(index) { 0 }
                val isIncluded = currentGrade >= 18

                LiquidCard(
                    backdropState = backdropState,
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = 14.dp,
                    onClick = {
                        onGradeChange(index, if (isIncluded) 0 else 28)
                    },
                    containerColor = if (isIncluded) colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent,
                    interactiveGelatin = true,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isIncluded) colorScheme.primary else colorScheme.outlineVariant),
                                )
                                Column {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isIncluded) colorScheme.onSurface else colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                    Text(
                                        text = "${preset.cfu} CFU • ${if (isIncluded) "Incluso nel calcolo" else "Tocca per attivare"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isIncluded) colorScheme.primary else colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    )
                                }
                            }

                            if (isIncluded) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colorScheme.primaryContainer.copy(alpha = 0.5f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                ) {
                                    Text(
                                        text = "$currentGrade / 30",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Black,
                                        color = colorScheme.primary,
                                    )
                                }
                            }
                        }

                        if (isIncluded) {
                            LiquidStepper(
                                value = currentGrade,
                                onValueChange = { newGrade -> onGradeChange(index, newGrade) },
                                minValue = 18,
                                maxValue = 30,
                                backdropState = backdropState,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 2: Grafici di Proiezione e Analisi di Impatto della Simulazione (in stile Statistiche).
 */
@Composable
fun SimulationChartTab(
    uiState: GradesUiState,
    backdropState: Backdrop,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Section 1: Evoluzione Voti & Proiezione Medie
        LiquidSectionHeader(
            title = "Proiezione Voti e Medie",
            subtitle = "Confronto cronologico tra voti, media ponderata e aritmetica (inclusa simulazione)",
        )

        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(26.dp),
            contentPadding = 18.dp,
            interactiveGelatin = false,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (uiState.singleGradeEntries.isNotEmpty()) {
                    LiquidLineChart(
                        entries = uiState.singleGradeEntries,
                        weightedAverageEntries = uiState.weightedAverageEntries,
                        arithmeticAverageEntries = uiState.arithmeticAverageEntries,
                        height = 210.dp,
                        backdropState = backdropState,
                        minValue = uiState.gradeMin,
                        maxValue = uiState.gradeMax,
                        showLegend = true,
                        primarySeriesLabel = "Voto",
                    )
                } else {
                    Text(
                        text = "Nessun dato disponibile per il grafico",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                    )
                }
            }
        }

        // Insights Grid (Delta Media & Nuova Base Laurea)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val delta = uiState.deltaAverage
            val isPositive = delta >= 0.0

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
                            text = "Delta Media",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = "${if (isPositive) "+" else ""}${delta.toFixedTwoDecimals()} Punti",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isPositive) colorScheme.primary else colorScheme.error,
                    )
                    Text(
                        text = if (uiState.activeSimulatedCount > 0) "${uiState.activeSimulatedCount} esami in simulazione" else "Nessun esame simulato",
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
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
                            imageVector = LiquidIcons.MenuBook,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = "Nuova Base Laurea",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = "${uiState.projectedGraduationBase.toFixedTwoDecimals()} / 110",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.primary,
                    )
                    Text(
                        text = "Attuale: ${uiState.currentGraduationBase.toFixedTwoDecimals()}",
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        // Section 2: Crediti CFU
        LiquidSectionHeader(
            title = "Distribuzione Crediti per Esame",
            subtitle = "Peso formativo e CFU degli esami sostenuti e simulati",
        )

        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(26.dp),
            contentPadding = 20.dp,
            interactiveGelatin = false,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (uiState.cfuEntries.isNotEmpty()) {
                    LiquidBarChart(
                        entries = uiState.cfuEntries,
                        height = 195.dp,
                        backdropState = backdropState,
                        maxValue = uiState.cfuMax,
                        valueSuffix = "CFU",
                        detailDescription = "Crediti formativi per questo esame",
                    )
                } else {
                    Text(
                        text = "Nessun dato CFU disponibile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                    )
                }
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
                        text = "${uiState.totalProjectedCfu} CFU proiettati (${uiState.totalCurrentCfu} verbalizzati + ${uiState.activeSimulatedCfu} simulati)",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "${((uiState.totalProjectedCfu.toFloat() / 180f) * 100).toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.primary,
                    )
                }
            }
        }
    }
}

/**
 * Tab 2: Obiettivi e Target di Laurea.
 */
@Composable
fun GraduationTargetTab(
    currentAverage: Double,
    totalCurrentCfu: Int,
    backdropState: Backdrop,
) {
    val colorScheme = MaterialTheme.colorScheme

    var selectedTarget by remember { mutableIntStateOf(110) }
    var thesisPoints by remember { mutableIntStateOf(5) }
    val totalDegreeCfu = 180
    val remainingCfu = (totalDegreeCfu - totalCurrentCfu).coerceAtLeast(0)

    val currentGradBase = (currentAverage * 110.0) / 30.0
    val neededBase = (selectedTarget - thesisPoints).toDouble()
    val neededAverage = (neededBase * 30.0) / 110.0

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_grades_target_title),
            subtitle = stringResource(Res.string.ui_grades_target_subtitle),
        )

        // Target Selector (2 a 2)
        val targets = listOf(100, 105, 110, 111) // 111 represents 110L
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            targets.chunked(2).forEach { rowTargets ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowTargets.forEach { target ->
                        val isSelected = selectedTarget == target
                        val label = if (target == 111) "110 e Lode" else "$target / 110"
                        val desc = when (target) {
                            100 -> "Obiettivo solido"
                            105 -> "Fascia alta"
                            110 -> "Massimo dei voti"
                            else -> "Eccellenza assoluta"
                        }

                        LiquidCard(
                            backdropState = backdropState,
                            modifier = Modifier
                                .weight(1f)
                                .height(76.dp),
                            shape = RoundedCornerShape(18.dp),
                            onClick = { selectedTarget = target },
                            containerColor = if (isSelected) colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                            contentPadding = 12.dp,
                            interactiveGelatin = true,
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) colorScheme.primary else colorScheme.onSurface,
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = LiquidIcons.Check,
                                            contentDescription = null,
                                            tint = colorScheme.primary,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Analysis Result Card
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(22.dp),
            contentPadding = 16.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Requisiti per ${if (selectedTarget == 111) "110L" else "$selectedTarget/110"}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )

                // Thesis points stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = stringResource(Res.string.ui_grades_thesis_points),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(Res.string.ui_grades_thesis_points_sub),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                    LiquidStepper(
                        value = thesisPoints,
                        onValueChange = { thesisPoints = it },
                        minValue = 1,
                        maxValue = 8,
                        backdropState = backdropState,
                    )
                }

                LiquidHorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Calculated Results
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colorScheme.surface.copy(alpha = 0.65f))
                            .padding(12.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(Res.string.ui_grades_needed_base),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = neededBase.toFixedTwoDecimals(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface,
                            )
                            Text(
                                text = "Attuale: ${currentGradBase.toFixedTwoDecimals()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colorScheme.primary.copy(alpha = 0.12f))
                            .border(1.dp, colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(Res.string.ui_grades_needed_average),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.primary,
                            )
                            Text(
                                text = neededAverage.toFixedTwoDecimals(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = colorScheme.primary,
                            )
                            Text(
                                text = if (neededAverage <= 30.0) stringResource(Res.string.ui_grades_achievable) else stringResource(Res.string.ui_grades_requires_thesis),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (neededAverage <= 30.0) colorScheme.primary else colorScheme.error,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable Metric Tile for the Hero Card KPI Grid.
 */
@Composable
fun KpiStatTile(
    label: String,
    value: String,
    subvalue: String,
    icon: ImageVector,
    backdropState: Backdrop,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surface.copy(alpha = 0.65f))
            .padding(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(14.dp),
                )
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )
                Text(
                    text = subvalue,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 1.dp),
                )
            }
        }
    }
}

fun Double.toFixedTwoDecimals(): String {
    val scaled = round(this * 100.0).toLong()
    val magnitude = abs(scaled)
    val sign = if (scaled < 0) "-" else ""
    val fraction = (magnitude % 100).toString().padStart(2, '0')
    return "$sign${magnitude / 100}.$fraction"
}
