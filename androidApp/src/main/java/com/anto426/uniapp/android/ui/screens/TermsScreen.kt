package com.anto426.uniapp.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.uniapp.android.R
import com.anto426.uniapp.android.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.android.ui.components.legal.LegalSection
import com.anto426.uniapp.android.ui.data.UiInitialData
import com.kyant.backdrop.Backdrop

@Composable
fun TermsScreen(backdropState: Backdrop) {
    UniScreenColumn {
        // Intro section in a clean group
        LiquidPreferenceGroup(backdropState = backdropState) {
            Text(
                text = stringResource(R.string.ui_terms_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp),
                lineHeight = 22.sp
            )
        }

        // Detailed sections
        LiquidCard(backdropState = backdropState) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                UiInitialData.termsSections.forEach { LegalSection(it) }
            }
        }
    }
}
