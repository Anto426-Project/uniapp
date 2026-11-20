package com.anto426.uniapp.ui.auth

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.avatar.LiquidAvatar
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.inputs.otpinput.LiquidOtpInput
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.UniBackHandler
import com.anto426.uniapp.session.presentation.AppPasswordUnlockError
import com.anto426.uniapp.session.presentation.AppUnlockUiState
import com.anto426.uniapp.settings.presentation.AppPasswordSetupError
import com.anto426.uniapp.settings.presentation.SettingsViewModel
import com.anto426.uniapp.ui.components.account.LocalAccountAvatars
import com.anto426.uniapp.ui.components.account.UniAccountAvatar
import com.anto426.uniapp.ui.components.account.formatSmartDisplayName
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

/**
 * Schermata di Sblocco dell'Applicazione con PIN numerico e autenticazione biometrica.
 */
@Composable
internal fun AppUnlockScreen(
    accountName: String?,
    accountId: String?,
    unlockUiState: AppUnlockUiState,
    onRequestUnlock: () -> Unit,
    onCancelUnlock: () -> Unit,
    onPasswordUnlock: (String) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val canUnlock = !accountId.isNullOrBlank()
    val accountAvatars = LocalAccountAvatars.current
    val avatar = remember(accountAvatars, accountId) { accountId?.let { accountAvatars[it] } }
    val displayName = remember(accountName) {
        accountName?.trim()?.takeIf(String::isNotBlank)?.let { raw ->
            formatSmartDisplayName(raw).ifBlank { raw }
        }
    }
    val initials = remember(displayName) {
        displayName
            ?.split(' ')
            ?.filter(String::isNotBlank)
            ?.take(2)
            ?.joinToString("") { it.first().uppercaseChar().toString() }
            .orEmpty()
    }
    val pinLength = SettingsViewModel.APP_PIN_LENGTH
    var pin by remember(accountId) { mutableStateOf("") }
    val submitPin: (String) -> Unit = { candidate ->
        if (canUnlock && !unlockUiState.isAuthenticating && candidate.length == pinLength) {
            onPasswordUnlock(candidate)
            pin = ""
        }
    }

    UniScreenColumn(modifier = Modifier.imePadding()) {
        Spacer(Modifier.height(12.dp))

        // 1. Account Welcome Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (displayName != null || avatar != null) {
                UniAccountAvatar(
                    imageData = avatar?.bytes,
                    imageCacheKey = avatar?.cacheKey,
                    initials = initials.ifBlank { stringResource(Res.string.msg_un) },
                    size = 84.dp,
                    contentDescription = displayName ?: stringResource(Res.string.ui_profile_picture),
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(2.dp, colorScheme.primary.copy(alpha = 0.28f), CircleShape),
                )
            } else {
                LiquidAvatar(
                    size = 80.dp,
                    icon = LiquidIcons.AccountCircle,
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (displayName != null) {
                    Text(
                        text = stringResource(Res.string.ui_unlock_welcome_back),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.6).sp,
                            fontSize = 28.sp,
                        ),
                        color = colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = stringResource(Res.string.ui_unlock_title),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.ui_app_name),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.8).sp,
                            fontSize = 32.sp,
                        ),
                        color = colorScheme.onSurface,
                    )

                    Text(
                        text = stringResource(Res.string.ui_unlock_title),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = colorScheme.primary,
                    )

                    Text(
                        text = stringResource(Res.string.ui_unlock_methods),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        // 2. Master Glass Authentication Card
        LiquidCard(
            shape = RoundedRectangle(28.dp),
            contentPadding = 24.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.ui_unlock_protected_access),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = colorScheme.onSurface,
                    )
                    LiquidBadge(
                        text = stringResource(Res.string.ui_unlock_security_badge),
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                        contentColor = colorScheme.primary,
                    )
                }

                // Messaggio di Errore o Stato Autenticazione (esclude 'Accesso annullato' mostrato via toast)
                val errorMessage = unlockUiState.errorMessage
                val cancelMessage = stringResource(Res.string.msg_accesso_annullato)
                if (errorMessage != null && !errorMessage.equals(cancelMessage, ignoreCase = true)) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Sezione PIN numerico UniApp
                PinInputSection(
                    label = stringResource(Res.string.ui_app_password_label),
                    pin = pin,
                    onPinChange = { pin = it },
                    length = pinLength,
                    enabled = canUnlock && !unlockUiState.isAuthenticating,
                    onComplete = submitPin,
                )

                unlockUiState.passwordError?.let { error ->
                    Text(
                        text = stringResource(when (error) {
                            AppPasswordUnlockError.Invalid -> Res.string.ui_app_password_invalid
                            AppPasswordUnlockError.RetryLater -> Res.string.ui_app_password_retry_later
                            AppPasswordUnlockError.Failed -> Res.string.ui_app_password_unlock_failed
                        }),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LiquidButton(
                        text = stringResource(Res.string.ui_app_password_unlock),
                        onClick = { submitPin(pin) },
                        enabled = canUnlock && !unlockUiState.isAuthenticating && pin.length == pinLength,
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    LiquidButton(
                        text = if (unlockUiState.isAuthenticating) {
                            stringResource(Res.string.ui_unlock_authenticating)
                        } else {
                            stringResource(Res.string.ui_unlock_action)
                        },
                        onClick = onRequestUnlock,
                        enabled = canUnlock && !unlockUiState.isAuthenticating,
                        isLoading = unlockUiState.isAuthenticating,
                        leadingIcon = {
                            Icon(
                                imageVector = LiquidIcons.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        variant = LiquidButtonVariant.Secondary,
                    )

                    LiquidButton(
                        text = stringResource(Res.string.ui_unlock_other_account),
                        onClick = onCancelUnlock,
                        enabled = canUnlock && !unlockUiState.isAuthenticating,
                        modifier = Modifier.fillMaxWidth(),
                        variant = LiquidButtonVariant.Text,
                    )
                }
            }
        }
    }
}

