package com.anto426.uniapp.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.icons.LiquidIcons
import com.kyant.backdrop.Backdrop

val LocalUniScreenPadding = compositionLocalOf { PaddingValues(0.dp) }

@Composable
fun UniScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    val padding = LocalUniScreenPadding.current
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .graphicsLayer(clip = false),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(padding.calculateTopPadding() + 12.dp))
        content()
        Spacer(Modifier.height(padding.calculateBottomPadding() + 32.dp))
    }
}

@Composable
fun UniHeroCard(
    backdropState: Backdrop,
    eyebrow: String,
    title: String,
    subtitle: String,
    icon: ImageVector? = null,
    leadingContent: (@Composable () -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    LiquidCard (backdropState = backdropState) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (leadingContent != null) {
                leadingContent()
            } else if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(eyebrow.uppercase(), color = colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                Text(title, color = colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall)
                Text(subtitle, color = colorScheme.onSurface.copy(alpha = .72f))
            }
        }
    }
}

/**
 * A decorative card inspired by a well-known slogan, adapted for UniApp.
 */
@Composable
fun UniNeverSettleCard(
    backdropState: Backdrop,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    LiquidCard(
        backdropState = backdropState,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        containerColor = colorScheme.primaryContainer.copy(alpha = 0.15f),
        contentPadding = 24.dp,
        interactiveGelatin = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text(
                    text = "UniApp Experience ✨",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = colorScheme.primary.copy(alpha = 0.8f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "NEVER",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            letterSpacing = (-1).sp
                        ),
                        color = colorScheme.onSurface
                    )

                    Surface(
                        color = colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "SETTLE",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LiquidIcons.Star,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
