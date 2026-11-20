package com.anto426.uniapp.data.local

import com.anto426.uniapp.model.didactics.SimulationItem
import com.anto426.uniapp.security.password.AppPasswordVerifier
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer

/** Canonical catalog of local values. Keys must never be duplicated inside feature packages. */
object UniAppDataKeys {
    val GradeSimulation = LocalDataKey(
        name = "didactics.grade-simulation.v1",
        serializer = ListSerializer(SimulationItem.serializer()),
        defaultValue = emptyList(),
    )
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
    val ThemePageMotion = LocalDataKey(
        name = "theme.page-motion.v1",
        serializer = com.anto426.uniapp.settings.model.PageMotionPreferences.serializer(),
        defaultValue = com.anto426.uniapp.settings.model.PageMotionPreferences(),
    )
    val ThemeCustomColor =
        LocalDataKey(
            name = "theme.custom-color",
            serializer = String.serializer().nullable,
            defaultValue = null,
        )
    val ThemeLiquidIntensity =
        LocalDataKey(
            name = "theme.liquid-intensity",
            serializer = Float.serializer(),
            defaultValue = 0.82f,
        )
    val ThemeGlassQuality =
        LocalDataKey(
            name = "theme.glass-quality",
            serializer = String.serializer().nullable,
            defaultValue = null,
        )
    val ThemeBackgroundIntensity =
        LocalDataKey(
            name = "theme.background-intensity",
            serializer = Float.serializer(),
            defaultValue = 0.90f,
        )
    val ThemeBackgroundSpeed =
        LocalDataKey(
            name = "theme.background-speed",
            serializer = Float.serializer(),
            defaultValue = 1.0f,
        )
    val ThemeBackgroundVignette =
        LocalDataKey(
            name = "theme.background-vignette",
            serializer = Boolean.serializer(),
            defaultValue = true,
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
