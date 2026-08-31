package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.liquidmonet.components.feedback.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.components.items.DidacticItem
import com.anto426.uniapp.ui.components.items.DidacticRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop

@Composable
fun DidacticsScreen(
    backdropState: Backdrop,
    onOpenCareer: () -> Unit = {},
    onOpenTaxes: () -> Unit = {},
    onOpenGrades: () -> Unit = {},
    onOpenStatistics: () -> Unit = {},
    onOpenTranscripts: () -> Unit = {},
    onOpenExams: () -> Unit = {},
    onOpenQuestionnaires: () -> Unit = {},
    onOpenBadge: () -> Unit = {},
    onOpenAttendance: () -> Unit = {},
    onOpenStudyPlan: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    UniScreenColumn {
        // 1. Academic Degree Header Card (Scenografica, Accademica & a Tema Monet)
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(26.dp),
            contentPadding = 18.dp,
            onClick = onOpenStatistics,
            interactiveGelatin = true
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header con Icona Vetro & Badge Esami
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(colorScheme.primaryContainer.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LiquidIcons.Info,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = "Ingegneria Informatica",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "Classe L-8 • Matricola 165432",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    LiquidBadge(
                        text = "14 / 20 Esami",
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.primary,
                        backdropState = backdropState
                    )
                }

                // Stepper Triennale Scenografico a Vetro
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1° Anno
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colorScheme.primaryContainer.copy(alpha = 0.25f))
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    imageVector = LiquidIcons.Check,
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "1° Anno",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "60 / 60 CFU",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.primary
                            )
                        }
                    }

                    // 2° Anno
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colorScheme.primaryContainer.copy(alpha = 0.25f))
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    imageVector = LiquidIcons.Check,
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "2° Anno",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "60 / 60 CFU",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.primary
                            )
                        }
                    }

                    // 3° Anno
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    imageVector = LiquidIcons.Time,
                                    contentDescription = null,
                                    tint = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "3° Anno",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "In Corso (0/60)",
                                fontSize = 10.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Barra fluida Liquid Monet
                LiquidLinearProgressIndicator(
                    progress = 0.67f,
                    backdropState = backdropState
                )

                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "120 su 180 CFU totali (67%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Media Ponderata: 28.2 / 30",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                }
            }
        }

        // 2. Section: Carriera e Piano di Studi
        LiquidSectionHeader(
            title = "Carriera e Valutazioni",
            subtitle = "Libretto esami, piano e calcolo della media"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(clip = false),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DidacticRow(
                item1 = {
                    DidacticItem(
                        title = "Libretto",
                        subtitle = "Esami e voti registrati",
                        icon = LiquidIcons.Calendar,
                        backdropState = backdropState,
                        onClick = onOpenTranscripts
                    )
                },
                item2 = {
                    DidacticItem(
                        title = "Piano di Studio",
                        subtitle = "Curriculum triennale",
                        icon = LiquidIcons.Edit,
                        backdropState = backdropState,
                        onClick = onOpenStudyPlan
                    )
                }
            )

            DidacticRow(
                item1 = {
                    DidacticItem(
                        title = "Statistiche",
                        subtitle = "Grafici e andamento",
                        icon = LiquidIcons.Star,
                        backdropState = backdropState,
                        onClick = onOpenStatistics
                    )
                },
                item2 = {
                    DidacticItem(
                        title = "Media e Voti",
                        subtitle = "Simulazione e calcolo",
                        icon = LiquidIcons.Edit,
                        backdropState = backdropState,
                        onClick = onOpenGrades
                    )
                }
            )

            DidacticRow(
                item1 = {
                    DidacticItem(
                        title = "Carriera",
                        subtitle = "Riepilogo e progressi",
                        icon = LiquidIcons.Info,
                        backdropState = backdropState,
                        onClick = onOpenCareer
                    )
                },
                item2 = {
                    DidacticItem(
                        title = "Tasse e Contributi",
                        subtitle = "Situazione pagamenti",
                        icon = LiquidIcons.Warning,
                        backdropState = backdropState,
                        onClick = onOpenTaxes
                    )
                }
            )
        }

        // 3. Section: Esami e Attività Didattica
        LiquidSectionHeader(
            title = "Esami e Aula",
            subtitle = "Appelli d'esame, presenze e rilevazioni"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(clip = false),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DidacticRow(
                item1 = {
                    DidacticItem(
                        title = "Appelli Esami",
                        subtitle = "Iscrizioni e prenotazioni",
                        icon = LiquidIcons.Calendar,
                        backdropState = backdropState,
                        badgeCount = 2,
                        onClick = onOpenExams
                    )
                },
                item2 = {
                    DidacticItem(
                        title = "Presenze Aula",
                        subtitle = "Rilevazione presenze QR",
                        icon = LiquidIcons.Check,
                        backdropState = backdropState,
                        onClick = onOpenAttendance
                    )
                }
            )

            DidacticRow(
                item1 = {
                    DidacticItem(
                        title = "Questionari OPIS",
                        subtitle = "Valutazione didattica",
                        icon = LiquidIcons.Edit,
                        backdropState = backdropState,
                        onClick = onOpenQuestionnaires
                    )
                },
                item2 = {
                    DidacticItem(
                        title = "Badge Studente",
                        subtitle = "Tessera identificativa",
                        icon = LiquidIcons.AccountCircle,
                        backdropState = backdropState,
                        onClick = onOpenBadge
                    )
                }
            )
        }
    }
}
