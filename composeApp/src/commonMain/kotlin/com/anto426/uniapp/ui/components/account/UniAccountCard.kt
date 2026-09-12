package com.anto426.uniapp.ui.components.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.LiquidIconButton
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidCardDefaults
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.account.model.UniAccountSummary
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

/**
 * Card di visualizzazione e selezione dell'account per lo sheet di switch account.
 * Ottimizza lo spazio orizzontale e verticale garantendo piena leggibilità di nome, corso e matricola.
 */
@Composable
fun UniAccountCard(
    account: UniAccountSummary,
    isActive: Boolean,
    isActivating: Boolean,
    isSwitching: Boolean,
    profileImage: ByteArray?,
    onSelect: () -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean,
    modifier: Modifier = Modifier,
) {
    val initials = account.displayName
        .split(' ')
        .filter(String::isNotBlank)
        .take(2)
        .map { it.first() }
        .joinToString("")

    val identity = accountDisplayIdentity(account.displayName, initials, profileImage, account)

    val colorScheme = MaterialTheme.colorScheme

    val cardModifier = if (isActive) {
        modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = colorScheme.primary.copy(alpha = 0.45f),
                shape = RoundedRectangle(20.dp),
            )
    } else {
        modifier.fillMaxWidth()
    }

    LiquidCard(
        modifier = cardModifier,
        onClick = if (isActive || isSwitching) null else onSelect,
        contentPadding = 14.dp,
        shape = RoundedRectangle(20.dp),
        colors = if (isActive) {
            LiquidCardDefaults.colors(
                containerColor = colorScheme.primary.copy(alpha = 0.12f),
            )
        } else {
            LiquidCardDefaults.colors()
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Riga principale: Avatar con checkmark se attivo + Dettagli utente + Elimina
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Avatar con checkmark se attivo
                Box(contentAlignment = Alignment.BottomEnd) {
                    UniAccountAvatar(
                        imageData = identity.photo,
                        initials = if (identity.initials.isNotBlank()) identity.initials else stringResource(Res.string.msg_un),
                        size = 46.dp,
                        contentDescription = stringResource(Res.string.ui_profile_picture),
                    )
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedRectangle(5.dp))
                                .background(colorScheme.primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = LiquidIcons.Check,
                                contentDescription = null,
                                tint = colorScheme.onPrimary,
                                modifier = Modifier.size(10.dp),
                            )
                        }
                    }
                }

                // Dati Utente: Nome, Badge di Stato e Corso di Laurea
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = identity.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (isActive) {
                            LiquidBadge(
                                text = stringResource(Res.string.ui_account_active),
                                containerColor = colorScheme.primary.copy(alpha = 0.18f),
                                contentColor = colorScheme.primary,
                            )
                        } else if (isActivating) {
                            LiquidBadge(
                                text = stringResource(Res.string.ui_account_activating),
                                containerColor = colorScheme.secondary.copy(alpha = 0.18f),
                                contentColor = colorScheme.secondary,
                            )
                        }
                    }

                    Text(
                        text = account.degreeName.ifBlank {
                            if (account.isProfessor) {
                                stringResource(Res.string.ui_professor_role)
                            } else {
                                stringResource(Res.string.ui_degree_label)
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Pulsante elimina compatto
                LiquidIconButton(
                    icon = LiquidIcons.Delete,
                    onClick = onRemove,
                    enabled = canRemove,
                    size = 36.dp,
                    iconSize = 18.dp,
                    contentDescription = stringResource(Res.string.ui_account_remove),
                )
            }

            // Riga secondaria: Matricola o email a sinistra, invito al passaggio account a destra
            val matricolaText = account.matricola?.takeIf { it.isNotBlank() }?.let {
                stringResource(Res.string.ui_matricola_prefix, it)
            } ?: account.email?.takeIf { it.isNotBlank() && account.isProfessor }

            if (matricolaText != null || (!isActive && !isActivating)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 58.dp), // allineato otticamente dopo l'avatar
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (matricolaText != null) {
                        Text(
                            text = matricolaText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.primary,
                        )
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    if (!isActive && !isActivating) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.ui_home_switch_career),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.primary.copy(alpha = 0.85f),
                                fontWeight = FontWeight.SemiBold,
                            )
                            Icon(
                                imageVector = LiquidIcons.ArrowForward,
                                contentDescription = null,
                                tint = colorScheme.primary.copy(alpha = 0.85f),
                                modifier = Modifier.size(11.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
