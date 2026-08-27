package com.anto426.uniapp.android.ui.screens


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.anto426.uniapp.android.R
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidStatusCard
import com.anto426.liquidmonet.components.cards.LiquidStatusType
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.feedback.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.components.feedback.LiquidSheet
import com.anto426.liquidmonet.components.selection.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.android.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.android.ui.data.UiInitialData
import com.anto426.uniapp.android.ui.models.NewsItem

import com.kyant.backdrop.Backdrop


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    backdropState: Backdrop,
    onOpenCareer: () -> Unit,
    onOpenServices: () -> Unit = {},
    onOpenDidactics: () -> Unit = {},
    onOpenTaxes: () -> Unit = {},
    onOpenGrades: () -> Unit = {},
    onOpenExams: () -> Unit = {},
    onOpenTranscripts: () -> Unit = {},
    onOpenContacts: () -> Unit = {},
    onOpenTransport: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenNews: () -> Unit = {},
    onOpenDevices: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    var selectedNews by remember { mutableStateOf<NewsItem?>(null) }

    val homeNews = UiInitialData.homeNews
    val allActions = UiInitialData.allQuickActions

    // ID selezionati dall'utente
    val selectedActionIds = remember {
        mutableStateListOf("libretto", "appelli", "didattica", "trasporti", "tasse", "rubrica")
    }

    var isCustomizing by remember { mutableStateOf(false) }

    UniScreenColumn {
        // 1. Card Carriera — leggera, tipografica, minimal
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(28.dp),
            contentPadding = 20.dp,
            onClick = onOpenCareer
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Eyebrow
                Text(
                    text = stringResource(R.string.ui_home_career_eyebrow),
                    color = colorScheme.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.6.sp
                )

                // Metrica principale + media affiancata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "90",
                            color = colorScheme.onSurface,
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-2).sp,
                            lineHeight = 52.sp
                        )
                        Text(
                            text = stringResource(R.string.ui_home_degree_estimate),
                            color = colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Badge media ponderata
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(colorScheme.primaryContainer.copy(alpha = 0.45f))
                            .clickable { onOpenGrades() }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "27.8",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = colorScheme.onSurface,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = stringResource(R.string.ui_home_average_label),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Barra progresso CFU
                LiquidLinearProgressIndicator(
                    progress = 0.67f,
                    backdropState = backdropState
                )

                // Footer CFU compatto
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.ui_home_cfu_acquired, "120"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.ui_home_cfu_missing, "60"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.primary
                    )
                }
            }
        }

        // 2. Indicatori — Appelli + Rate con LiquidIcons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(clip = false),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiquidCard(
                modifier = Modifier.weight(1f),
                backdropState = backdropState,
                shape = RoundedCornerShape(18.dp),
                contentPadding = 16.dp,
                onClick = onOpenDidactics
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = LiquidIcons.Calendar,
                            contentDescription = null,
                            tint = colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "0",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onSurface,
                            letterSpacing = (-0.5).sp,
                            lineHeight = 22.sp
                        )
                    }
                    Text(
                        text = stringResource(R.string.ui_home_exams_open),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            LiquidCard(
                modifier = Modifier.weight(1f),
                backdropState = backdropState,
                shape = RoundedCornerShape(18.dp),
                contentPadding = 16.dp,
                onClick = onOpenTaxes
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = LiquidIcons.Warning,
                            contentDescription = null,
                            tint = colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "1",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onSurface,
                            letterSpacing = (-0.5).sp,
                            lineHeight = 22.sp
                        )
                    }
                    Text(
                        text = stringResource(R.string.ui_home_tax_deadline),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }



        // 3. Notizie — scroll orizzontale
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.ui_home_news_eyebrow),
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )

                Text(
                    text = stringResource(R.string.ui_home_news_all),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onOpenNews() }
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(homeNews) { news ->
                    LiquidStatusCard(
                        modifier = Modifier.width(280.dp),
                        title = news.title,
                        description = news.description,
                        statusType = news.type,
                        backdropState = backdropState,
                        onClick = { selectedNews = news }
                    )
                }
            }
        }

        selectedNews?.let { news ->
            LiquidSheet(
                onDismissRequest = { selectedNews = null },
                title = news.title,
                subtitle = news.description,
                backdropState = backdropState
            ) {
                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                    Text(
                        text = news.fullContent,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )
                }
            }
        }




        // Accesso Rapido — scroll orizzontale di chip compatti
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(clip = false)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(clip = false),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.ui_home_quick_access_eyebrow),
                    modifier = Modifier.padding(start = 4.dp),
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )

                LiquidButton(
                    onClick = { isCustomizing = !isCustomizing },
                    modifier = Modifier.padding(end = 4.dp),
                    variant = if (isCustomizing) LiquidButtonVariant.Tonal else LiquidButtonVariant.Glass,
                    size = LiquidButtonSize.Small,
                    backdropState = backdropState
                ) {
                    AnimatedContent(
                        targetState = isCustomizing,
                        transitionSpec = {
                            (fadeIn() + scaleIn(initialScale = 0.85f)) togetherWith
                            (fadeOut() + scaleOut(targetScale = 0.85f))
                        },
                        label = "customizeBtn"
                    ) { customizing ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (customizing) LiquidIcons.Check else LiquidIcons.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (customizing) stringResource(R.string.ui_done) else stringResource(R.string.ui_customize),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

            }

            if (isCustomizing) {
                // Pannello selezione
                LiquidCard(
                    modifier = Modifier.graphicsLayer(clip = false),
                    backdropState = backdropState,
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = 20.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer(clip = false),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.ui_home_customize_title),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            color = colorScheme.primary
                        )

                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer(clip = false),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            allActions.forEach { action ->
                                val isSelected = selectedActionIds.contains(action.id)
                                LiquidChip(
                                    label = action.title,
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) {
                                            if (selectedActionIds.size > 2) selectedActionIds.remove(action.id)
                                        } else {
                                            selectedActionIds.add(action.id)
                                        }
                                    },
                                    leadingIcon = action.icon,
                                    trailingIcon = if (isSelected) LiquidIcons.Check else null,
                                    backdropState = backdropState
                                )
                            }
                        }



                        LiquidButton(
                            text = stringResource(R.string.ui_save),
                            onClick = { isCustomizing = false },
                            modifier = Modifier.fillMaxWidth(),
                            variant = LiquidButtonVariant.Primary,
                            backdropState = backdropState
                        )
                    }
                }
            } else {
                val currentSelectedActions = allActions.filter { selectedActionIds.contains(it.id) }
                val chunkedActions = currentSelectedActions.chunked(2)
                chunkedActions.forEach { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer(clip = false),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { action ->
                            LiquidCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .graphicsLayer(clip = false),
                                backdropState = backdropState,
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = 16.dp,
                                onClick = {
                                    when (action.id) {
                                        "libretto" -> onOpenTranscripts()
                                        "media" -> onOpenGrades()
                                        "appelli" -> onOpenExams()
                                        "didattica" -> onOpenDidactics()
                                        "trasporti" -> onOpenTransport()
                                        "tasse" -> onOpenTaxes()
                                        "rubrica" -> onOpenContacts()
                                        "notifiche" -> onOpenNews()
                                        "condivisione" -> onOpenDidactics()
                                        "sicurezza" -> onOpenDevices()
                                        "impostazioni" -> onOpenSettings()
                                        else -> onOpenServices()
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.graphicsLayer(clip = false),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = action.icon,
                                        contentDescription = null,
                                        tint = colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = action.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
