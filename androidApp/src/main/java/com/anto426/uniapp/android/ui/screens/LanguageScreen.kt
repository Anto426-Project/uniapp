package com.anto426.uniapp.android.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.anto426.uniapp.android.R
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.uniapp.android.ui.components.items.LanguageItem
import com.anto426.uniapp.android.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.android.ui.data.UiInitialData
import com.kyant.backdrop.Backdrop

@Composable
fun LanguageScreen(backdropState: Backdrop) {
    val languages = UiInitialData.languages
    var selectedLanguage by remember { mutableStateOf(languages.firstOrNull()?.code.orEmpty()) }

    UniScreenColumn {
        // Language Selection Group without title and nested containers
        LiquidPreferenceGroup(backdropState = backdropState) {
            languages.forEachIndexed { index, language ->
                LanguageItem(
                    language = language,
                    isSelected = selectedLanguage == language.code,
                    onClick = { selectedLanguage = language.code },
                    backdropState = backdropState
                )

                if (index < languages.lastIndex) {
                    LiquidHorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }

        // Informational Footer
        Text(
            text = stringResource(R.string.ui_language_restart),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}
