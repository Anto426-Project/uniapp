package com.anto426.uniapp.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.uniapp.settings.presentation.LanguageUiState
import com.anto426.uniapp.ui.components.items.LanguageItem
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop

@Composable
fun LanguageScreen(
    backdropState: Backdrop,
    uiState: LanguageUiState,
    onLanguageSelected: (String) -> Unit,
) {
    UniScreenColumn {
        // Language Selection Group without title and nested containers
        LiquidPreferenceGroup(backdropState = backdropState) {
            uiState.languages.forEachIndexed { index, language ->
                LanguageItem(
                    language = language,
                    isSelected = uiState.selectedLanguageCode == language.code,
                    onClick = { onLanguageSelected(language.code) },
                    backdropState = backdropState
                )

                if (index < uiState.languages.lastIndex) {
                    LiquidHorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }

        // Informational Footer
        Text(
            text = stringResource(Res.string.ui_language_restart),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}
