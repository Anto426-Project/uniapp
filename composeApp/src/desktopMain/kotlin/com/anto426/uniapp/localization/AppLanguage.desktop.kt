package com.anto426.uniapp.localization
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.util.Locale
@Composable
internal actual fun BindAppLanguage(languageCode: String) {
    LaunchedEffect(languageCode) {
        if (languageCode.isNotBlank()) Locale.setDefault(Locale.forLanguageTag(languageCode))
    }
}
