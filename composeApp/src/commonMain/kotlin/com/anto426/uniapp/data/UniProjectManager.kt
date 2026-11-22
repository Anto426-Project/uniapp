package com.anto426.uniapp.data

import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.data.local.UniAppDataKeys
import com.anto426.uniapp.data.local.UniLocalDataStore
import com.anto426.uniapp.localization.readAppString
import com.anto426.uniapp.project.model.GitHubAuthor
import com.anto426.uniapp.project.model.GitHubContributor
import com.anto426.uniapp.project.model.GitHubProjectSnapshot
import com.anto426.uniapp.project.model.GitHubRepositoryInfo
import com.anto426.unisdk.platform.ProjectInfo
import com.anto426.unisdk.platform.currentEpochMillis
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import uniapp.composeapp.generated.resources.*

/** Public, unauthenticated GitHub requests, isolated from university cookies and credentials. */
internal class GitHubProjectClient(
    private val client: HttpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 20_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 20_000
        }
    },
    private val now: () -> Long = ::currentEpochMillis,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun load(previous: GitHubProjectSnapshot? = null): GitHubProjectSnapshot = coroutineScope {
        suspend fun <T> section(fallback: T, load: suspend () -> T): Pair<T, Boolean> = try {
            load() to false
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            fallback to true
        }
        val authorTask = async {
            section(previous?.author) {
                json.decodeFromString<GitHubAuthor>(request("$API/users/${ProjectInfo.authorLogin}").first)
            }
        }
        val projectTask = async {
            section(previous?.project) {
                json.decodeFromString<GitHubRepositoryInfo>(request("$API/repos/${ProjectInfo.repository}").first)
            }
        }
        val repositoriesTask = async {
            section(previous?.repositories.orEmpty()) {
                pages("$API/users/${ProjectInfo.authorLogin}/repos?sort=updated&per_page=100")
                    .map { json.decodeFromJsonElement(GitHubRepositoryInfo.serializer(), it) }.distinctBy { it.id }
            }
        }
        val contributorsTask = async {
            section(previous?.contributors.orEmpty()) {
                pages("$API/repos/${ProjectInfo.repository}/contributors?anon=true&per_page=100")
                    .map { json.decodeFromJsonElement(GitHubContributor.serializer(), it) }.sortedByDescending { it.contributions }
            }
        }
        val (author, authorFailed) = authorTask.await()
        val (project, projectFailed) = projectTask.await()
        val (repositories, repositoriesFailed) = repositoriesTask.await()
        val (contributors, contributorsFailed) = contributorsTask.await()
        if (author == null && project == null && repositories.isEmpty() && contributors.isEmpty()) {
            error(readAppString(Res.string.msg_github_non_e_disponibile_riprova_piu_tardi))
        }
        val incomplete = authorFailed || projectFailed || repositoriesFailed || contributorsFailed
        GitHubProjectSnapshot(
            author,
            project,
            repositories,
            contributors,
            if (incomplete) previous?.fetchedAt ?: 0 else now(),
            incomplete,
        )
    }

    private suspend fun pages(initial: String): List<JsonElement> {
        val items = mutableListOf<JsonElement>()
        val visited = mutableSetOf<String>()
        var next: String? = initial
        while (next != null) {
            check(visited.add(next)) { "GitHub returned a pagination cycle" }
            val (body, link) = request(next)
            items.addAll(json.parseToJsonElement(body) as JsonArray)
            next = link?.split(',')?.firstOrNull { it.contains("rel=\"next\"") }
                ?.substringAfter('<')?.substringBefore('>')
        }
        return items
    }

    private suspend fun request(url: String): Pair<String, String?> {
        val parsed = Url(url)
        require(parsed.protocol.name == "https" && parsed.host == "api.github.com")
        val response = client.get(url) {
            header("Accept", "application/vnd.github+json")
            header("X-GitHub-Api-Version", "2022-11-28")
            header("User-Agent", "UniApp")
        }
        check(response.status.value in 200..299) { "GitHub HTTP ${response.status.value}" }
        return (if (response.status.value == 204) "[]" else response.bodyAsText()) to response.headers["Link"]
    }

    fun close() { client.close() }
    private companion object { const val API = "https://api.github.com" }
}

data class ProjectDataState(
    val data: GitHubProjectSnapshot? = null,
    val loading: Boolean = true,
    val stale: Boolean = false,
    val error: Boolean = false,
    val authorLogin: String = ProjectInfo.authorLogin,
    val authorUrl: String = ProjectInfo.authorUrl,
    val repositoryUrl: String = ProjectInfo.repositoryUrl,
    val issuesUrl: String = ProjectInfo.issuesUrl,
)

internal class UniProjectManager(
    private val scope: CoroutineScope,
    private val store: UniLocalDataStore,
    private val client: GitHubProjectClient = GitHubProjectClient(),
    private val policy: UniCachePolicy = UniCachePolicies.GitHubProject,
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
                    store.read(LocalDataScope.Application, UniAppDataKeys.GitHubProject)
                        .takeIf { it.fetchedAt > 0 || it.author != null }
                } catch (error: CancellationException) { throw error } catch (_: Exception) { null }
                state.value = ProjectDataState(data = cached)
                val age = cached?.let { currentEpochMillis() - it.fetchedAt }
                if (!force && cached != null && !cached.incomplete && age != null && age in 0 until policy.maxAgeMillis) {
                    state.value = ProjectDataState(data = cached, loading = false)
                    return@launch
                }
                val data = client.load(cached)
                state.value = ProjectDataState(data, loading = false, stale = data.incomplete, error = data.incomplete)
                try {
                    store.write(LocalDataScope.Application, UniAppDataKeys.GitHubProject, data)
                } catch (error: CancellationException) { throw error } catch (_: Exception) {
                }
            } catch (error: CancellationException) {
                state.value = state.value.copy(loading = false)
                throw error
            } catch (_: Exception) {
                state.value = state.value.copy(loading = false, stale = state.value.data != null, error = true)
            } finally { lock.unlock() }
        }
    }

    fun close() { client.close() }
}
