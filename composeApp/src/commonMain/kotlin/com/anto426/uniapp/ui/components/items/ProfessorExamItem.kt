package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.data.contract.AcademicContentItem
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfessorExamItem(
    exam: AcademicContentItem,
    onClickDetail: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme

    val bookingsCount = exam.bookings.size.takeIf { it > 0 }
        ?: exam.detail?.let { d ->
            val match = Regex("""(\d+)\s*(?:prenotat|iscritt)""", RegexOption.IGNORE_CASE).find(d)
            match?.groupValues?.get(1)?.toIntOrNull()
        }

    val subtitle = listOfNotNull(
        exam.date?.takeIf(String::isNotBlank),
        exam.subtitle?.takeIf(String::isNotBlank)?.takeIf { sub -> !sub.equals(exam.date, ignoreCase = true) },
    ).joinToString(" • ").ifBlank {
        stringResource(Res.string.ui_professor_exam_session_fallback)
    }

    UniInsetGroup(animateEntrance = false) {
        UniInsetRow(
            title = exam.title,
            subtitle = subtitle,
            icon = LiquidIcons.Calendar,
            iconTint = colorScheme.primary,
            onClick = onClickDetail,
            showDivider = false,
            titleMaxLines = 2,
            subtitleMaxLines = 2,
            subtitleContent = if (!exam.code.isNullOrBlank() || bookingsCount != null) {
                {
                    FlowRow(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        exam.code?.takeIf(String::isNotBlank)?.let { code ->
                            LiquidBadge(
                                text = code,
                                containerColor = colorScheme.primary.copy(alpha = 0.14f),
                                contentColor = colorScheme.primary,
                            )
                        }
                        bookingsCount?.let { count ->
                            LiquidBadge(
                                text = stringResource(Res.string.ui_professor_registered_count, count),
                                containerColor = colorScheme.onSurface.copy(alpha = 0.06f),
                                contentColor = colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                null
            },
        )
    }
}
