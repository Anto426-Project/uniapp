package com.anto426.uniapp.ui.components.display

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.uniapp.data.images.ApplicationImageStore
import com.anto426.uniapp.ui.components.account.UniAccountAvatar

internal val LocalApplicationImages = staticCompositionLocalOf<ApplicationImageStore?> { null }

/** Public avatars use the same renderer, with bytes owned by application storage. */
@Composable
internal fun UniRemoteAvatar(imageUrl: String?, name: String, size: Dp = 48.dp) {
    val store = LocalApplicationImages.current
    val image by produceState<com.anto426.uniapp.data.images.ApplicationImage?>(null, store, imageUrl) {
        value = null
        if (store != null && !imageUrl.isNullOrBlank()) value = store.load(imageUrl)
    }
    UniAccountAvatar(imageData = image?.bytes, initials = name.take(2).uppercase(), size = size,
        imageCacheKey = image?.cacheKey)
}
