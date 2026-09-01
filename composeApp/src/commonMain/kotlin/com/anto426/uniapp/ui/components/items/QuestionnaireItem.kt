package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidIconBox
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.didactics.QuestionnaireData
import com.anto426.uniapp.model.didactics.QuestionnaireStatus
import com.kyant.backdrop.Backdrop

@Composable
fun QuestionnaireItem(
    data: QuestionnaireData,
    backdropState: Backdrop,
    onClick: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val isCompleted = data.status == QuestionnaireStatus.COMPLETED

    LiquidCard(
        backdropState = backdropState,
        shape = RoundedCornerShape(22.dp),
        contentPadding = 16.dp,
        onClick = if (data.status == QuestionnaireStatus.PENDING) onClick else null,
        interactiveGelatin = data.status == QuestionnaireStatus.PENDING,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.course,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    fontSize = 15.sp,
                    letterSpacing = (-0.3).sp
                )
                Text(
                    text = "${data.prof} • ${data.code}",
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (isCompleted) {
                LiquidIconBox(
                    icon = LiquidIcons.Check,
                    size = 40.dp,
                    iconSize = 20.dp,
                    containerColor = Color(0xFF00C853).copy(alpha = 0.12f),
                    iconTint = Color(0xFF00C853),
                    shape = RoundedCornerShape(12.dp),
                )
            } else if (data.status == QuestionnaireStatus.PENDING) {
                LiquidIconBox(
                    icon = LiquidIcons.Feedback,
                    size = 40.dp,
                    iconSize = 20.dp,
                    containerColor = colorScheme.primary.copy(alpha = 0.12f),
                    iconTint = colorScheme.primary,
                    shape = RoundedCornerShape(12.dp),
                )
            } else {
                LiquidIconBox(
                    icon = LiquidIcons.Lock,
                    size = 40.dp,
                    iconSize = 20.dp,
                    containerColor = colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    iconTint = colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                )
            }
        }
    }
}
