package com.anto426.uniapp.ui.legal

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.uniapp.model.legal.LegalSectionData
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop

@Composable
fun PrivacyScreen(backdropState: Backdrop, sections: List<LegalSectionData>) {
    val colorScheme = MaterialTheme.colorScheme

    UniScreenColumn {
        sections.forEach { section ->
            LiquidPreferenceGroup(
                title = section.title,
                backdropState = backdropState,
            ) {
                Text(
                    text = section.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface.copy(alpha = 0.85f),
                    modifier = Modifier.padding(16.dp),
                    lineHeight = 22.sp,
                )
            }
        }
    }
}
