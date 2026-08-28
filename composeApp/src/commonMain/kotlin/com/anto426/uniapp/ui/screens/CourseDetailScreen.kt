package com.anto426.uniapp.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import uniapp.composeapp.generated.resources.*
import com.anto426.uniapp.ui.components.layout.UniHeroCard
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.models.StudyCourse
import com.kyant.backdrop.Backdrop

@Composable
fun CourseDetailScreen(course: StudyCourse, backdropState: Backdrop) {
    val colorScheme = MaterialTheme.colorScheme

    UniScreenColumn {
        // 1. Course Hero
        UniHeroCard(
            backdropState = backdropState,
            eyebrow = course.cfu,
            title = course.name,
            subtitle = course.semester,
            icon = LiquidIcons.Star
        )

        // 2. Professor & Department
        LiquidPreferenceGroup(title = "Didattica", backdropState = backdropState) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_professor),
                subtitle = course.professor,
                icon = LiquidIcons.AccountCircle,
                backdropState = backdropState
            )
            LiquidHorizontalDivider()
            LiquidPreferenceItem(
                title = "Codice Corso",
                subtitle = "INF/01 - ${course.id}",
                icon = LiquidIcons.Info,
                backdropState = backdropState
            )
        }

        // 3. Description Section
        LiquidPreferenceGroup(title = "Descrizione", backdropState = backdropState) {
            Text(
                text = course.description,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(16.dp),
                lineHeight = 22.sp
            )
        }

        // 4. Status Group
        LiquidPreferenceGroup(title = "Stato Carriera", backdropState = backdropState) {
            val statusLabel = course.status.name.lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

            LiquidPreferenceItem(
                title = "Status",
                subtitle = statusLabel,
                icon = LiquidIcons.Check,
                backdropState = backdropState
            )
        }
    }
}
