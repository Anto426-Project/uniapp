package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoading
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoadingSize
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoadingStyle
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.AttendanceUiState
import com.anto426.uniapp.feedback.runtime.LocalAppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.ui.components.items.AttendanceItem
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniContentHost
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.layout.uniListEntranceOrder
import com.anto426.uniapp.ui.components.state.AppLoadingState
import com.anto426.uniapp.ui.components.state.UniExpressiveStatusState
import com.anto426.uniapp.ui.didactics.components.AttendanceSummaryHeroCard
import com.anto426.uniapp.ui.motion.UniMotion
import com.anto426.uniapp.ui.scanner.UniQrScannerDialog
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun AttendanceScreen(
    uiState: AttendanceUiState,
    showCameraScanner: Boolean = false,
    onOpenCameraScanner: () -> Unit = {},
    onDismissCameraScanner: () -> Unit = {},
    onRetry: () -> Unit = {},
    onRegisterAttendance: (String) -> Unit = {},
    onClearRegistrationStatus: () -> Unit = {},
) {
    val toastSink = LocalAppToastSink.current

    LaunchedEffect(uiState.registrationSuccessMessage) {
        val message = uiState.registrationSuccessMessage ?: return@LaunchedEffect
        toastSink.success(message)
        onClearRegistrationStatus()
    }

    LaunchedEffect(uiState.registrationErrorMessage) {
        val message = uiState.registrationErrorMessage ?: return@LaunchedEffect
        toastSink.error(message)
        onClearRegistrationStatus()
    }

    val screenPhase = when {
        uiState.loadState == FeatureLoadState.Loading && uiState.records.isEmpty() -> 0
        uiState.loadState == FeatureLoadState.Error && uiState.records.isEmpty() -> 1
        else -> 2
    }

    UniContentHost(
        targetState = screenPhase,
        modifier = Modifier.fillMaxSize(),
        transition = UniMotion.inlineContentTransition,
        suppressElementEntrance = false,
        contentAlignment = Alignment.TopStart,
        label = "AttendanceScreenPhase",
    ) { phase ->
        when (phase) {
            0 -> AppLoadingState()

            1 -> UniExpressiveStatusState(
                isError = true,
                title = stringResource(Res.string.ui_state_error_title),
                description = uiState.errorMessage ?: stringResource(Res.string.ui_state_error_desc),
                actionLabel = stringResource(Res.string.ui_retry),
                onActionClick = onRetry,
            )

            else -> {
                UniScreenColumn {
                    if (uiState.isRegistering) {
                        LiquidCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedRectangle(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                LiquidLoading(
                                    style = LiquidLoadingStyle.Dots,
                                    size = LiquidLoadingSize.Small,
                                )
                                Text(
                                    text = stringResource(Res.string.ui_attendance_dialog_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }

                    if (uiState.records.isEmpty()) {
                        UniExpressiveStatusState(
                            title = stringResource(Res.string.ui_attendance_empty_title),
                            description = stringResource(Res.string.ui_attendance_empty_desc),
                            mainIcon = LiquidIcons.QrCode,
                            actionLabel = stringResource(Res.string.ui_attendance_action_scan),
                            onActionClick = onOpenCameraScanner,
                            inline = true,
                        )
                    } else {
                        UniAnimatedEntrance(order = 0) {
                            AttendanceSummaryHeroCard(uiState = uiState)
                        }

                        UniAnimatedEntrance(order = 1) {
                            LiquidSectionHeader(
                                title = stringResource(Res.string.ui_attendance_title),
                                subtitle = stringResource(Res.string.ui_attendance_subtitle),
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            uiState.records.forEachIndexed { index, data ->
                                key(data.course, index) {
                                    UniAnimatedEntrance(order = uniListEntranceOrder(index, offset = 1)) {
                                        AttendanceItem(data)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    UniQrScannerDialog(
        visible = showCameraScanner,
        title = stringResource(Res.string.ui_attendance_scanner_scan_qr),
        subtitle = stringResource(Res.string.ui_attendance_scanner_explainer),
        onScanned = onRegisterAttendance,
        onDismiss = onDismissCameraScanner,
        onManualInput = null,
    )
}
