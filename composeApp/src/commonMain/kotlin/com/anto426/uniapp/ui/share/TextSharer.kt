package com.anto426.uniapp.ui.share

import androidx.compose.runtime.Composable

/** Returns a callback that opens the platform share sheet for plain text. */
@Composable
expect fun rememberTextSharer(): (String) -> Unit
