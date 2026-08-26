package com.anto426.uniapp.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.anto426.uniapp.ui.home.AcademicStatsSummary
import com.anto426.uniapp.ui.theme.UniColors

@Composable
fun QuickStatsGrid(
    stats: AcademicStatsSummary,
    modifier: Modifier = Modifier,
    onExamsClick: (() -> Unit)? = null,
    onTaxesClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Appelli Aperti Card
        GlassCard(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(26.dp),
            backgroundColor = Color(0x33261238),
            borderBrush = UniColors.GlassBorderGradient,
            contentPadding = 18.dp,
            onClick = onExamsClick
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(UniColors.PrimaryMagenta.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "📝", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${stats.openExamRoundsCount}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = UniColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "APPELLI APERTI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = UniColors.PrimaryMagenta
                )
            }
        }

        // Tasse Aperte Card
        GlassCard(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(26.dp),
            backgroundColor = Color(0x2B1A132C),
            borderBrush = UniColors.GlassBorderGradient,
            contentPadding = 18.dp,
            onClick = onTaxesClick
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(UniColors.AccentCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "💳", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${stats.unpaidTaxesCount}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = UniColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "TASSE APERTE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = UniColors.AccentCyan
                )
            }
        }
    }
}
