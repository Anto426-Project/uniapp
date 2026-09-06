package com.anto426.uniapp.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.local.LocalDataKey
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

internal data class ProjectInfoUiState(
    val data: GitHubProjectSnapshot? = null,
    val loading: Boolean = true,
    val stale: Boolean = false,
    val error: Boolean = false,
)

internal class ProjectInfoViewModel(
    private val store: UniLocalDataStore,
    private val repository: GitHubProjectRepository = GitHubProjectRepository(),
) : ViewModel() {
    private val lock = Mutex()
    private val state = MutableStateFlow(ProjectInfoUiState())
    val uiState = state.asStateFlow()

    init { refresh() }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            if (!lock.tryLock()) return@launch
            try {
                val cached = state.value.data ?: try {
                    store.read(LocalDataScope.Application, CacheKey).takeIf { it.fetchedAt > 0 || it.author != null }
                } catch (error: CancellationException) { throw error } catch (_: Exception) { null }
                state.value = ProjectInfoUiState(data = cached)
                val age = cached?.let { currentEpochMillis() - it.fetchedAt }
                if (!force && cached != null && !cached.incomplete && age != null && age in 0 until CACHE_MILLIS) {
                    state.value = ProjectInfoUiState(data = cached, loading = false)
                    return@launch
                }
                val data = repository.load(cached)
                state.value = ProjectInfoUiState(data, loading = false, stale = data.incomplete, error = data.incomplete)
                try {
                    store.write(LocalDataScope.Application, CacheKey, data)
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

    override fun onCleared() { repository.close() }

    private companion object {
        const val CACHE_MILLIS = 6 * 60 * 60 * 1000L
        val CacheKey = LocalDataKey("project.github.v1", GitHubProjectSnapshot.serializer(), GitHubProjectSnapshot())
    }
}
