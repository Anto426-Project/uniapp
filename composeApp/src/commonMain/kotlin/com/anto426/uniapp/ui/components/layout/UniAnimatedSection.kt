package com.anto426.uniapp.ui.components.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.layout.sectionentrance.LiquidSectionEntrance

/** Reveals related content inside the existing scene, after it reaches the area between its bars. */
@Composable
fun UniAnimatedSection(
    modifier: Modifier = Modifier,
    order: Int = 0,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val padding = LocalUniScreenPadding.current
    val window = LocalWindowInfo.current.containerSize
    val viewport = with(LocalDensity.current) {
        Rect(
            left = 0f,
            top = padding.calculateTopPadding().toPx(),
            right = window.width.toFloat(),
            bottom = (window.height - padding.calculateBottomPadding().toPx()).coerceAtLeast(0f),
        )
    }
    LiquidSectionEntrance(
        modifier = modifier.fillMaxWidth(),
        delayMillis = order.coerceIn(0, 3) * 60,
        viewportBoundsInWindow = viewport,
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = verticalArrangement, content = content)
    }
}
