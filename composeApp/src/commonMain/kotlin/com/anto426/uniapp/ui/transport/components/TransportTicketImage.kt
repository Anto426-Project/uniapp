package com.anto426.uniapp.ui.transport.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.liquidIconContainer
import com.anto426.liquidmonet.components.feedback.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.components.feedback.LiquidSheet
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.codes.CodeFormat
import com.anto426.uniapp.codes.CodeGenerator
import com.anto426.uniapp.codes.DecodedCode
import com.anto426.uniapp.transport.presentation.TicketImageUiState
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun TicketCodeImage(code: DecodedCode?, modifier: Modifier = Modifier) {
    val painter = remember(code) { code?.let { runCatching { CodeGenerator().generate(it) }.getOrNull() } }
    Box(
        modifier = modifier
            .background(Color.White, shape = RoundedRectangle(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = stringResource(Res.string.ui_ticket_digital_code),
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = stringResource(Res.string.ui_ticket_use_original),
                modifier = Modifier.padding(12.dp),
                color = Color.Black,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
internal fun TransportTicketActions(
    state: TicketImageUiState,
    onReload: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    var originalVisible by remember(state.original) { mutableStateOf(false) }
    var digitalVisible by remember(state.original) { mutableStateOf(false) }
    val reproducible = remember(state.codes) {
        state.codes.filter { runCatching { CodeGenerator().generate(it) }.isSuccess }
    }

    LiquidCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedRectangle(22.dp),
        contentPadding = 18.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header Row: Icon + Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = LiquidIcons.QrCode,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.liquidIconContainer(
                        containerSize = 40.dp,
                        iconSize = 20.dp,
                        containerColor = colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedRectangle(12.dp),
                    ),
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(Res.string.ui_ticket_digital_code),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = if (state.original != null) {
                            stringResource(Res.string.ui_ticket_original_saved)
                        } else if (state.isLoading || state.isReading) {
                            stringResource(if (state.isLoading) Res.string.ui_ticket_downloading else Res.string.ui_ticket_reading)
                        } else {
                            stringResource(Res.string.ui_transport_ticket_title_badge)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }

            // Progress / Status indicator
            if (state.isLoading || state.isReading) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LiquidLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.error,
                )
            }

            // Action Buttons
            if (state.original != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (reproducible.isNotEmpty()) {
                        LiquidButton(
                            text = stringResource(Res.string.ui_ticket_show_digital),
                            onClick = { digitalVisible = true },
                            modifier = Modifier.fillMaxWidth(),
                            variant = LiquidButtonVariant.Primary,
                        )
                    }

                    LiquidButton(
                        text = stringResource(Res.string.ui_ticket_show_original),
                        onClick = { originalVisible = true },
                        modifier = Modifier.fillMaxWidth(),
                        variant = if (reproducible.isNotEmpty()) LiquidButtonVariant.Tonal else LiquidButtonVariant.Primary,
                    )
                }
            } else if (!state.isLoading && !state.isReading) {
                LiquidButton(
                    text = stringResource(if (state.errorMessage != null) Res.string.ui_ticket_reload else Res.string.ui_ticket_show_original),
                    onClick = onReload,
                    modifier = Modifier.fillMaxWidth(),
                    variant = LiquidButtonVariant.Primary,
                )
            }
        }
    }

    // Sheet per il Biglietto Originale
    if (originalVisible && state.original != null) {
        LiquidSheet(
            onDismissRequest = { originalVisible = false },
            title = stringResource(Res.string.ui_ticket_original_image),
            subtitle = stringResource(Res.string.ui_transport_ticket_title_badge),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                var scale by remember { mutableFloatStateOf(1f) }
                var translation by remember { mutableStateOf(Offset.Zero) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedRectangle(20.dp))
                        .background(colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .clipToBounds()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                val limitX = size.width * (scale - 1f) / 2f
                                val limitY = size.height * (scale - 1f) / 2f
                                translation = Offset(
                                    (translation.x + pan.x).coerceIn(-limitX, limitX),
                                    (translation.y + pan.y).coerceIn(-limitY, limitY),
                                )
                            }
                        },
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(state.original)
                            .memoryCachePolicy(CachePolicy.DISABLED)
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .build(),
                        contentDescription = stringResource(Res.string.ui_ticket_original_image),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = translation.x
                                translationY = translation.y
                            },
                    )
                }

                LiquidButton(
                    text = stringResource(Res.string.ui_ticket_close),
                    onClick = { originalVisible = false },
                    modifier = Modifier.fillMaxWidth(),
                    variant = LiquidButtonVariant.Tonal,
                )
            }
        }
    }

    // Sheet per i Codici Digitali QR
    if (digitalVisible) {
        LiquidSheet(
            onDismissRequest = { digitalVisible = false },
            title = stringResource(Res.string.ui_ticket_digital_code),
            subtitle = stringResource(Res.string.ui_transport_scan_turnstile_hint),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    for (code in state.codes) {
                        if (code in reproducible) {
                            TicketCodeImage(
                                code = code,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (code.format == CodeFormat.Qr) Modifier.aspectRatio(1f) else Modifier.height(180.dp),
                                    ),
                            )
                        } else {
                            Text(
                                text = stringResource(Res.string.ui_ticket_use_original),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                LiquidButton(
                    text = stringResource(Res.string.ui_ticket_close),
                    onClick = { digitalVisible = false },
                    modifier = Modifier.fillMaxWidth(),
                    variant = LiquidButtonVariant.Tonal,
                )
            }
        }
    }
}
