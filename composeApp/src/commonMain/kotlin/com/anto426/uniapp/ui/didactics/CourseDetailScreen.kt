package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.model.didactics.StudyCourse
import com.kyant.backdrop.Backdrop

@Composable
fun CourseDetailScreen(course: StudyCourse, backdropState: Backdrop) {
    val colorScheme = MaterialTheme.colorScheme

    UniScreenColumn {
        // 1. Course Header Card
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(24.dp),
            contentPadding = 18.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LiquidIcons.Star,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = course.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        LiquidBadge(
                            text = course.cfu,
                            containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                            contentColor = colorScheme.primary,
                            backdropState = backdropState
                        )
                    }
                    Text(
                        text = course.semester,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }

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
