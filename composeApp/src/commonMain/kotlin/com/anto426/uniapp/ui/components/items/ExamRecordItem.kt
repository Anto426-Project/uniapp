package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.didactics.ExamRecord
import com.kyant.backdrop.Backdrop

@Composable
fun ExamRecordItem(exam: ExamRecord, backdropState: Backdrop) {
    val colorScheme = MaterialTheme.colorScheme

    LiquidCard(
        backdropState = backdropState,
        shape = RoundedCornerShape(20.dp),
        contentPadding = 16.dp,
        interactiveGelatin = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LiquidIcons.Check,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = exam.name,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        letterSpacing = (-0.2).sp
                    )
                    Text(
                        text = buildString {
                            if (exam.code.isNotBlank()) append("${exam.code} • ")
                            append("${exam.date} • ${exam.cfu}")
                        },
                        color = colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(contentAlignment = Alignment.Center) {
                LiquidBadge(
                    text = exam.grade,
                    containerColor = colorScheme.primaryContainer,
                    contentColor = colorScheme.primary,
                    backdropState = backdropState,
                    modifier = Modifier.graphicsLayer {
                        scaleX = 1.05f
                        scaleY = 1.05f
                    }
                )
            }
        }
    }
}
