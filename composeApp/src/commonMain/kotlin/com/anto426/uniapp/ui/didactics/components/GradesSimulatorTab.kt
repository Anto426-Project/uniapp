package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.feedback.progressbar.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.components.interaction.liquidClickable
import com.anto426.liquidmonet.components.layout.expandablecontent.LiquidExpandableContent
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.components.selection.switch.LiquidSwitch
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.data.contract.SimulationItem
import com.anto426.uniapp.didactics.presentation.GradesUiState
import com.anto426.uniapp.ui.components.display.UniBlockDefaults
import com.anto426.uniapp.ui.components.display.UniCircularProgressRing
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.layout.UniHorizontalChipRow
import com.anto426.uniapp.ui.components.state.UniExpressiveStatusState
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun GradesSimulatorTab(
    uiState: GradesUiState,
    onToggleItem: (String) -> Unit,
    onGradeChange: (String, Int) -> Unit,
    onCfuChange: (String, Int) -> Unit,
    onAddCustomExam: (String, Int, Int) -> Unit,
    onRemoveCustomExam: (String) -> Unit,
    onSetAllGrades: (Int) -> Unit,
    onApplyCurrentAverage: () -> Unit,
    onResetSimulation: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val glass = LiquidGlassTheme.colors
    val haptics = LocalHapticFeedback.current
    val pillShape = remember { Capsule() }
    var isAddDialogOpen by rememberSaveable { mutableStateOf(false) }
    var focusedItemId by rememberSaveable { mutableStateOf<String?>(null) }

    val hasScenario = uiState.activeSimulatedCount > 0
    val deltaAvg = uiState.deltaAverage
    val deltaBase = uiState.deltaGraduationBase
    val accent = when {
        !hasScenario -> colors.primary
        deltaAvg >= 0.0 -> glass.success
        else -> colors.error
    }
    val degreeProgress = (uiState.projectedGraduationBase / 110.0).toFloat().coerceIn(0f, 1f)

    Column(
        modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // 1. Live Impact Dashboard Card
        UniInsetGroup(
            contentPadding = PaddingValues(18.dp),
            animateEntrance = false,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(
                            if (hasScenario) Res.string.ui_grades_simulation_result else Res.string.ui_grades_current,
                        ).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.7.sp,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (hasScenario) {
                        Row(
                            modifier = Modifier
                                .clip(pillShape)
                                .background(colors.onSurface.copy(alpha = 0.07f), pillShape)
                                .liquidClickable(
                                    shape = pillShape,
                                    role = Role.Button,
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                        focusedItemId = null
                                        onResetSimulation()
                                    },
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = LiquidIcons.Refresh,
                                contentDescription = null,
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                text = stringResource(Res.string.ui_transport_clear_selection),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = gradeMetric(uiState.projectedAverage),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontSize = 38.sp,
                                    lineHeight = 42.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-1.4).sp,
                                    fontFeatureSettings = "tnum",
                                ),
                                color = colors.onSurface,
                                maxLines = 1,
                            )
                            Text(
                                text = "/ 30",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 5.dp),
                                maxLines = 1,
                            )
                        }

                        if (hasScenario) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                LiquidBadge(
                                    text = stringResource(Res.string.ui_grades_simulation_delta, signedGradeMetric(deltaAvg)),
                                    containerColor = accent.copy(alpha = 0.14f),
                                    contentColor = accent,
                                )
                                LiquidBadge(
                                    text = "${signedGradeMetric(deltaBase)} pt",
                                    containerColor = colors.primary.copy(alpha = 0.13f),
                                    contentColor = colors.primary,
                                )
                            }
                            if (uiState.currentAverage > 0.0) {
                                Text(
                                    text = "${stringResource(Res.string.ui_grades_current)}: ${gradeMetric(uiState.currentAverage)} → ${gradeMetric(uiState.projectedAverage)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                    color = colors.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(Res.string.ui_grades_simulation_to_simulate_sub),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    UniCircularProgressRing(
                        progress = degreeProgress,
                        centerText = gradeMetric(uiState.projectedGraduationBase),
                        subtitleText = "/110",
                        size = 78.dp,
                        strokeWidth = 6.dp,
                        primaryColor = accent,
                        secondaryColor = colors.primary,
                    )
                }

                LiquidHorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (hasScenario) {
                                stringResource(
                                    Res.string.ui_grades_simulation_summary,
                                    uiState.activeSimulatedCount,
                                    uiState.activeSimulatedCfu,
                                )
                            } else {
                                stringResource(Res.string.ui_stats_study_plan_progress)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${uiState.totalProjectedCfu}/${uiState.degreeCfuTarget} CFU",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum",
                            ),
                            color = accent,
                            maxLines = 1,
                        )
                    }
                    LiquidLinearProgressIndicator(
                        progress = uiState.careerProgress,
                        progressColor = if (hasScenario) accent else null,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // 2. Quick Presets + Individual LiquidCard Exam List
        val activeLabel = stringResource(
            if (uiState.activeSimulatedCount == 1) Res.string.ui_grades_active_singular
            else Res.string.ui_grades_active_plural,
            uiState.activeSimulatedCount,
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_grades_simulation_to_simulate),
                subtitle = if (uiState.simulationItems.isNotEmpty()) {
                    stringResource(
                        Res.string.ui_grades_exams_to_simulate_subtitle,
                        uiState.simulationItems.size,
                        activeLabel,
                    )
                } else {
                    null
                },
            )

            if (uiState.simulationItems.isNotEmpty()) {
                val allEnabledSameGrade = if (uiState.simulationItems.all { it.isEnabled }) {
                    uiState.simulationItems.map { it.grade }.distinct().singleOrNull()
                } else {
                    null
                }

                UniHorizontalChipRow {
                    if (uiState.currentAverage > 0.0) {
                        LiquidChip(
                            label = stringResource(Res.string.ui_grades_current),
                            selected = false,
                            leadingIcon = LiquidIcons.Analytics,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                onApplyCurrentAverage()
                            },
                        )
                    }
                    quickScenarioGradePresets.forEach { presetGrade ->
                        key(presetGrade) {
                            val isPresetSelected = allEnabledSameGrade == presetGrade
                            LiquidChip(
                                label = simulationGradeLabel(presetGrade),
                                selected = isPresetSelected,
                                leadingIcon = if (isPresetSelected) LiquidIcons.Check else null,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    onSetAllGrades(presetGrade)
                                },
                            )
                        }
                    }
                }
            }

            if (uiState.simulationItems.isEmpty()) {
                UniExpressiveStatusState(
                    title = stringResource(Res.string.ui_grades_no_residual_exams),
                    description = stringResource(Res.string.ui_grades_simulation_empty),
                    actionLabel = stringResource(Res.string.ui_grades_add_custom_exam_title),
                    onActionClick = { isAddDialogOpen = true },
                    inline = true,
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    uiState.simulationItems.forEach { item ->
                        key(item.id) {
                            val isEditorOpen = item.isEnabled && focusedItemId == item.id
                            SimulatorExamCard(
                                item = item,
                                impact = uiState.simulationItemImpact(item),
                                hasCurrentCfu = uiState.totalCurrentCfu > 0,
                                isEditorOpen = isEditorOpen,
                                onCardClick = {
                                    if (!item.isEnabled) {
                                        onToggleItem(item.id)
                                        focusedItemId = item.id
                                    } else {
                                        focusedItemId = if (focusedItemId == item.id) null else item.id
                                    }
                                },
                                onSwitchToggle = {
                                    val willEnable = !item.isEnabled
                                    onToggleItem(item.id)
                                    focusedItemId = if (willEnable) item.id else focusedItemId.takeUnless { it == item.id }
                                },
                                onGradeChange = { onGradeChange(item.id, it) },
                                onCfuChange = { onCfuChange(item.id, it) },
                                onRemove = if (item.isCustom) {
                                    {
                                        if (focusedItemId == item.id) focusedItemId = null
                                        onRemoveCustomExam(item.id)
                                    }
                                } else {
                                    null
                                },
                            )
                        }
                    }

                    LiquidButton(
                        text = stringResource(Res.string.ui_grades_add_custom_exam_title),
                        onClick = { isAddDialogOpen = true },
                        leadingIcon = {
                            Icon(
                                imageVector = LiquidIcons.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        variant = LiquidButtonVariant.Tonal,
                        size = LiquidButtonSize.Medium,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }

    if (isAddDialogOpen) {
        AddCustomExamDialog(
            onDismiss = { isAddDialogOpen = false },
            onConfirm = { name, cfu, grade ->
                onAddCustomExam(name, cfu, grade)
                isAddDialogOpen = false
            },
            baseAverage = uiState.projectedAverage,
            baseCfu = uiState.totalCurrentCfu + uiState.activeSimulatedCfu,
            baseGraduation = uiState.projectedGraduationBase,
        )
    }
}

@Composable
private fun SimulatorExamCard(
    item: SimulationItem,
    impact: Double,
    hasCurrentCfu: Boolean,
    isEditorOpen: Boolean,
    onCardClick: () -> Unit,
    onSwitchToggle: () -> Unit,
    onGradeChange: (Int) -> Unit,
    onCfuChange: (Int) -> Unit,
    onRemove: (() -> Unit)?,
) {
    val colors = MaterialTheme.colorScheme
    val glass = LiquidGlassTheme.colors
    val cardShape = remember { RoundedRectangle(UniBlockDefaults.CornerRadius) }
    val iconShape = remember { RoundedRectangle(UniBlockDefaults.IconCornerRadius) }
    val included = item.isEnabled
    val impactColor = if (impact >= 0.0) glass.success else colors.error
    val iconTint = when {
        item.isCustom -> colors.tertiary
        included -> colors.primary
        else -> colors.onSurfaceVariant
    }

    LiquidCard(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(clip = false),
        shape = cardShape,
        contentPadding = PaddingValues(0.dp),
        clipContent = false,
    ) {
        Column(modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = UniBlockDefaults.RowMinHeight)
                    .liquidClickable(shape = cardShape, role = Role.Button, onClick = onCardClick)
                    .semantics(mergeDescendants = true) {}
                    .padding(
                        horizontal = UniBlockDefaults.RowHorizontalPadding,
                        vertical = UniBlockDefaults.RowVerticalPadding,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    imageVector = when {
                        item.isCustom -> LiquidIcons.Star
                        included -> LiquidIcons.Check
                        else -> LiquidIcons.MenuBook
                    },
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.liquidIconContainer(
                        containerSize = UniBlockDefaults.IconContainerSize,
                        iconSize = UniBlockDefaults.IconSize,
                        containerColor = iconTint.copy(alpha = 0.14f),
                        shape = iconShape,
                    ),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 15.sp,
                        fontWeight = if (included) FontWeight.Bold else FontWeight.SemiBold,
                        letterSpacing = (-0.15).sp,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${item.cfu} CFU",
                            style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                        )
                        if (included && hasCurrentCfu) {
                            Text(
                                text = "·",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant.copy(alpha = 0.5f),
                            )
                            Text(
                                text = signedGradeMetric(impact),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFeatureSettings = "tnum",
                                ),
                                color = impactColor,
                                maxLines = 1,
                            )
                        }
                    }
                }

                if (included) {
                    LiquidBadge(
                        text = simulationGradeLabel(item.grade),
                        containerColor = colors.primary.copy(alpha = if (isEditorOpen) 0.24f else 0.14f),
                        contentColor = colors.primary,
                    )
                }

                LiquidSwitch(
                    checked = included,
                    onCheckedChange = { onSwitchToggle() },
                )
            }

            LiquidExpandableContent(visible = isEditorOpen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LiquidHorizontalDivider()

                    GradesSliderParameter(
                        label = stringResource(Res.string.ui_grades_custom_exam_grade),
                        formattedValue = simulationGradeLabel(item.grade),
                        value = item.grade,
                        minValue = 18,
                        maxValue = 31,
                        onValueChange = onGradeChange,
                        accentColor = colors.primary,
                    )

                    GradesPresetStrip(
                        values = quickExamGradePresets,
                        selectedValue = item.grade,
                        labelFor = ::simulationGradeLabel,
                        onSelect = onGradeChange,
                    )

                    if (item.isCustom) {
                        LiquidHorizontalDivider()
                        GradesSliderParameter(
                            label = stringResource(Res.string.ui_grades_custom_exam_cfu),
                            formattedValue = "${item.cfu} CFU",
                            value = item.cfu,
                            minValue = 1,
                            maxValue = 30,
                            onValueChange = onCfuChange,
                            accentColor = colors.tertiary,
                        )
                    }

                    if (onRemove != null) {
                        LiquidButton(
                            text = stringResource(Res.string.ui_grades_remove_custom_exam),
                            onClick = onRemove,
                            containerColor = colors.error.copy(alpha = 0.12f),
                            contentColor = colors.error,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            leadingIcon = { Icon(LiquidIcons.Delete, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
