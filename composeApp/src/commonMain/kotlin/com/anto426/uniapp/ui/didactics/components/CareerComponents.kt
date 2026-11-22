package com.anto426.uniapp.ui.didactics.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.data.contract.CourseStatus
import com.anto426.uniapp.didactics.presentation.ALL_ACADEMIC_YEARS
import com.anto426.uniapp.ui.components.layout.UniHorizontalChipRow
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun CareerYearChipRow(
    years: List<Int>,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    includeAllChip: Boolean = false,
) {
    if (years.isEmpty()) return
    UniHorizontalChipRow {
        if (includeAllChip && years.size > 1) {
            key(ALL_ACADEMIC_YEARS) {
                val selected = selectedYear == ALL_ACADEMIC_YEARS
                LiquidChip(
                    label = stringResource(Res.string.ui_career_all_statuses),
                    selected = selected,
                    leadingIcon = if (selected) LiquidIcons.Check else null,
                    onClick = { onYearSelected(ALL_ACADEMIC_YEARS) },
                )
            }
        }
        years.forEach { year ->
            key(year) {
                val selected = year == selectedYear
                LiquidChip(
                    label = careerYearLabel(year),
                    selected = selected,
                    leadingIcon = if (selected) LiquidIcons.Check else null,
                    onClick = { onYearSelected(year) },
                )
            }
        }
    }
}

@Composable
internal fun CareerStatusFilterRow(
    selectedStatus: CourseStatus?,
    onStatusSelected: (CourseStatus?) -> Unit,
) {
    val options: List<Pair<CourseStatus?, String>> = listOf(
        null to stringResource(Res.string.ui_career_all_statuses),
        CourseStatus.ACTIVE to stringResource(Res.string.ui_career_to_complete),
        CourseStatus.COMPLETED to stringResource(Res.string.ui_status_completed),
    )
    UniHorizontalChipRow {
        options.forEach { (status, label) ->
            key(status) {
                val selected = selectedStatus == status
                LiquidChip(
                    label = label,
                    selected = selected,
                    leadingIcon = if (selected) LiquidIcons.Check else null,
                    onClick = { onStatusSelected(status) },
                )
            }
        }
    }
}

@Composable
internal fun careerYearLabel(year: Int): String = when (year) {
    0 -> stringResource(Res.string.ui_year_unspecified)
    1 -> stringResource(Res.string.ui_year_first)
    2 -> stringResource(Res.string.ui_year_second)
    3 -> stringResource(Res.string.ui_year_third)
    else -> stringResource(Res.string.ui_year_nth, year)
}
