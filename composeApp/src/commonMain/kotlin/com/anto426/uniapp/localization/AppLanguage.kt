package com.anto426.uniapp.localization

import androidx.compose.runtime.Composable

/** Applies the persisted application language at the platform boundary. */
@Composable
internal expect fun BindAppLanguage(languageCode: String)
