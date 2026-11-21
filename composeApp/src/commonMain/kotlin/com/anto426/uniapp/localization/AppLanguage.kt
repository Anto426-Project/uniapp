package com.anto426.uniapp.localization

import androidx.compose.runtime.Composable
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

internal suspend fun readAppString(
    resource: StringResource,
    environment: ResourceEnvironment? = null,
): String = withContext(appStringReadDispatcher) {
    if (environment == null) getString(resource) else getString(environment, resource)
}

internal suspend fun readAppString(
    resource: StringResource,
    vararg formatArgs: Any,
): String = withContext(appStringReadDispatcher) {
    getString(resource, *formatArgs)
}
