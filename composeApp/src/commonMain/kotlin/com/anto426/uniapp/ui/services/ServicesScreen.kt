package com.anto426.uniapp.ui.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.uniapp.services.presentation.ServicesUiState
import com.anto426.uniapp.ui.components.items.ServiceRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun ServicesScreen(
    uiState: ServicesUiState,
    onNavigateToService: (String) -> Unit = {}
) {
    UniScreenColumn {
        // 1. Student / Professor Core Services
        if (uiState.studentServices.isNotEmpty()) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_services_student_title),
                subtitle = stringResource(Res.string.ui_services_student_sub),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(clip = false),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.studentServices
                    .chunked(2)
                    .forEach { ServiceRow(it, onNavigateToService) }
            }
        }

        // 2. University Digital Portals
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_services_portals_title),
            subtitle = stringResource(Res.string.ui_services_portals_sub)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(clip = false),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            uiState.universityPortals
                .chunked(2)
                .forEach { ServiceRow(it, onNavigateToService) }
        }
    }
}
