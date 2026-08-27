package com.anto426.uniapp.android.ui.components.items

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.feedback.LiquidLinearProgressIndicator
import com.anto426.uniapp.android.ui.models.AttendanceData
import com.kyant.backdrop.Backdrop

@Composable
fun AttendanceItem(data: AttendanceData, backdropState: Backdrop) {
    val colorScheme = MaterialTheme.colorScheme
    val progress = try { data.percentage.replace("%", "").toFloat() / 100f } catch (e: Exception) { 0f }

    LiquidCard(
        backdropState = backdropState,
        shape = RoundedCornerShape(22.dp),
        contentPadding = 16.dp,
        interactiveGelatin = false
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        text = "Frequenza: ${data.count} lezioni",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                LiquidBadge(text = data.percentage, backdropState = backdropState)
            }

            LiquidLinearProgressIndicator(
                progress = progress,
                backdropState = backdropState
            )
        }
    }
}
