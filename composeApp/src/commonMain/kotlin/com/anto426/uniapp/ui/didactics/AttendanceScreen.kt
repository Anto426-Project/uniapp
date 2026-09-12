package com.anto426.uniapp.ui.didactics

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
import androidx.compose.foundation.layout.width
import com.kyant.shapes.RoundedRectangle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.anto426.uniapp.ui.scanner.UniQrScannerDialog
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun AttendanceScreen(
    uiState: AttendanceUiState,
    onRegisterAttendance: (String) -> Unit = {},
    onClearRegistrationStatus: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    var showCodeDialog by remember { mutableStateOf(false) }
    var showCameraScanner by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.registrationSuccessMessage) {
        if (uiState.registrationSuccessMessage != null) {
            showCodeDialog = false
            showCameraScanner = false
        }
    }

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
                    verticalAlignment = Alignment.CenterVertically,
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

                // Action Buttons (full width so text is never truncated)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LiquidButton(
                        onClick = {
                            onClearRegistrationStatus()
                            showCameraScanner = true
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
                            maxLines = 1,
                        )
                    }

                    LiquidButton(
                        onClick = {
                            onClearRegistrationStatus()
                            showCodeDialog = true
                        },
                        variant = LiquidButtonVariant.Glass,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = LiquidIcons.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.ui_attendance_scanner_manual_entry),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
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

        Spacer(modifier = Modifier.height(110.dp))
    }

    // ==========================================
    // 4. SCANNER & CODE REGISTRATION DIALOGS
    // ==========================================
    // 4a. Official LiquidDialog from SDK for manual code registration
    if (showCodeDialog) {
        var qrInput by remember { mutableStateOf("") }

        LiquidDialog(
            onDismissRequest = { showCodeDialog = false },
            title = stringResource(Res.string.ui_attendance_dialog_title),
            confirmButton = {
                LiquidButton(
                    text = stringResource(Res.string.ui_attendance_dialog_confirm),
                    onClick = {
                        val trimmed = qrInput.trim()
                        if (trimmed.isNotBlank()) {
                            onRegisterAttendance(trimmed)
                        }
                    },
                    variant = LiquidButtonVariant.Primary,
                    isLoading = uiState.isRegistering,
                    enabled = qrInput.isNotBlank() && !uiState.isRegistering,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            dismissButton = {
                LiquidButton(
                    text = stringResource(Res.string.ui_attendance_dialog_close),
                    onClick = { showCodeDialog = false },
                    variant = LiquidButtonVariant.Glass,
                    enabled = !uiState.isRegistering,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.ui_attendance_dialog_input_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                )

                LiquidTextField(
                    value = qrInput,
                    onValueChange = { qrInput = it },
                    label = stringResource(Res.string.ui_attendance_dialog_input_label),
                    placeholder = stringResource(Res.string.ui_attendance_dialog_input_placeholder),
                    enabled = !uiState.isRegistering,
                )

                LiquidButton(
                    onClick = {
                        showCodeDialog = false
                        showCameraScanner = true
                    },
                    variant = LiquidButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = LiquidIcons.QrCode,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(Res.string.ui_attendance_scanner_open_camera),
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                if (uiState.registrationErrorMessage != null) {
                    Text(
                        text = uiState.registrationErrorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }

    // 4b. Full-Screen Camera QR Scanner
    UniQrScannerDialog(
        visible = showCameraScanner,
        title = stringResource(Res.string.ui_attendance_scanner_scan_qr),
        subtitle = stringResource(Res.string.ui_attendance_scanner_explainer),
        onScanned = { code ->
            showCameraScanner = false
            onRegisterAttendance(code)
        },
        onDismiss = { showCameraScanner = false },
        onManualInput = {
            showCameraScanner = false
            showCodeDialog = true
        },
    )
}

