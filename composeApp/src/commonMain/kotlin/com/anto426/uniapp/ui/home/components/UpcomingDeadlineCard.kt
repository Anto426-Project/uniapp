package com.anto426.uniapp.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.uniapp.model.home.dashboard.UpcomingDeadlineSummary
import com.anto426.uniapp.ui.theme.UniColors

@Composable
fun UpcomingDeadlineCard(
    deadline: UpcomingDeadlineSummary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        backgroundColor = Color(0x28201530),
        borderBrush = UniColors.GlassBorderGradient,
        contentPadding = 18.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Pill
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFB74D)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💳",
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "PROSSIMA SCADENZA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = UniColors.AccentAmber
                )

                Text(
                    text = "${deadline.amountFormatted} entro ${deadline.deadlineFormatted}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = UniColors.TextPrimary,
                    modifier = Modifier.padding(top = 2.dp)
                )

                if (deadline.title.isNotEmpty()) {
                    Text(
                        text = deadline.title,
                        fontSize = 12.sp,
                        color = UniColors.TextSecondary,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
            }

            if (deadline.isUrgent) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(UniColors.StatusError.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Urgente",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = UniColors.StatusError
                    )
                }
            }
        }
    }
}
