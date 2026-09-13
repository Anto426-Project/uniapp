package com.anto426.uniapp.ui.transport.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.uniapp.codes.*
import com.anto426.uniapp.transport.presentation.TicketImageUiState
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun TicketCodeImage(code: DecodedCode?, modifier: Modifier = Modifier) {
    val painter = remember(code) { code?.let { runCatching { CodeGenerator().generate(it) }.getOrNull() } }
    Box(modifier.background(Color.White), contentAlignment = Alignment.Center) {
        if (painter != null) Image(painter, stringResource(Res.string.ui_ticket_digital_code), Modifier.fillMaxSize())
        else Text(stringResource(Res.string.ui_ticket_use_original), Modifier.padding(12.dp), color = Color.Black, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
internal fun TransportTicketActions(state: TicketImageUiState, onReload: () -> Unit) {
    var originalVisible by remember(state.original) { mutableStateOf(false) }
    var digitalVisible by remember(state.original) { mutableStateOf(false) }
    val reproducible = remember(state.codes) {
        state.codes.filter { runCatching { CodeGenerator().generate(it) }.isSuccess }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (state.isLoading || state.isReading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text(stringResource(if (state.isLoading) Res.string.ui_ticket_downloading else Res.string.ui_ticket_reading))
        }
        state.errorMessage?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        if (state.original != null) {
            LiquidButton(text = stringResource(Res.string.ui_ticket_show_original), onClick = { originalVisible = true }, modifier = Modifier.fillMaxWidth())
            if (reproducible.isNotEmpty()) {
                LiquidButton(text = stringResource(Res.string.ui_ticket_show_digital), onClick = { digitalVisible = true }, modifier = Modifier.fillMaxWidth(), variant = LiquidButtonVariant.Primary)
            } else if (!state.isReading && state.errorMessage == null) {
                Text(stringResource(Res.string.ui_ticket_use_original), style = MaterialTheme.typography.bodyMedium)
            }
            Text(stringResource(Res.string.ui_ticket_original_saved), style = MaterialTheme.typography.bodySmall)
        }
        TextButton(onClick = onReload, enabled = !state.isLoading && !state.isReading) {
            Text(stringResource(Res.string.ui_ticket_reload))
        }
    }
    if (originalVisible && state.original != null) TicketViewer(onDismiss = { originalVisible = false }) {
        var scale by remember { mutableFloatStateOf(1f) }
        var translation by remember { mutableStateOf(Offset.Zero) }
        Box(Modifier.fillMaxSize().clipToBounds().pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                scale = (scale * zoom).coerceIn(1f, 5f)
                val limitX = size.width * (scale - 1f) / 2f
                val limitY = size.height * (scale - 1f) / 2f
                translation = Offset((translation.x + pan.x).coerceIn(-limitX, limitX), (translation.y + pan.y).coerceIn(-limitY, limitY))
            }
        }) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current).data(state.original)
                    .memoryCachePolicy(CachePolicy.DISABLED).diskCachePolicy(CachePolicy.DISABLED).build(),
                contentDescription = stringResource(Res.string.ui_ticket_original_image),
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    scaleX = scale; scaleY = scale
                    translationX = translation.x; translationY = translation.y
                },
            )
        }
    }
    if (digitalVisible) TicketViewer(onDismiss = { digitalVisible = false }) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            for (code in state.codes) {
                if (code in reproducible) TicketCodeImage(code, Modifier.fillMaxWidth().then(
                    if (code.format == CodeFormat.Qr) Modifier.aspectRatio(1f) else Modifier.height(170.dp),
                )) else Text(stringResource(Res.string.ui_ticket_use_original), color = Color.Black)
            }
        }
    }
}

@Composable
private fun TicketViewer(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(Modifier.fillMaxSize().background(Color.White).safeDrawingPadding()) {
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(Res.string.ui_ticket_close), color = Color.Black)
            }
            Box(Modifier.weight(1f)) { content() }
        }
    }
}
