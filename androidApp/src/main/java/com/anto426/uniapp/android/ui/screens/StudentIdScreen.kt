package com.anto426.uniapp.android.ui.screens

import com.anto426.uniapp.android.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.android.ui.components.layout.UniHeroCard
import com.anto426.liquidmonet.components.display.LiquidSectionTitle

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.anto426.uniapp.android.R
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidAvatar
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.icons.LiquidIcons
import com.kyant.backdrop.Backdrop
import com.anto426.uniapp.android.ui.components.items.BadgeDetailRow

@Composable
fun StudentIdScreen(backdropState: Backdrop) {
    UniScreenColumn {
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(32.dp),
            contentPadding = 24.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LiquidBadge(
                        text = stringResource(R.string.ui_student_card),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        backdropState = backdropState
                    )
                    Text(
                        text = stringResource(R.string.ui_student_id_year),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Profile Section
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LiquidAvatar(initials = "AN", size = 110.dp, backdropState = backdropState)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.ui_student_name),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = stringResource(R.string.ui_matricola_prefix, "123456"),
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

                // Info Grid
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BadgeDetailRow(label = stringResource(R.string.ui_degree_label), value = stringResource(R.string.ui_degree), icon = LiquidIcons.Calendar)
                    BadgeDetailRow(label = stringResource(R.string.ui_department_label), value = stringResource(R.string.ui_department), icon = LiquidIcons.Info)

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            BadgeDetailRow(label = stringResource(R.string.ui_status_label), value = stringResource(R.string.ui_student_status), icon = LiquidIcons.Check)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            BadgeDetailRow(label = stringResource(R.string.ui_deadline_label), value = "31/12/2026", icon = LiquidIcons.Time)
                        }
                    }
                }

                // QR Code Section with improved visual
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .liquidGlass(
                                backdrop = backdropState,
                                shape = RoundedCornerShape(28.dp),
                                role = LiquidGlassRole.Surface,
                                containerColor = Color.White.copy(alpha = 0.08f)
                            )
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Mock QR Visual
                        Icon(
                            imageVector = LiquidIcons.Search, // Using Search as a base for QR-like look
                            contentDescription = "QR Code Placeholder",
                            modifier = Modifier
                                .size(120.dp)
                                .graphicsLayer {
                                    alpha = 0.8f
                                },
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = stringResource(R.string.ui_verified_validity),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
