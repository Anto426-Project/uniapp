package com.anto426.uniapp.android.ui.screens

import com.anto426.uniapp.android.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.android.ui.components.layout.UniHeroCard
import com.anto426.liquidmonet.components.display.LiquidSectionTitle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.feedback.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.android.ui.components.items.DidacticItem
import com.anto426.uniapp.android.ui.components.items.DidacticRow
import com.anto426.uniapp.android.ui.data.UiCopy
import com.kyant.backdrop.Backdrop

@Composable
fun DidacticsScreen(
    backdropState: Backdrop,
    onOpenCareer: () -> Unit = {},
    onOpenTaxes: () -> Unit = {},
    onOpenGrades: () -> Unit = {},
    onOpenTranscripts: () -> Unit = {},
    onOpenExams: () -> Unit = {},
    onOpenQuestionnaires: () -> Unit = {},
    onOpenBadge: () -> Unit = {},
    onOpenAttendance: () -> Unit = {},
    onOpenStudyPlan: () -> Unit = {}
) {
    UniScreenColumn {
        // 1. Career Progress Hero (Renamed from Study Plan)
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(28.dp),
            contentPadding = 20.dp,
            onClick = onOpenCareer
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.ui_didactics_career),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = stringResource(R.string.ui_degree),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LiquidBadge(text = "9%", backdropState = backdropState)
                }

                LiquidLinearProgressIndicator(
                    progress = 0.09f,
                    backdropState = backdropState
                )

                Text(
                    text = stringResource(R.string.ui_cfu_progress, "12", "180"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 3. Main Actions Grid
        LiquidSectionTitle(title = stringResource(R.string.ui_didactics_title), subtitle = stringResource(R.string.ui_didactics_subtitle))

        Column(
            modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DidacticRow(
                item1 = {
                    DidacticItem(
                        stringResource(R.string.ui_exams),
                        stringResource(R.string.ui_exams_subtitle),
                        LiquidIcons.Calendar,
                        backdropState,
                        badgeCount = 2,
                        onClick = onOpenExams
                    )
                },
                item2 = {
                    DidacticItem(
                        stringResource(R.string.ui_average),
                        stringResource(R.string.ui_average_subtitle),
                        LiquidIcons.Star,
                        backdropState,
                        onClick = onOpenGrades
                    )
                }
            )

            DidacticRow(
                item1 = {
                    DidacticItem(
                        stringResource(R.string.ui_taxes),
                        stringResource(R.string.ui_taxes_subtitle),
                        LiquidIcons.Warning,
                        backdropState,
                        iconColor = MaterialTheme.colorScheme.error,
                        onClick = onOpenTaxes
                    )
                },
                item2 = {
                    DidacticItem(
                        stringResource(R.string.ui_questionnaires),
                        stringResource(R.string.ui_questionnaires_subtitle),
                        LiquidIcons.Edit,
                        backdropState,
                        onClick = onOpenQuestionnaires
                    )
                }
            )

            DidacticRow(
                item1 = {
                    DidacticItem(
                        stringResource(R.string.ui_badge),
                        stringResource(R.string.ui_badge_subtitle),
                        LiquidIcons.AccountCircle,
                        backdropState,
                        onClick = onOpenBadge
                    )
                },
                item2 = {
                    DidacticItem(
                        stringResource(R.string.ui_attendance),
                        stringResource(R.string.ui_attendance_desc),
                        LiquidIcons.Check,
                        backdropState,
                        onClick = onOpenAttendance
                    )
                }
            )

            DidacticRow(
                item1 = {
                    DidacticItem(
                        title = stringResource(R.string.ui_transcript),
                        subtitle = stringResource(R.string.ui_transcript_subtitle),
                        icon = LiquidIcons.Calendar,
                        backdropState = backdropState,
                        onClick = onOpenTranscripts
                    )
                },
                item2 = {
                    DidacticItem(
                        title = stringResource(R.string.ui_study_plan),
                        subtitle = stringResource(R.string.ui_study_plan_subtitle),
                        icon = LiquidIcons.Edit,
                        backdropState = backdropState,
                        onClick = onOpenStudyPlan
                    )
                }
            )
        }
    }
}
