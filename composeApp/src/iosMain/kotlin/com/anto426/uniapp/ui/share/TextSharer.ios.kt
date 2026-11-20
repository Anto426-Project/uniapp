package com.anto426.uniapp.ui.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

@Composable
actual fun rememberTextSharer(): (String) -> Unit = remember {
    { text ->
        val controller = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null,
        )
        var top = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (top?.presentedViewController != null) top = top.presentedViewController
        top?.presentViewController(controller, animated = true, completion = null)
    }
}
