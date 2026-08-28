package com.anto426.uniapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTabBar
import com.anto426.liquidmonet.components.selection.LiquidStepper
import com.anto426.liquidmonet.components.selection.LiquidSwitch
import com.anto426.liquidmonet.icons.LiquidIcons
import uniapp.composeapp.generated.resources.*
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.liquidmonet.components.display.LiquidSectionTitle
import com.anto426.uniapp.ui.data.UiInitialData
import com.anto426.uniapp.ui.models.GradeExam
import com.anto426.uniapp.ui.models.GradeSimulationPreset
import com.kyant.backdrop.Backdrop
import kotlin.math.abs
import kotlin.math.round

@Composable
fun GradesScreen(backdropState: Backdrop) {
    val currentExams = UiInitialData.currentGradeExams
    val simulationData = UiInitialData.gradeSimulation

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    // Simulation state: using 0 to represent "Not included"
    var math2Grade by remember { mutableIntStateOf(0) }
    var physicsGrade by remember { mutableIntStateOf(0) }
    var osGrade by remember { mutableIntStateOf(0) }

    // Calculations
    val totalCfuCurrent = currentExams.sumOf { it.cfu }
    val weightedSumCurrent = currentExams.sumOf { it.grade * it.cfu }
    val currentAverage = if (totalCfuCurrent > 0) weightedSumCurrent.toDouble() / totalCfuCurrent else 0.0

    // Simulation logic
    val activeSimulationExams = mutableListOf<Pair<Int, Int>>() // grade to cfu
    if (math2Grade >= 18) activeSimulationExams.add(math2Grade to simulationData[0].cfu)
    if (physicsGrade >= 18) activeSimulationExams.add(physicsGrade to simulationData[1].cfu)
    if (osGrade >= 18) activeSimulationExams.add(osGrade to simulationData[2].cfu)

    val totalCfuSim = activeSimulationExams.sumOf { it.second }
    val weightedSumSim = activeSimulationExams.sumOf { it.first * it.second }

    val totalCfuTotal = totalCfuCurrent + totalCfuSim
    val projectedAverage = if (totalCfuTotal > 0) (weightedSumCurrent + weightedSumSim).toDouble() / totalCfuTotal else currentAverage

    val tabs = listOf(
        LiquidNavigationItem(stringResource(Res.string.ui_grades_current_tab), icon = LiquidIcons.Star),
        LiquidNavigationItem(stringResource(Res.string.ui_grades_simulation), icon = LiquidIcons.Edit)
    )

    UniScreenColumn {
        LiquidTabBar(
            items = tabs,
            selectedIndex = selectedTabIndex,
            onTabSelected = { selectedTabIndex = it },
            backdropState = backdropState,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        AnimatedContent(
            targetState = selectedTabIndex,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "GradesTabContent"
        ) { targetTab ->
            when (targetTab) {
                0 -> CurrentGradesContent(
                    currentAverage = currentAverage,
                    totalCfu = totalCfuCurrent,
                    exams = currentExams,
                    backdropState = backdropState
                )
                1 -> SimulationContent(
                    currentAverage = currentAverage,
                    projectedAverage = projectedAverage,
                    simulationData = simulationData,
                    math2Grade = math2Grade,
                    physicsGrade = physicsGrade,
                    osGrade = osGrade,
                    onMath2Change = { math2Grade = it },
                    onPhysicsChange = { physicsGrade = it },
                    onOsChange = { osGrade = it },
                    backdropState = backdropState
                )
            }
        }
    }
}

@Composable
private fun CurrentGradesContent(
    currentAverage: Double,
    totalCfu: Int,
    exams: List<GradeExam>,
    backdropState: Backdrop
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        LiquidCard(backdropState = backdropState, shape = RoundedCornerShape(28.dp), contentPadding = 20.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = stringResource(Res.string.ui_grades_current),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(currentAverage.toFixedTwoDecimals(), fontSize = 48.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, letterSpacing = (-1).sp, lineHeight = 48.sp)
                    Column(horizontalAlignment = Alignment.End) {
                        Text("$totalCfu CFU", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(stringResource(Res.string.ui_achieved), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        LiquidSectionTitle(
            title = stringResource(Res.string.ui_grades_taken),
            subtitle = stringResource(Res.string.ui_grades_taken_subtitle)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            exams.forEach { exam ->
                GradeListItem(exam, backdropState)
            }
        }
    }
}

@Composable
private fun SimulationContent(
    currentAverage: Double,
    projectedAverage: Double,
    simulationData: List<GradeSimulationPreset>,
    math2Grade: Int,
    physicsGrade: Int,
    osGrade: Int,
    onMath2Change: (Int) -> Unit,
    onPhysicsChange: (Int) -> Unit,
    onOsChange: (Int) -> Unit,
    backdropState: Backdrop
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        val difference = projectedAverage - currentAverage
        val differenceText = if (difference >= 0) "+${difference.toFixedTwoDecimals()}" else difference.toFixedTwoDecimals()
        val differenceColor = if (difference >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

        LiquidCard(
            backdropState = backdropState,
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            shape = RoundedCornerShape(28.dp),
            contentPadding = 20.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = stringResource(Res.string.ui_grades_projected),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp
                )
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(projectedAverage.toFixedTwoDecimals(), fontSize = 48.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, letterSpacing = (-1).sp)
                    if (difference != 0.0) {
                        Box(Modifier.clip(RoundedCornerShape(14.dp)).background(differenceColor.copy(alpha = 0.2f)).padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(differenceText, color = differenceColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        LiquidSectionTitle(
            title = stringResource(Res.string.ui_grades_simulation),
            subtitle = stringResource(Res.string.ui_grades_simulation_subtitle)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            simulationData.forEachIndexed { index, preset ->
                val currentVal = when(index) {
                    0 -> math2Grade
                    1 -> physicsGrade
                    else -> osGrade
                }
                val setter: (Int) -> Unit = when(index) {
                    0 -> onMath2Change
                    1 -> onPhysicsChange
                    else -> onOsChange
                }

                SimulationItem(
                    label = preset.name,
                    cfu = preset.cfu,
                    grade = currentVal,
                    onGradeChange = setter,
                    backdropState = backdropState
                )
            }
        }
    }
}

private fun Double.toFixedTwoDecimals(): String {
    val scaled = round(this * 100.0).toLong()
    val magnitude = abs(scaled)
    val sign = if (scaled < 0) "-" else ""
    return "$sign${magnitude / 100}.${(magnitude % 100).toString().padStart(2, '0')}"
}

@Composable
private fun SimulationItem(
    label: String,
    cfu: Int,
    grade: Int,
    onGradeChange: (Int) -> Unit,
    backdropState: Backdrop
) {
    val isActive = grade >= 18
    val colorScheme = MaterialTheme.colorScheme

    LiquidCard(
        backdropState = backdropState,
        shape = RoundedCornerShape(24.dp),
        contentPadding = 16.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("$cfu CFU", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (isActive) "Incluso" else "Escluso",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isActive) colorScheme.primary else colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    LiquidSwitch(
                        checked = isActive,
                        onCheckedChange = { active ->
                            onGradeChange(if (active) 18 else 0)
                        },
                        backdropState = backdropState
                    )
                }
            }

            if (isActive) {
                // Stepper without the "Voto" unit to avoid visual bugs in the small box
                LiquidStepper(
                    value = grade,
                    onValueChange = onGradeChange,
                    minValue = 18,
                    maxValue = 30,
                    backdropState = backdropState
                )
            }
        }
    }
}

@Composable
private fun GradeListItem(
    exam: GradeExam,
    backdropState: Backdrop
) {
    LiquidCard(backdropState = backdropState, shape = RoundedCornerShape(18.dp), contentPadding = 16.dp) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(exam.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("${exam.cfu} CFU", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = exam.grade.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
