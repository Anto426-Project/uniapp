package com.anto426.uniapp.ui.components.items

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.Role
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.data.contract.ExamRecord
import com.anto426.uniapp.ui.components.display.UniInsetRow

@Composable
fun ExamRecordItem(
    exam: ExamRecord,
    showDivider: Boolean = false,
    onClick: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val hasDetails = exam.code.isNotBlank()
    val accentColor = colorScheme.primary

    val metadata = listOfNotNull(
        exam.cfu.takeIf { it.isNotBlank() },
        exam.date.takeIf { it.isNotBlank() && it != "-" },
        exam.code.takeIf { it.isNotBlank() },
    ).joinToString(" • ").ifBlank { null }

    UniInsetRow(
        title = exam.name,
        subtitle = metadata,
        icon = if (exam.lode) LiquidIcons.Star else LiquidIcons.Check,
        iconTint = accentColor,
        onClick = if (hasDetails) onClick else null,
        clickRole = if (hasDetails) Role.Button else null,
        showDivider = showDivider,
        showTrailingChevron = hasDetails,
        trailingContent = {
            LiquidBadge(
                text = exam.grade,
                containerColor = accentColor.copy(alpha = 0.14f),
                contentColor = accentColor,
            )
        },
    )
}
