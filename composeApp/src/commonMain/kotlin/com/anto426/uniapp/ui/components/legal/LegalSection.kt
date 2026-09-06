package com.anto426.uniapp.ui.components.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidAccordionItem
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.legal.LegalSectionData
import com.kyant.shapes.RoundedRectangle

/**
 * Hero card for legal and information screens, featuring squircle geometry,
 * an icon in a tinted container, badges, and introductory overview.
 */
@Composable
fun LegalHeroCard(
    title: String,
    subtitle: String,
    intro: String,
    icon: ImageVector,
    badges: List<String>,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    LiquidCard(
        shape = RoundedRectangle(24.dp),
        contentPadding = 20.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.liquidIconContainer(
                        containerSize = 48.dp,
                        iconSize = 24.dp,
                        containerColor = colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedRectangle(16.dp),
                    ),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (badges.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    badges.forEach { badgeText ->
                        LiquidBadge(
                            text = badgeText,
                            containerColor = colorScheme.primary.copy(alpha = 0.10f),
                            contentColor = colorScheme.primary,
                        )
                    }
                }
            }

            if (intro.isNotBlank()) {
                Text(
                    text = intro,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface.copy(alpha = 0.85f),
                    lineHeight = 22.sp,
                )
            }
        }
    }
}

/**
 * Control bar for long legal documents with section count and expand/collapse all toggle.
 */
@Composable
fun LegalControlBar(
    sectionCount: Int,
    allExpanded: Boolean,
    onToggleExpandAll: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "sezioni",
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "$sectionCount $label",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LiquidButton(
            text = if (allExpanded) "Comprimi tutti" else "Espandi tutti",
            onClick = onToggleExpandAll,
            variant = LiquidButtonVariant.Glass,
            size = LiquidButtonSize.Small,
            leadingIcon = {
                Icon(
                    imageVector = if (allExpanded) LiquidIcons.KeyboardArrowUp else LiquidIcons.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            },
        )
    }
}

/**
 * Controlled expandable legal section item with fluid spring physics.
 */
@Composable
fun LegalSection(
    section: LegalSectionData,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector = LiquidIcons.Info,
) {
    LiquidAccordionItem(
        title = section.title,
        leadingIcon = leadingIcon,
        isExpanded = isExpanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier,
    ) {
        Text(
            text = section.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            lineHeight = 22.sp,
            modifier = Modifier.padding(vertical = 4.dp),
        )
    }
}

/**
 * Uncontrolled expandable legal section item with internal state.
 */
@Composable
fun LegalSection(
    section: LegalSectionData,
    defaultExpanded: Boolean = false,
    leadingIcon: ImageVector = LiquidIcons.Info,
) {
    var isExpanded by remember { mutableStateOf(defaultExpanded) }
    LegalSection(
        section = section,
        isExpanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        leadingIcon = leadingIcon,
    )
}

/**
 * Legal document footer with last update badge and open-source project note.
 */
@Composable
fun LegalFooter(
    modifier: Modifier = Modifier,
    lastUpdate: String = "2 settembre 2026",
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LiquidBadge(
            text = "Ultimo aggiornamento • $lastUpdate",
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "UniApp • Progetto Open Source Indipendente",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
        )
    }
}
