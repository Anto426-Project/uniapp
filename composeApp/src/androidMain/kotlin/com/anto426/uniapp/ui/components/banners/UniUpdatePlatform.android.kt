package com.anto426.uniapp.ui.components.banners

import android.os.Build
import android.util.Log

internal actual fun supportsUniAppRuntimeShader(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

internal actual fun logUniAppShaderError(message: String, error: Throwable) {
    Log.e("UniAppAgsl", message, error)
}
