package com.anto426.uniapp.ui.home.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.uniapp.model.home.dashboard.CareerOverviewSummary
import com.anto426.uniapp.ui.theme.UniColors

@Composable
fun HeroCareerCard(
    career: CareerOverviewSummary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = career.progressPercentage,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "cfuProgress"
    )

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        backgroundBrush = UniColors.CareerHeroGradient,
        borderBrush = Brush.linearGradient(
            colors = listOf(
                Color(0x99FF007F),
                Color(0x33C084FC),
                Color(0x2210B981)
            )
        ),
        borderWidth = 1.5.dp,
        contentPadding = 24.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PANORAMICA CARRIERA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    color = UniColors.PrimaryMagenta
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33FF007F))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${(career.progressPercentage * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = UniColors.TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Big Grade / Base Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = career.degreeBase110,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = UniColors.TextPrimary,
                        letterSpacing = (-1).sp
                    )

                    Text(
                        text = "Base di Laurea • Media ${career.weightedAverage}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = UniColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Controlla media, progressione e strumenti principali del tuo percorso.",
                fontSize = 13.sp,
                color = UniColors.TextSecondary.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Animated Smooth Progress Bar with Glowing Accent
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x33FFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(UniColors.ProgressBarGradient)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer CFU Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CFU ACQUISITI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = UniColors.TextSecondary
                )

                Text(
                    text = "${career.cfuAcquired} / ${career.cfuTarget}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = UniColors.PrimaryMagenta
                )
            }
        }
    }
}
