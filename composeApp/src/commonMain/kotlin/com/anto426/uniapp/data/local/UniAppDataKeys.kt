package com.anto426.uniapp.data.local

import com.anto426.uniapp.security.password.AppPasswordVerifier
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer

/** Canonical catalog of local values. Keys must never be duplicated inside feature packages. */
object UniAppDataKeys {
    val GitHubProject = LocalDataKey(
        "project.github.v1",
        com.anto426.uniapp.project.model.GitHubProjectSnapshot.serializer(),
        com.anto426.uniapp.project.model.GitHubProjectSnapshot(),
    )

    val ThemeSelection =
        LocalDataKey(
            name = "theme.selection",
            serializer = Int.serializer(),
            defaultValue = 0,
        )
    val ThemeMode =
        LocalDataKey(
            name = "theme.mode",
            serializer = String.serializer(),
            defaultValue = "System",
        )
    val ThemeBackground =
        LocalDataKey(
            name = "theme.background",
            serializer = String.serializer(),
            defaultValue = "Aurora",
        )
    val ThemeReducedMotion =
        LocalDataKey(
            name = "theme.reduced-motion",
            serializer = Boolean.serializer(),
            defaultValue = false,
        )
    val ThemeCustomColor =
        LocalDataKey(
            name = "theme.custom-color",
            serializer = String.serializer().nullable,
            defaultValue = null,
        )
    val LanguageCode =
        LocalDataKey(
            name = "language.selected",
            serializer = String.serializer(),
            defaultValue = "",
        )
    val NotificationsEnabled =
        LocalDataKey(
            name = "notifications.enabled",
            serializer = Boolean.serializer(),
            defaultValue = false,
        )
    val BiometricUnlock =
        LocalDataKey(
            name = "security.biometric-unlock",
            serializer = Boolean.serializer(),
            defaultValue = false,
        )
    val PasswordVerifier =
        LocalDataKey(
            name = "security.password-verifier",
            serializer = AppPasswordVerifier.serializer().nullable,
            defaultValue = null,
        )
    val PasswordRetryAfter = LocalDataKey("security.password-retry-after", Long.serializer(), 0L)
}
