package com.anto426.uniapp.ui.components.display

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.anto426.liquidmonet.components.display.LiquidAvatar

/** The SDK owns the shape, material and fallback; Coil only supplies the public image. */
@Composable
internal fun UniRemoteAvatar(imageUrl: String?, name: String, size: Dp = 48.dp) {
    val painter = rememberAsyncImagePainter(imageUrl)
    val state by painter.state.collectAsState()
    val loaded = state is AsyncImagePainter.State.Success
    LiquidAvatar(
        initials = if (loaded) null else name.take(2).uppercase(),
        icon = null,
        size = size,
        content = if (loaded) {
            {
                Image(painter = painter, contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
            }
        } else null,
    )
}
