package com.anto426.uniapp.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.stringResource
import com.anto426.uniapp.android.R
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.android.ui.components.layout.UniScreenColumn
import com.anto426.liquidmonet.components.display.LiquidSectionTitle
import com.anto426.uniapp.android.ui.components.items.AttendanceItem
import com.anto426.uniapp.android.ui.data.UiInitialData
import com.anto426.uniapp.android.ui.data.UiCopy
import com.kyant.backdrop.Backdrop

@Composable
fun AttendanceScreen(backdropState: Backdrop) {
    val attendance = UiInitialData.attendance

    UniScreenColumn {
        // 1. Universal Scanner Hero
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.ui_scanner_eyebrow),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.ui_scanner_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.ui_scanner_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                    LiquidCard(
                        modifier = Modifier.size(72.dp),
                        backdropState = backdropState,
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = 10.dp,
                        onClick = {}, // Interactive icon area
                        interactiveGelatin = true,
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        // Decorative QR-like pattern
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(Modifier.size(14.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                                Box(Modifier.size(14.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(2.dp)))
                                Box(Modifier.size(14.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(Modifier.size(14.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(2.dp)))
                                Box(Modifier.size(14.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                                Box(Modifier.size(14.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(2.dp)))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(Modifier.size(14.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                                Box(Modifier.size(14.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(2.dp)))
                                Box(Modifier.size(14.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                            }
                        }
                    }
                }

                LiquidButton(
                    text = stringResource(R.string.ui_start_scan),
                    onClick = {},
                    variant = LiquidButtonVariant.Glass,
                    backdropState = backdropState,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(LiquidIcons.Search, contentDescription = null) }
                )
            }
        }

        LiquidSectionTitle(title = stringResource(R.string.ui_attendance_title), subtitle = stringResource(R.string.ui_attendance_subtitle))

        Column(
            modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            attendance.forEach { data ->
                AttendanceItem(data, backdropState)
            }
        }
    }
}
