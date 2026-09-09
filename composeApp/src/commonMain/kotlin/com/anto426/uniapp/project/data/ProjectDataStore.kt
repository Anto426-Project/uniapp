package com.anto426.uniapp.project.data

import com.anto426.uniapp.data.local.UniAppDataKeys
import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.data.local.UniLocalDataStore
import com.anto426.uniapp.project.data.GitHubProjectRepository
import com.anto426.uniapp.project.model.GitHubProjectSnapshot
import com.anto426.unisdk.platform.currentEpochMillis
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

internal data class ProjectDataState(
    val data: GitHubProjectSnapshot? = null,
    val loading: Boolean = true,
    val stale: Boolean = false,
    val error: Boolean = false,
)

internal class ProjectDataStore(
    private val scope: kotlinx.coroutines.CoroutineScope,
    private val store: UniLocalDataStore,
    private val repository: GitHubProjectRepository = GitHubProjectRepository(),
) {
    private val lock = Mutex()
    private val state = MutableStateFlow(ProjectDataState())
    val uiState = state.asStateFlow()

    init { refresh() }

    fun refresh(force: Boolean = false) {
        scope.launch {
            if (!lock.tryLock()) return@launch
            try {
                val cached = state.value.data ?: try {
                    store.read(LocalDataScope.Application, UniAppDataKeys.GitHubProject).takeIf { it.fetchedAt > 0 || it.author != null }
                } catch (error: CancellationException) { throw error } catch (_: Exception) { null }
                state.value = ProjectDataState(data = cached)
                val age = cached?.let { currentEpochMillis() - it.fetchedAt }
                if (!force && cached != null && !cached.incomplete && age != null && age in 0 until CACHE_MILLIS) {
                    state.value = ProjectDataState(data = cached, loading = false)
                    return@launch
                }
                val data = repository.load(cached)
                state.value = ProjectDataState(data, loading = false, stale = data.incomplete, error = data.incomplete)
                try {
                    store.write(LocalDataScope.Application, UniAppDataKeys.GitHubProject, data)
                } catch (error: CancellationException) { throw error } catch (_: Exception) {
                    // Keep the successfully downloaded content if local persistence fails.
                }
            } catch (error: CancellationException) {
                state.value = state.value.copy(loading = false)
                throw error
            } catch (_: Exception) {
                state.value = state.value.copy(loading = false, stale = state.value.data != null, error = true)
            } finally { lock.unlock() }
        }
    }

    fun close() { repository.close() }

    private companion object {
        const val CACHE_MILLIS = 6 * 60 * 60 * 1000L
    }
}
