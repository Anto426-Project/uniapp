package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.feedback.dialog.LiquidDialog
import com.anto426.liquidmonet.components.inputs.textfield.LiquidTextField
import com.anto426.liquidmonet.components.interaction.liquidClickable
import com.anto426.liquidmonet.components.selection.slider.LiquidSlider
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.data.contract.toFixedTwoDecimals
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

internal val quickExamGradePresets = listOf(18, 24, 27, 30, 31)
internal val quickScenarioGradePresets = listOf(24, 27, 30, 31)
internal val quickTargetDegreePresets = listOf(100, 105, 108, 110, 111)

internal fun gradeMetric(value: Double): String = if (value > 0.0) value.toFixedTwoDecimals() else "—"

internal fun signedGradeMetric(value: Double): String =
    if (value > 0.0) "+${value.toFixedTwoDecimals()}" else value.toFixedTwoDecimals()

internal fun simulationGradeLabel(value: Int): String = if (value >= 31) "30L" else value.toString()

internal fun graduationGradeLabel(value: Int): String = if (value >= 111) "110L" else value.toString()

internal fun projectWeightedAveragePreview(
    baseAverage: Double,
    baseCfu: Int,
    addedCfu: Int,
    addedGrade: Int,
): Double {
    val gradeToUse = addedGrade.coerceAtMost(30)
    if (baseCfu <= 0 || baseAverage <= 0.0) return gradeToUse.toDouble()
    val totalCfu = baseCfu + addedCfu
    return if (totalCfu <= 0) baseAverage else ((baseAverage * baseCfu) + (gradeToUse * addedCfu)) / totalCfu
}

/**
 * Shared compact metric column used in Grades, AcademicSection, CourseDetail, and Library hero cards.
 */
@Composable
internal fun GradesMetricTile(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    unit: String? = null,
    prominent: Boolean = false,
) {
    val typography = MaterialTheme.typography
    val scheme = MaterialTheme.colorScheme
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = typography.labelSmall,
            color = scheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = buildAnnotatedString {
                append(value)
                if (unit != null && value != "—") {
                    withStyle(SpanStyle(fontSize = typography.labelMedium.fontSize, color = scheme.onSurfaceVariant)) {
                        append(" $unit")
                    }
                }
            },
            style = (if (prominent) typography.headlineMedium else typography.titleMedium).copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp,
                fontFeatureSettings = "tnum",
            ),
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun GradesCompactStat(
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp,
                fontFeatureSettings = "tnum",
            ),
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun GradesVerticalHairline(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(1.dp)
            .height(28.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f)),
    )
}

/**
 * Direct-manipulation 1:1 slider control with discrete haptic tick feedback.
 */
