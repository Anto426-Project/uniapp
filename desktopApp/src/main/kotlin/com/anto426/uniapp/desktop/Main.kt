package com.anto426.uniapp.desktop
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.anto426.uniapp.DesktopBackDispatcher
import com.anto426.uniapp.UniApp
import java.awt.Dimension
fun main() = application {
    Window(
        title = "UniApp",
        icon = painterResource("icon.png"),
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(width = 1100.dp, height = 800.dp),
        onPreviewKeyEvent = DesktopBackDispatcher::handle,
    ) {
        window.minimumSize = Dimension(420, 640)
        UniApp()
    }
}
