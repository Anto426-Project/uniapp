package com.anto426.uniapp.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.anto426.uniapp.android.R
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.feedback.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.android.ui.components.layout.UniScreenColumn
import com.anto426.liquidmonet.components.display.LiquidSectionTitle
import com.anto426.uniapp.android.ui.components.items.ExamRecordItem
import com.anto426.uniapp.android.ui.data.UiInitialData
import com.kyant.backdrop.Backdrop

@Composable
fun TranscriptsScreen(backdropState: Backdrop) {
    val exams = UiInitialData.transcripts

    UniScreenColumn {
        // 1. Career Summary Hero
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(28.dp),
            contentPadding = 20.dp,
            interactiveGelatin = false // Static as requested
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "${stringResource(R.string.ui_student_name)} • ${stringResource(R.string.ui_matricola_prefix, "123456")}",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.ui_current_average),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "27.8 / 30",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    LiquidCard(
                        modifier = Modifier.size(52.dp),
                        backdropState = backdropState,
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = 0.dp,
                        onClick = {}, // Interactive icon area
                        interactiveGelatin = true,
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = LiquidIcons.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp).align(Alignment.Center)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.ui_acquired_cfu),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "36 / 180",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    LiquidLinearProgressIndicator(
                        progress = 0.2f,
                        backdropState = backdropState
                    )
                }
            }
        }

        LiquidSectionTitle(title = stringResource(R.string.ui_registered_exams), subtitle = stringResource(R.string.ui_registered_exams_subtitle))

        Column(
            modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            exams.forEach { exam ->
                ExamRecordItem(exam, backdropState)
            }
        }
    }
}
