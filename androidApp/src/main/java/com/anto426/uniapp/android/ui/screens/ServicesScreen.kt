package com.anto426.uniapp.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import com.anto426.liquidmonet.components.cards.LiquidStatusCard
import com.anto426.liquidmonet.components.cards.LiquidStatusType
import com.anto426.uniapp.android.ui.components.items.ServiceRow
import com.anto426.uniapp.android.ui.components.layout.UniScreenColumn
import com.anto426.liquidmonet.components.display.LiquidSectionTitle
import com.anto426.uniapp.android.ui.data.UiInitialData
import com.kyant.backdrop.Backdrop

@Composable
fun ServicesScreen(backdropState: Backdrop, onNavigateToService: (String) -> Unit = {}) {
    UniScreenColumn {
        Column(Modifier.fillMaxWidth().graphicsLayer(clip = false), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            UiInitialData.studentServices.chunked(2).forEach { ServiceRow(it, backdropState, onNavigateToService) }
        }
        Spacer(Modifier.height(8.dp))
        LiquidSectionTitle(title = "Ateneo", subtitle = "Portali e carriera accademica")
        Column(Modifier.fillMaxWidth().graphicsLayer(clip = false), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LiquidStatusCard(title = "Manutenzione Esse3", description = "Il portale sarà offline Domenica 30 dalle 08:00 alle 14:00.", statusType = LiquidStatusType.Warning, backdropState = backdropState)
            UiInitialData.universityPortals.chunked(2).forEach { ServiceRow(it, backdropState, onNavigateToService) }
        }
    }
}
