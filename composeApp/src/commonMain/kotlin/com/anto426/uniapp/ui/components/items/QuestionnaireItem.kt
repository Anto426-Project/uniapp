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
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.models.QuestionnaireData
import com.anto426.uniapp.ui.models.QuestionnaireStatus
import com.kyant.backdrop.Backdrop

@Composable
fun QuestionnaireItem(data: QuestionnaireData, backdropState: Backdrop) {
    val colorScheme = MaterialTheme.colorScheme
    val isCompleted = data.status == QuestionnaireStatus.COMPLETED

    LiquidCard(
        backdropState = backdropState,
        shape = RoundedCornerShape(22.dp),
        contentPadding = 16.dp,
        onClick = if (isCompleted) null else ({}),
        interactiveGelatin = !isCompleted
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
                Icon(
                    imageVector = LiquidIcons.Check,
                    contentDescription = null,
                    tint = Color(0xFF00C853),
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = LiquidIcons.Edit,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
