package com.anto426.uniapp.ui.components.account

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

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
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.anto426.liquidmonet.components.display.LiquidAvatar

internal val LocalAccountAvatars = androidx.compose.runtime.staticCompositionLocalOf<Map<String, com.anto426.uniapp.account.data.AccountAvatar>> { emptyMap() }

/** Displays the account photo when available and preserves the SDK initials fallback. */
@Composable
fun UniAccountAvatar(
    imageData: ByteArray?,
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    contentDescription: String? = null,
    imageCacheKey: String? = null,
) {
    if (imageData == null || imageData.isEmpty()) {
        LiquidAvatar(
            modifier = modifier,
            initials = initials.ifBlank { stringResource(Res.string.msg_un) },
            size = size,
        )
        return
    }

    val cacheKey = imageCacheKey ?: LocalAccountAvatars.current.values.firstOrNull { it.bytes === imageData }?.cacheKey
    val platformContext = LocalPlatformContext.current
    val request =
        androidx.compose.runtime.remember(imageData, platformContext, cacheKey) {
            ImageRequest.Builder(platformContext)
                .data(imageData)
                .memoryCacheKey(cacheKey)
                .size(256, 256)
                .diskCachePolicy(CachePolicy.DISABLED)
                .networkCachePolicy(CachePolicy.DISABLED)
                .build()
        }
    val painter = rememberAsyncImagePainter(request)
    val painterState by painter.state.collectAsState()
    val imageLoaded = painterState is AsyncImagePainter.State.Success

    LiquidAvatar(
        modifier = modifier,
        initials = if (imageLoaded) null else initials.ifBlank { stringResource(Res.string.msg_un) },
        icon = null,
        size = size,
        content =
            if (imageLoaded) {
                {
                    Image(
                        painter = painter,
                        contentDescription = contentDescription,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                }
            } else {
                null
            },
    )
}
