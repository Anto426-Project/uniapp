package com.anto426.uniapp.localization

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Composable
internal actual fun BindAppLanguage(languageCode: String) {
    val context = LocalContext.current
    LaunchedEffect(context, languageCode) {
        if (languageCode.isBlank()) return@LaunchedEffect
        val currentLanguage = context.resources.configuration.locales[0]?.language
        if (currentLanguage == languageCode) return@LaunchedEffect

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags(languageCode)
        } else {
            applyLegacyLanguage(context, languageCode)
        }
    }
}

@Suppress("DEPRECATION")
private fun applyLegacyLanguage(context: Context, languageCode: String) {
    val locale = Locale.forLanguageTag(languageCode)
    Locale.setDefault(locale)
    val configuration = Configuration(context.resources.configuration).apply {
        setLocale(locale)
    }
    context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    context.findActivity()?.recreate()
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
