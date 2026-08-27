package com.anto426.uniapp.android.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.anto426.uniapp.android.ui.components.items.ChangelogVersion
import com.anto426.uniapp.android.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.android.ui.data.UiInitialData
import com.kyant.backdrop.Backdrop

@Composable
fun ChangelogScreen(backdropState: Backdrop) {
    val versions = UiInitialData.changelog
    var expandedVersion by remember { mutableStateOf(versions.firstOrNull()?.version.orEmpty()) }
    UniScreenColumn {
        versions.forEach { version ->
            ChangelogVersion(version, expandedVersion == version.version, { expandedVersion = if (it) version.version else "" }, backdropState)
        }
    }
}
