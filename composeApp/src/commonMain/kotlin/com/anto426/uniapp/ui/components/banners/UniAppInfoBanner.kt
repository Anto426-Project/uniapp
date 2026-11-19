package com.anto426.uniapp.ui.components.banners

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.uniapp.ui.components.cards.UniHeroCardPalette
import com.anto426.uniapp.ui.components.cards.UniHeroFlipTrigger
import com.anto426.uniapp.ui.components.cards.UniHeroGlassCard
import com.anto426.uniapp.ui.components.cards.rememberUniHeroCardPalette

/**
 * Hero Card compatta di presentazione e branding per la schermata Info dell'applicazione.
 * Basata sull'estetica di [UniAppUpdateBanner] con tipografia dinamica [VersionText]
 * e retro 3D "NEVER SETTLE", ma senza controlli o informazioni di download/aggiornamento.
 */
@Composable
fun UniAppInfoBanner(
    modifier: Modifier = Modifier,
    height: Dp = 360.dp,
    version: String,
    title: String,
    subtitle: String,
    buildInfo: String? = null,
    onClick: (() -> Unit)? = null,
    palette: UniHeroCardPalette = rememberUniHeroCardPalette(),
) {
    val heroFontSize = if (height <= 250.dp) 72 else if (height < 360.dp) 82 else 96

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
                    .heightIn(min = height)
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // Parte superiore: Versione hero con alone ottico, titolo e ateneo
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    VersionText(
                        version = version,
                        fontSize = heroFontSize,
                        maxHeight = height * 0.40f,
                    )
                    Text(
                        text = title,
                        color = Color.White,
                        style = HeroBannerTextStyle,
                        fontSize = 24.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.94f),
                        style = HeroBannerTextStyle,
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Parte inferiore: Dettagli build/versione installata
                if (buildInfo != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = buildInfo,
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Spacer(Modifier.height(4.dp))
                }
            }
        },
        backContent = {
            UpdateBannerNeverSettleBackFace()
        },
    )
}
