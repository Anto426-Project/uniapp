package com.anto426.uniapp.ui.settings.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal object ThemeIcons {
    val Sparkles: ImageVector by lazy {
        ImageVector.Builder(
            name = "Sparkles",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(9.5f, 3f)
                lineTo(11.4f, 8.6f)
                lineTo(17f, 10.5f)
                lineTo(11.4f, 12.4f)
                lineTo(9.5f, 18f)
                lineTo(7.6f, 12.4f)
                lineTo(2f, 10.5f)
                lineTo(7.6f, 8.6f)
                close()
                moveTo(18.5f, 2f)
                lineTo(19.6f, 4.9f)
                lineTo(22.5f, 6f)
                lineTo(19.6f, 7.1f)
                lineTo(18.5f, 10f)
                lineTo(17.4f, 7.1f)
                lineTo(14.5f, 6f)
                lineTo(17.4f, 4.9f)
                close()
                moveTo(18.5f, 14f)
                lineTo(19.4f, 16.6f)
                lineTo(22f, 17.5f)
                lineTo(19.4f, 18.4f)
                lineTo(18.5f, 21f)
                lineTo(17.6f, 18.4f)
                lineTo(15f, 17.5f)
                lineTo(17.6f, 16.6f)
                close()
            }
        }.build()
    }

    val AutoMode: ImageVector by lazy {
        ImageVector.Builder(
            name = "AutoMode",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.EvenOdd,
            ) {
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
                curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
                close()
                moveTo(12f, 4f)
                lineTo(12f, 20f)
                curveTo(7.58f, 20f, 4f, 16.42f, 4f, 12f)
                curveTo(4f, 7.58f, 7.58f, 4f, 12f, 4f)
                close()
            }
        }.build()
    }

    val LightMode: ImageVector by lazy {
        ImageVector.Builder(
            name = "LightMode",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 7f)
                curveTo(9.24f, 7f, 7f, 9.24f, 7f, 12f)
                curveTo(7f, 14.76f, 9.24f, 17f, 12f, 17f)
                curveTo(14.76f, 17f, 17f, 14.76f, 17f, 12f)
                curveTo(17f, 9.24f, 14.76f, 7f, 12f, 7f)
                close()
                moveTo(11f, 2f); lineTo(13f, 2f); lineTo(13f, 5f); lineTo(11f, 5f); close()
                moveTo(11f, 19f); lineTo(13f, 19f); lineTo(13f, 22f); lineTo(11f, 22f); close()
                moveTo(2f, 11f); lineTo(5f, 11f); lineTo(5f, 13f); lineTo(2f, 13f); close()
                moveTo(19f, 11f); lineTo(22f, 11f); lineTo(22f, 13f); lineTo(19f, 13f); close()
                moveTo(4.93f, 6.34f); lineTo(6.34f, 4.93f); lineTo(8.46f, 7.05f); lineTo(7.05f, 8.46f); close()
                moveTo(15.54f, 16.95f); lineTo(16.95f, 15.54f); lineTo(19.07f, 17.66f); lineTo(17.66f, 19.07f); close()
                moveTo(17.66f, 4.93f); lineTo(19.07f, 6.34f); lineTo(16.95f, 8.46f); lineTo(15.54f, 7.05f); close()
                moveTo(7.05f, 15.54f); lineTo(8.46f, 16.95f); lineTo(6.34f, 19.07f); lineTo(4.93f, 17.66f); close()
            }
        }.build()
    }

    val DarkMode: ImageVector by lazy {
        ImageVector.Builder(
            name = "DarkMode",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12.3f, 3f)
                curveTo(10.64f, 4.58f, 9.75f, 6.91f, 10.12f, 9.39f)
                curveTo(10.67f, 13.06f, 13.68f, 15.86f, 17.39f, 16.15f)
                curveTo(18.78f, 16.26f, 20.1f, 15.96f, 21.22f, 15.36f)
                curveTo(19.86f, 18.69f, 16.59f, 21.02f, 12.78f, 21f)
                curveTo(7.69f, 20.98f, 3.63f, 16.75f, 3.85f, 11.67f)
                curveTo(4.06f, 6.89f, 7.77f, 3.16f, 12.3f, 3f)
                close()
            }
        }.build()
    }

    val Wallpaper: ImageVector by lazy {
        ImageVector.Builder(
            name = "Wallpaper",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.EvenOdd,
            ) {
                moveTo(5f, 3f); lineTo(19f, 3f)
                curveTo(20.1f, 3f, 21f, 3.9f, 21f, 5f)
                lineTo(21f, 19f)
                curveTo(21f, 20.1f, 20.1f, 21f, 19f, 21f)
                lineTo(5f, 21f)
                curveTo(3.9f, 21f, 3f, 20.1f, 3f, 19f)
                lineTo(3f, 5f)
                curveTo(3f, 3.9f, 3.9f, 3f, 5f, 3f)
                close()
                moveTo(6f, 17f); lineTo(18f, 17f); lineTo(14.2f, 12f); lineTo(11.2f, 15.8f); lineTo(9.1f, 13.2f); close()
                moveTo(8.5f, 7f)
                curveTo(7.67f, 7f, 7f, 7.67f, 7f, 8.5f)
                curveTo(7f, 9.33f, 7.67f, 10f, 8.5f, 10f)
                curveTo(9.33f, 10f, 10f, 9.33f, 10f, 8.5f)
                curveTo(10f, 7.67f, 9.33f, 7f, 8.5f, 7f)
                close()
            }
        }.build()
    }

    val SlowMotion: ImageVector by lazy {
        ImageVector.Builder(
            name = "SlowMotion",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(4f, 7f); lineTo(14f, 7f); lineTo(14f, 9f); lineTo(4f, 9f); close()
                moveTo(16f, 6f); lineTo(18f, 6f); lineTo(18f, 10f); lineTo(16f, 10f); close()
                moveTo(19f, 7f); lineTo(21f, 7f); lineTo(21f, 9f); lineTo(19f, 9f); close()
                moveTo(4f, 15f); lineTo(8f, 15f); lineTo(8f, 17f); lineTo(4f, 17f); close()
                moveTo(9f, 14f); lineTo(11f, 14f); lineTo(11f, 18f); lineTo(9f, 18f); close()
                moveTo(12f, 15f); lineTo(21f, 15f); lineTo(21f, 17f); lineTo(12f, 17f); close()
            }
        }.build()
    }

    val PageTransition: ImageVector by lazy {
        ImageVector.Builder(
            name = "PageTransition",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(4f, 6f); lineTo(12f, 6f)
                curveTo(13.1f, 6f, 14f, 6.9f, 14f, 8f)
                lineTo(14f, 18f)
                curveTo(14f, 19.1f, 13.1f, 20f, 12f, 20f)
                lineTo(4f, 20f)
                curveTo(2.9f, 20f, 2f, 19.1f, 2f, 18f)
                lineTo(2f, 8f)
                curveTo(2f, 6.9f, 2.9f, 6f, 4f, 6f)
                close()
                moveTo(16f, 4f); lineTo(20f, 4f)
                curveTo(21.1f, 4f, 22f, 4.9f, 22f, 6f)
                lineTo(22f, 16f)
                curveTo(22f, 17.1f, 21.1f, 18f, 20f, 18f)
                lineTo(16f, 18f)
                close()
            }
        }.build()
    }

    val NavigationBar: ImageVector by lazy {
        ImageVector.Builder(
            name = "NavigationBar",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.EvenOdd,
            ) {
                moveTo(4f, 3f); lineTo(20f, 3f)
                curveTo(21.1f, 3f, 22f, 3.9f, 22f, 5f)
                lineTo(22f, 19f)
                curveTo(22f, 20.1f, 21.1f, 21f, 20f, 21f)
                lineTo(4f, 21f)
                curveTo(2.9f, 21f, 2f, 20.1f, 2f, 19f)
                lineTo(2f, 5f)
                curveTo(2f, 3.9f, 2.9f, 3f, 4f, 3f)
                close()
                moveTo(4f, 5f); lineTo(4f, 15f); lineTo(20f, 15f); lineTo(20f, 5f); close()
                moveTo(6f, 17.2f); lineTo(10f, 17.2f); lineTo(10f, 18.8f); lineTo(6f, 18.8f); close()
                moveTo(14f, 17.2f); lineTo(18f, 17.2f); lineTo(18f, 18.8f); lineTo(14f, 18.8f); close()
            }
        }.build()
    }

    val ElementEntrance: ImageVector by lazy {
        ImageVector.Builder(
            name = "ElementEntrance",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(4f, 5f); lineTo(20f, 5f); lineTo(20f, 8f); lineTo(4f, 8f); close()
                moveTo(4f, 10.5f); lineTo(17f, 10.5f); lineTo(17f, 13.5f); lineTo(4f, 13.5f); close()
                moveTo(4f, 16f); lineTo(14f, 16f); lineTo(14f, 19f); lineTo(4f, 19f); close()
            }
        }.build()
    }
}
