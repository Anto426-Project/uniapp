package com.anto426.uniapp.ui.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.services.presentation.ServicesUiState
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.items.ServiceRow
import com.anto426.uniapp.ui.components.items.serviceAccentColor
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.layout.UniSection
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun ServicesScreen(
    uiState: ServicesUiState,
    onNavigateToService: (String) -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme

    UniScreenColumn {
        // 1. Servizi interni in griglia Bento compatta 2x2
        if (uiState.studentServices.isNotEmpty()) {
            UniSection(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                entranceOrder = 0,
            ) {
                uiState.studentServices
                    .chunked(2)
                    .forEach { ServiceRow(it, onNavigateToService) }
            }
        }

        // 2. Portali digitali esterni in stile Apple Inset Grouped List
        if (uiState.universityPortals.isNotEmpty()) {
            UniSection(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                entranceOrder = 1,
            ) {
                LiquidSectionHeader(
                    title = stringResource(Res.string.ui_services_portals_title),
                    subtitle = stringResource(Res.string.ui_services_portals_sub),
                )
                UniInsetGroup(cornerRadius = 22.dp, animateEntrance = false) {
                    uiState.universityPortals.forEachIndexed { index, portal ->
                        val navTarget = portal.id.ifBlank { portal.title }
                        UniInsetRow(
                            title = portal.title,
                            subtitle = portal.subtitle,
                            icon = portal.icon,
                            iconTint = serviceAccentColor(portal.id),
                            onClick = { onNavigateToService(navTarget) },
                            showDivider = index < uiState.universityPortals.lastIndex,
                            trailingContent = {
                                Icon(
                                    imageVector = LiquidIcons.ArrowForward,
                                    contentDescription = null,
                                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .size(15.dp)
                                        .rotate(-45f),
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}
