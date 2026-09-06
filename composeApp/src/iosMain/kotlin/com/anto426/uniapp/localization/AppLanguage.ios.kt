package com.anto426.uniapp.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.Foundation.NSUserDefaults

/** Compose resources picks the new Apple language preference on the next application launch. */
@Composable
internal actual fun BindAppLanguage(languageCode: String) {
    LaunchedEffect(languageCode) {
        if (languageCode.isNotBlank()) {
            NSUserDefaults.standardUserDefaults.setObject(
                value = listOf(languageCode),
                forKey = "AppleLanguages"
            )
        }
    }
}