@Composable
internal fun GradesSliderParameter(
    label: String,
    formattedValue: String,
    value: Int,
    minValue: Int,
    maxValue: Int,
    onValueChange: (Int) -> Unit,
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
    val colors = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val safeMin = minValue.toFloat()
    val safeMax = maxValue.coerceAtLeast(minValue + 1).toFloat()
    val clamped = value.coerceIn(minValue, maxValue)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = formattedValue,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.3).sp,
                    fontFeatureSettings = "tnum",
                ),
                color = accentColor,
                maxLines = 1,
            )
        }
        LiquidSlider(
            value = clamped.toFloat(),
            onValueChange = { raw ->
                val stepped = raw.roundToInt().coerceIn(minValue, maxValue)
                if (stepped != clamped) {
                    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    onValueChange(stepped)
                }
            },
            valueRange = safeMin..safeMax,
            tint = accentColor,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Compact 1-tap preset pill strip for direct numeric selection (grades or graduation targets).
 */
@Composable
internal fun GradesPresetStrip(
    values: List<Int>,
    selectedValue: Int,
    labelFor: (Int) -> String,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val pillShape = remember { Capsule() }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        values.forEach { preset ->
            key(preset) {
                val isSelected = preset == selectedValue
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(pillShape)
                        .background(
                            color = if (isSelected) colors.primary else colors.onSurface.copy(alpha = 0.06f),
                            shape = pillShape,
                        )
                        .liquidClickable(
                            shape = pillShape,
                            role = Role.RadioButton,
                            onClick = {
                                if (!isSelected) {
                                    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    onSelect(preset)
                                }
                            },
                        )
                        .semantics { selected = isSelected }
                        .padding(horizontal = 4.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = labelFor(preset),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum",
                        ),
                        color = if (isSelected) colors.onPrimary else colors.onSurface,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/**
 * Dialog for adding a custom hypothetical exam to the simulator with live impact preview.
 */
@Composable
fun AddCustomExamDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int) -> Unit,
    baseAverage: Double = 0.0,
    baseCfu: Int = 0,
    baseGraduation: Double = baseAverage * 110.0 / 30.0,
) {
    val colors = MaterialTheme.colorScheme
    val glass = LiquidGlassTheme.colors
    val previewShape = remember { RoundedRectangle(18.dp) }
    var name by rememberSaveable { mutableStateOf("") }
    var cfu by rememberSaveable { mutableIntStateOf(6) }
    var grade by rememberSaveable { mutableIntStateOf(28) }

    val defaultName = stringResource(Res.string.ui_grades_custom_exam_default)
    val nameLabel = stringResource(Res.string.ui_grades_custom_exam_name)
    val previewAverage = projectWeightedAveragePreview(baseAverage, baseCfu, cfu, grade)
    val delta = previewAverage - baseAverage
    val previewBase = baseGraduation + delta * 110.0 / 30.0

    LiquidDialog(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.ui_grades_add_custom_exam_title),
        modifier = Modifier.imePadding(),
        confirmButton = {
            LiquidButton(
                text = stringResource(Res.string.ui_add),
                onClick = { onConfirm(name.trim().ifBlank { defaultName }, cfu, grade) },
                variant = LiquidButtonVariant.Primary,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        dismissButton = {
            LiquidButton(
                text = stringResource(Res.string.ui_cancel),
                onClick = onDismiss,
                variant = LiquidButtonVariant.Text,
                size = LiquidButtonSize.Medium,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            LiquidTextField(
                value = name,
                onValueChange = { name = it },
                label = nameLabel,
                placeholder = stringResource(Res.string.ui_grades_custom_exam_name_placeholder),
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = nameLabel },
            )
            GradesSliderParameter(
                label = stringResource(Res.string.ui_grades_custom_exam_cfu),
                formattedValue = "$cfu CFU",
                value = cfu,
                minValue = 1,
                maxValue = 30,
                onValueChange = { cfu = it },
                accentColor = colors.tertiary,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GradesSliderParameter(
                    label = stringResource(Res.string.ui_grades_custom_exam_grade),
                    formattedValue = simulationGradeLabel(grade),
                    value = grade,
                    minValue = 18,
                    maxValue = 31,
                    onValueChange = { grade = it },
                    accentColor = colors.primary,
                )
                GradesPresetStrip(
                    values = quickExamGradePresets,
                    selectedValue = grade,
                    labelFor = ::simulationGradeLabel,
                    onSelect = { grade = it },
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(previewShape)
                    .background(colors.onSurface.copy(alpha = 0.04f), previewShape)
                    .border(1.dp, colors.onSurface.copy(alpha = 0.08f), previewShape)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    GradesMetricTile(
                        label = stringResource(Res.string.ui_grades_simulation_estimated),
                        value = gradeMetric(previewAverage),
                        valueColor = colors.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    GradesMetricTile(
                        label = stringResource(Res.string.ui_grades_projected_degree_base),
                        value = gradeMetric(previewBase),
                        unit = "/110",
                        valueColor = colors.primary,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (baseCfu > 0 && baseAverage > 0.0) {
                    Text(
                        text = stringResource(Res.string.ui_grades_simulation_delta, signedGradeMetric(delta)),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFeatureSettings = "tnum",
                        ),
                        color = if (delta >= 0.0) glass.success else colors.error,
                    )
                }
            }
        }
    }
}
