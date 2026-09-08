package com.anto426.uniapp.project.data

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import com.anto426.uniapp.project.model.*
import com.anto426.unisdk.platform.ProjectInfo
import com.anto426.unisdk.platform.currentEpochMillis
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray

/** Public, unauthenticated GitHub requests, isolated from university cookies and credentials. */
internal class GitHubProjectRepository(
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

    suspend fun load(previous: GitHubProjectSnapshot? = null): GitHubProjectSnapshot {
        var incomplete = false
        suspend fun <T> section(fallback: T, load: suspend () -> T): T = try {
            load()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            incomplete = true
            fallback
        }
        val author = section(previous?.author) {
            json.decodeFromString<GitHubAuthor>(request("$API/users/${ProjectInfo.authorLogin}").first)
        }
        val project = section(previous?.project) {
            json.decodeFromString<GitHubRepositoryInfo>(request("$API/repos/${ProjectInfo.repository}").first)
        }
        val repositories = section(previous?.repositories.orEmpty()) {
            pages("$API/users/${ProjectInfo.authorLogin}/repos?sort=updated&per_page=100")
                .map { json.decodeFromJsonElement(GitHubRepositoryInfo.serializer(), it) }.distinctBy { it.id }
        }
        val contributors = section(previous?.contributors.orEmpty()) {
            pages("$API/repos/${ProjectInfo.repository}/contributors?anon=true&per_page=100")
                .map { json.decodeFromJsonElement(GitHubContributor.serializer(), it) }
                .sortedByDescending { it.contributions }
        }
        if (author == null && project == null && repositories.isEmpty() && contributors.isEmpty()) {
            error(getString(Res.string.msg_github_non_e_disponibile_riprova_piu_tardi))
        }
        return GitHubProjectSnapshot(author, project, repositories, contributors,
            if (incomplete) previous?.fetchedAt ?: 0 else now(), incomplete)
    }

    private suspend fun pages(initial: String): List<kotlinx.serialization.json.JsonElement> {
        val items = mutableListOf<kotlinx.serialization.json.JsonElement>()
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
