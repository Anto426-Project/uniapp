package com.anto426.uniapp.localization

import androidx.compose.runtime.Composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

/** Applies the persisted application language at the platform boundary. */
@Composable
internal expect fun BindAppLanguage(languageCode: String)

internal suspend fun readAppString(resource: StringResource): String =
    withContext(Dispatchers.Default) { getString(resource) }
