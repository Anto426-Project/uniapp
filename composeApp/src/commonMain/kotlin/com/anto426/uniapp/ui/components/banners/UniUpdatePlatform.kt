package com.anto426.uniapp.ui.components.banners

internal expect fun supportsUniAppRuntimeShader(): Boolean

internal expect fun logUniAppShaderError(message: String, error: Throwable)
