package com.anto426.uniapp.ui.components.banners

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.ui.components.cards.UniHeroCardPalette
import com.anto426.uniapp.ui.components.cards.UniHeroFlipTrigger
import com.anto426.uniapp.ui.components.cards.UniHeroGlassCard
import com.anto426.uniapp.ui.components.cards.heroTextAccent
import com.anto426.uniapp.ui.components.cards.rememberUniHeroCardPalette

/**
 * Hero Card minimale per la schermata Info dell'applicazione.
 * Condivide il disegno atmosferico calmo e la tipografia [VersionText] di [UniAppUpdateBanner].
 */
@Composable
fun UniAppInfoBanner(
    modifier: Modifier = Modifier,
    height: Dp = 310.dp,
    version: String,
    title: String,
    subtitle: String,
    buildInfo: String? = null,
    statusBadge: String? = null,
    statusIsHighlight: Boolean = false,
    statusIsMandatory: Boolean = false,
    onClick: (() -> Unit)? = null,
    palette: UniHeroCardPalette = rememberUniHeroCardPalette(),
) {
    val heroFontSize = when {
        height <= 300.dp -> 68
        height < 380.dp -> 84
        else -> 92
    }
    val glassColors = LiquidGlassTheme.colors
    val coolAccent = remember(palette) { palette.cool.heroTextAccent() }
    val warmAccent = remember(palette) { palette.warm.heroTextAccent() }
    val statusDotColor = when {
        statusIsMandatory -> glassColors.warning
        statusIsHighlight -> warmAccent
        else -> coolAccent
    }

    UniHeroGlassCard(
        modifier = modifier,
        height = height,
        flipTrigger = UniHeroFlipTrigger.LONG_PRESS,
        onClick = onClick,
        palette = palette,
        frontContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (!statusBadge.isNullOrBlank()) {
                    MinimalStatusIndicatorRow(
                        text = statusBadge,
                        dotColor = statusDotColor,
                    )
                }

                Spacer(Modifier.weight(0.55f))

                // Parte centrale leggermente rialzata: Versione hero, titolo e ateneo
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    VersionText(
                        version = version,
                        fontSize = heroFontSize,
                        maxHeight = height * 0.42f,
                    )
                    Text(
                        text = title,
                        color = Color.White.copy(alpha = 0.96f),
                        style = MaterialTheme.typography.headlineSmall,
                        fontSize = 25.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.78f),
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.weight(1f))

                // Parte inferiore: dicitura build minimale senza cornici
                if (buildInfo != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = buildInfo,
                            color = Color.White.copy(alpha = 0.68f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (onClick != null) {
                            Icon(
                                imageVector = LiquidIcons.ChevronRight,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.56f),
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                } else {
                    Spacer(Modifier.height(2.dp))
                }
            }
        },
        backContent = {
            UpdateBannerNeverSettleBackFace()
        },
    )
}