/**
 * Schermata a pagina intera per configurare il PIN numerico di protezione UniApp
 * quando l'utente attiva la sicurezza biometrica dalle Impostazioni.
 */
@Composable
internal fun AppPasswordSetupScreen(
    isAuthenticating: Boolean,
    error: AppPasswordSetupError?,
    onSubmit: (password: String, confirmation: String) -> Unit,
    onDismiss: () -> Unit,
    handleBack: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    val pinLength = SettingsViewModel.APP_PIN_LENGTH
    var pin by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    val submit: (String, String) -> Unit = { candidatePin, candidateConfirmation ->
        if (!isAuthenticating && candidatePin.length == pinLength && candidateConfirmation.length == pinLength) {
            onSubmit(candidatePin, candidateConfirmation)
        }
    }

    if (handleBack) {
        UniBackHandler(enabled = !isAuthenticating, onBack = onDismiss)
    } else if (isAuthenticating) {
        UniBackHandler(enabled = true, onBack = {})
    }

    UniScreenColumn(modifier = Modifier.imePadding()) {
        Spacer(Modifier.height(12.dp))

        // 1. Brand Presentation Header (coerente con AppUnlockScreen)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LiquidAvatar(
                size = 80.dp,
                icon = LiquidIcons.Lock,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(Res.string.ui_app_name),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.8).sp,
                        fontSize = 32.sp,
                    ),
                    color = colorScheme.onSurface,
                )

                Text(
                    text = stringResource(Res.string.ui_app_password_setup_title),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = colorScheme.primary,
                )

                Text(
                    text = stringResource(Res.string.ui_app_password_setup_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // 2. Master Glass PIN Setup Card
        LiquidCard(
            shape = RoundedRectangle(28.dp),
            contentPadding = 24.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.ui_unlock_protected_access),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = colorScheme.onSurface,
                    )
                    LiquidBadge(
                        text = stringResource(Res.string.ui_unlock_security_badge),
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                        contentColor = colorScheme.primary,
                    )
                }

                PinInputSection(
                    label = stringResource(Res.string.ui_app_password_label),
                    pin = pin,
                    onPinChange = { pin = it },
                    length = pinLength,
                    enabled = !isAuthenticating,
                )

                PinInputSection(
                    label = stringResource(Res.string.ui_app_password_confirm),
                    pin = confirmation,
                    onPinChange = { confirmation = it },
                    length = pinLength,
                    enabled = !isAuthenticating,
                    onComplete = { completedConfirmation ->
                        submit(pin, completedConfirmation)
                    },
                )

                error?.let { setupError ->
                    Text(
                        text = stringResource(
                            when (setupError) {
                                AppPasswordSetupError.TooShort -> Res.string.ui_app_password_too_short
                                AppPasswordSetupError.TooLong -> Res.string.ui_app_password_too_long
                                AppPasswordSetupError.Mismatch -> Res.string.ui_app_password_mismatch
                            },
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LiquidButton(
                        text = stringResource(Res.string.ui_app_password_enable),
                        onClick = { submit(pin, confirmation) },
                        enabled = !isAuthenticating && pin.length == pinLength && confirmation.length == pinLength,
                        isLoading = isAuthenticating,
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = LiquidIcons.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    LiquidButton(
                        text = stringResource(Res.string.ui_cancel),
                        onClick = onDismiss,
                        enabled = !isAuthenticating,
                        modifier = Modifier.fillMaxWidth(),
                        variant = LiquidButtonVariant.Text,
                    )
                }
            }
        }
    }
}

@Composable
private fun PinInputSection(
    label: String,
    pin: String,
    onPinChange: (String) -> Unit,
    length: Int,
    enabled: Boolean,
    onComplete: ((String) -> Unit)? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        LiquidOtpInput(
            otpValue = pin,
            onOtpChange = onPinChange,
            length = length,
            obscureText = true,
            enabled = enabled,
            cellWidth = 42.dp,
            cellHeight = 52.dp,
            spacing = 8.dp,
            onComplete = onComplete,
        )
    }
}
