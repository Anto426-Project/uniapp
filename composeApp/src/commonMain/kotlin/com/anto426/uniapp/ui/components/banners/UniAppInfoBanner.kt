package com.anto426.uniapp.ui.components.banners

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.uniapp.ui.components.cards.UniHeroFlipTrigger
import com.anto426.uniapp.ui.components.cards.UniHeroGlassCard

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
) {
    val heroFontSize = if (height <= 250.dp) 72 else if (height < 360.dp) 82 else 96

    UniHeroGlassCard(
        modifier = modifier,
        height = height,
        flipTrigger = UniHeroFlipTrigger.LONG_PRESS,
        onClick = onClick,
        frontContent = {
            val scheme = MaterialTheme.colorScheme
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
            ) {
                // Parte superiore: Versione hero con alone ottico, titolo e ateneo
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    VersionText(version = version, fontSize = heroFontSize)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = title,
                        color = scheme.onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                    )
                    Text(
                        text = subtitle,
                        color = scheme.onSurface.copy(alpha = 0.72f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                // Parte inferiore: Dettagli build/versione installata
                if (buildInfo != null) {
                    Text(
                        text = buildInfo,
                        color = scheme.onSurface.copy(alpha = 0.62f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        },
        backContent = {
            UpdateBannerNeverSettleBackFace()
        },
    )
}
