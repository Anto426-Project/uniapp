package com.anto426.uniapp.localization

import androidx.compose.runtime.Composable
import kotlin.concurrent.Volatile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

/** Applies the persisted application language at the platform boundary. */
@Composable
internal expect fun BindAppLanguage(languageCode: String)

/**
 * Dispatcher used by [readAppString] to stay off `Dispatchers.Main` in production and avoid
 * deadlocking with `stringResource`. Overridden by unit tests to stay deterministic under `runTest`.
 */
internal var appStringReadDispatcher: CoroutineDispatcher = Dispatchers.Default

/**
 * Latest Compose [ResourceEnvironment] observed at the app root so background [readAppString] calls
 * (including ViewModel toasts and error messages) resolve in the active in-app locale instead of
 * falling back to the system default locale.
 */
@Volatile
internal var activeAppResourceEnvironment: ResourceEnvironment? = null

internal suspend fun readAppString(
    resource: StringResource,
    environment: ResourceEnvironment? = null,
): String = withContext(appStringReadDispatcher) {
    val effectiveEnvironment = environment ?: activeAppResourceEnvironment
    if (effectiveEnvironment == null) getString(resource) else getString(effectiveEnvironment, resource)
}

internal suspend fun readAppString(
    resource: StringResource,
    vararg formatArgs: Any,
): String = withContext(appStringReadDispatcher) {
    val effectiveEnvironment = activeAppResourceEnvironment
    if (effectiveEnvironment == null) {
        getString(resource, *formatArgs)
    } else {
        getString(effectiveEnvironment, resource, *formatArgs)
    }
}
