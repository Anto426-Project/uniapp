package com.anto426.uniapp.ui.didactics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.kyant.shapes.RoundedRectangle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidCardDefaults
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidEmptyState
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.liquidmonet.components.display.liquidIconContainer
import com.anto426.liquidmonet.components.feedback.LiquidDialog
import com.anto426.liquidmonet.components.inputs.LiquidTextField
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.AttendanceUiState
import com.anto426.uniapp.ui.components.items.AttendanceItem
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.didactics.components.AttendanceKpiCard
import com.anto426.uniapp.ui.didactics.components.AttendanceQrScannerDialog
import com.kyant.shapes.Capsule
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun AttendanceScreen(
    uiState: AttendanceUiState,
    onRegisterAttendance: (String) -> Unit = {},
    onClearRegistrationStatus: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    var showScannerDialog by remember { mutableStateOf(false) }

    UniScreenColumn {
        // ==========================================
        // 1. UNIVERSAL QR SCANNER HERO CARD
        // ==========================================
        LiquidCard(
            shape = RoundedRectangle(28.dp),
            contentPadding = 20.dp,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            imageVector = LiquidIcons.QrCode,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.liquidIconContainer(
                                containerSize = 48.dp,
                                iconSize = 26.dp,
                                containerColor = colorScheme.primary.copy(alpha = 0.16f),
                                shape = RoundedRectangle(16.dp),
                            ),
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(Res.string.ui_attendance_scanner_eyebrow),
                                color = colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                fontSize = 10.sp,
                            )
                            Text(
                                text = stringResource(Res.string.ui_attendance_scanner_scan_qr),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = colorScheme.onSurface,
                            )
                        }
                    }

                    LiquidBadge(
                        text = stringResource(Res.string.ui_attendance_scanner_live_badge),
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.primary,
                    )
                }

                Text(
                    text = stringResource(Res.string.ui_attendance_scanner_explainer),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                )

                // Registration Feedback Alert Banner (if any)
                if (uiState.registrationSuccessMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedRectangle(14.dp))
                            .background(colorScheme.primaryContainer.copy(alpha = 0.6f))
                            .padding(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = LiquidIcons.Check,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = uiState.registrationSuccessMessage,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.primary,
                            )
                        }
                    }
                }

                if (uiState.registrationErrorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedRectangle(14.dp))
                            .background(colorScheme.errorContainer.copy(alpha = 0.6f))
                            .padding(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = LiquidIcons.Warning,
                                contentDescription = null,
                                tint = colorScheme.error,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = uiState.registrationErrorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.error,
                            )
                        }
                    }
                }

                LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LiquidButton(
                        onClick = {
                            onClearRegistrationStatus()
                            showScannerDialog = true
                        },
                        variant = LiquidButtonVariant.Primary,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = LiquidIcons.QrCode,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.ui_attendance_action_scan),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. ATTENDANCE KPI STATS BAR
        // ==========================================
        if (uiState.records.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AttendanceKpiCard(
                    label = stringResource(Res.string.ui_attendance_kpi_courses),
                    value = "${uiState.totalCoursesCount}",
                    subvalue = stringResource(Res.string.ui_attendance_kpi_courses_sub),
                    icon = LiquidIcons.MenuBook,
                    modifier = Modifier.weight(1f),
                )
                AttendanceKpiCard(
                    label = stringResource(Res.string.ui_attendance_kpi_average),
                    value = "${uiState.averageAttendancePercent}%",
                    subvalue = stringResource(Res.string.ui_attendance_kpi_average_sub),
                    icon = LiquidIcons.Analytics,
                    modifier = Modifier.weight(1f),
                )
                AttendanceKpiCard(
                    label = stringResource(Res.string.ui_attendance_kpi_lectures),
                    value = "${uiState.totalAttendedLectures}",
                    subvalue = stringResource(Res.string.ui_attendance_kpi_lectures_sub),
                    icon = LiquidIcons.Check,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // ==========================================
        // 3. ATTENDANCE HISTORY LIST
        // ==========================================
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_attendance_title),
            subtitle = stringResource(Res.string.ui_attendance_subtitle),
        )

        if (uiState.records.isEmpty()) {
            LiquidEmptyState(
                title = stringResource(Res.string.ui_attendance_empty_title),
                description = stringResource(Res.string.ui_attendance_empty_desc),
                icon = LiquidIcons.QrCode,
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                uiState.records.forEach { data ->
                    AttendanceItem(data)
                }
            }
        }
    }

    // ==========================================
    // 4. SCANNER & CODE REGISTRATION DIALOG
    // ==========================================
    if (showScannerDialog) {
        AttendanceQrScannerDialog(
            isRegistering = uiState.isRegistering,
            errorMessage = uiState.registrationErrorMessage,
            onDismiss = { showScannerDialog = false },
            onConfirmCode = { code ->
                onRegisterAttendance(code)
            },
        )
    }
}

