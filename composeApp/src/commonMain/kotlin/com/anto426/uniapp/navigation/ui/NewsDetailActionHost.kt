package com.anto426.uniapp.navigation.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal class NewsDetailActions(
    val onOpenOriginal: (() -> Unit)?,
    val onShare: () -> Unit,
)

internal class NewsDetailActionHost {
    var actions by mutableStateOf<NewsDetailActions?>(null)
        private set

    fun register(actions: NewsDetailActions) {
        this.actions = actions
    }

    fun unregister(actions: NewsDetailActions) {
        if (this.actions === actions) this.actions = null
    }
}

internal val NewsOpenInNewIcon: ImageVector by lazy {
    ImageVector.Builder("OpenInNew", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Black)) {
            moveTo(19.0f, 19.0f)
            horizontalLineTo(5.0f)
            verticalLineTo(5.0f)
            horizontalLineToRelative(7.0f)
            verticalLineTo(3.0f)
            horizontalLineTo(5.0f)
            curveToRelative(-1.11f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
            verticalLineToRelative(14.0f)
            curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
            horizontalLineToRelative(14.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
            verticalLineToRelative(-7.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(7.0f)
            close()
            moveTo(14.0f, 3.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(3.59f)
            lineToRelative(-9.83f, 9.83f)
            lineToRelative(1.41f, 1.41f)
            lineTo(19.0f, 6.41f)
            verticalLineTo(10.0f)
            horizontalLineToRelative(2.0f)
            verticalLineTo(3.0f)
            horizontalLineToRelative(-7.0f)
            close()
        }.build()
}
