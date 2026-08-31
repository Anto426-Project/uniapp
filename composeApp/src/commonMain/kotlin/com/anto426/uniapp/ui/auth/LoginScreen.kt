package com.anto426.uniapp.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidAvatar
import com.anto426.liquidmonet.components.display.LiquidAvatarPresence
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.feedback.LiquidDialog
import com.anto426.liquidmonet.components.inputs.LiquidTextField
import com.anto426.liquidmonet.components.inputs.LiquidTextFieldType
import com.anto426.liquidmonet.components.selection.LiquidSwitch
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.auth.presentation.LoginUiState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.unisdk.backend.model.LoginCareerOption
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

/**
 * Access & Authentication Screen built 100% with official Liquid Monet SDK components.
 */
@Composable
fun LoginScreen(
    backdropState: Backdrop,
    uiState: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberCredentialsChange: (Boolean) -> Unit = {},
    onSubmit: () -> Unit,
    onCareerSelected: (LoginCareerOption) -> Unit,
    onCancelCareerSelection: () -> Unit,
    onShowForgotPassword: () -> Unit,
    onDismissForgotPassword: () -> Unit,
    onOpenPrivacy: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme

    UniScreenColumn {
        Spacer(Modifier.height(12.dp))

        // 1. Official SDK LiquidAvatar & Brand Presentation Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Official SDK LiquidAvatar with Online presence status
            LiquidAvatar(
                size = 80.dp,
                icon = LiquidIcons.AccountCircle,
                presence = LiquidAvatarPresence.Online,
                backdropState = backdropState
            )

            // Typography
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "UniApp",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.8).sp,
                        fontSize = 32.sp
                    ),
                    color = colorScheme.onSurface
                )

                Text(
                    text = "Portale Universitario",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colorScheme.primary
                )

                Text(
                    text = "Accedi con le tue credenziali d'Ateneo",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 2. Master Glass Authentication Form Card
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(30.dp),
            contentPadding = 24.dp,
            interactiveGelatin = false
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Header Form Label with SDK LiquidBadge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Credenziali d'Accesso",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = colorScheme.onSurface
                    )
                    LiquidBadge(
                        text = "A.A. 2024/25",
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.50f),
                        contentColor = colorScheme.primary,
                        backdropState = backdropState
                    )
                }

                // Username / Matricola Field
                LiquidTextField(
                    value = uiState.username,
                    onValueChange = onUsernameChange,
                    type = LiquidTextFieldType.Text,
                    label = "Nome Utente o Matricola",
                    placeholder = "Es. m.rossi o 123456",
                    leadingIcon = LiquidIcons.AccountCircle,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    backdropState = backdropState
                )

                // Password Field
                LiquidTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    type = LiquidTextFieldType.Password,
                    label = "Password Istituzionale",
                    placeholder = "Inserisci la tua password...",
                    leadingIcon = LiquidIcons.Lock,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onSubmit() }
                    ),
                    backdropState = backdropState
                )

                // Remember Credentials Switch Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = "Ricorda credenziali",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = "Mantieni la sessione attiva su questo dispositivo",
                            fontSize = 11.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                    LiquidSwitch(
                        checked = uiState.rememberCredentials,
                        onCheckedChange = onRememberCredentialsChange,
                        backdropState = backdropState
                    )
                }

                // Error Feedback Alert Card
                AnimatedVisibility(
                    visible = uiState.errorMessage != null,
                    enter = fadeIn() + slideInVertically { h -> -h / 3 },
                    exit = fadeOut() + slideOutVertically { h -> -h / 3 }
                ) {
                    uiState.errorMessage?.let { error ->
                        LiquidCard(
                            backdropState = backdropState,
                            containerColor = colorScheme.errorContainer.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = 14.dp,
                            interactiveGelatin = false
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = LiquidIcons.Warning,
                                    contentDescription = null,
                                    tint = colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))

                // Primary Action Button
                LiquidButton(
                    text = "Accedi al Portale",
                    onClick = onSubmit,
                    isLoading = uiState.isLoading,
                    variant = LiquidButtonVariant.Primary,
                    size = LiquidButtonSize.Large,
                    backdropState = backdropState,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(LiquidIcons.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )

                // Secondary Forgot Password Link
                LiquidButton(
                    text = "Password dimenticata?",
                    onClick = onShowForgotPassword,
                    variant = LiquidButtonVariant.Text,
                    size = LiquidButtonSize.Small,
                    backdropState = backdropState,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 3. Legal Links Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiquidButton(
                text = stringResource(Res.string.ui_privacy),
                onClick = onOpenPrivacy,
                variant = LiquidButtonVariant.Text,
                size = LiquidButtonSize.Small,
                backdropState = backdropState
            )
            Text(
                text = "•",
                color = colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            LiquidButton(
                text = stringResource(Res.string.ui_terms),
                onClick = onOpenTerms,
                variant = LiquidButtonVariant.Text,
                size = LiquidButtonSize.Small,
                backdropState = backdropState
            )
        }
    }

    // Forgot Password Dialog
    if (uiState.isForgotPasswordDialogVisible) {
        LiquidDialog(
            onDismissRequest = onDismissForgotPassword,
            title = "Recupero Credenziali",
            text = "Le credenziali di accesso sono gestite direttamente dal tuo Ateneo. Per reimpostare la tua password o sbloccare l'account, contatta la segreteria studenti o visita la pagina di recupero del portale istituzionale.",
            backdropState = backdropState,
            confirmButton = {
                LiquidButton(
                    text = "Ho Capito",
                    onClick = onDismissForgotPassword,
                    variant = LiquidButtonVariant.Primary,
                    backdropState = backdropState,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }

    // Career Selection Dialog (for multi-career users)
    if (uiState.careers.isNotEmpty()) {
        LiquidDialog(
            onDismissRequest = onCancelCareerSelection,
            title = "Seleziona Carriera",
            text = "Abbiamo rilevato più carriere attive associate alle tue credenziali. Seleziona quella con cui desideri accedere.",
            backdropState = backdropState,
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    uiState.careers.forEach { career ->
                        LiquidCard(
                            backdropState = backdropState,
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = 16.dp,
                            onClick = {
                                onCareerSelected(career)
                            },
                            interactiveGelatin = true
                        ) {
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = career.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = colorScheme.primary
                                    )
                                    career.matricola?.let { matr ->
                                        LiquidBadge(
                                            text = "matr. $matr",
                                            containerColor = colorScheme.primaryContainer.copy(alpha = 0.50f),
                                            contentColor = colorScheme.primary,
                                            backdropState = backdropState
                                        )
                                    }
                                }
                                Text(
                                    text = career.degreeName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = colorScheme.onSurface
                                )
                                if (!career.departmentName.isNullOrBlank()) {
                                    Text(
                                        text = career.departmentName.orEmpty(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    LiquidButton(
                        text = stringResource(Res.string.ui_cancel),
                        onClick = onCancelCareerSelection,
                        variant = LiquidButtonVariant.Text,
                        backdropState = backdropState,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}
