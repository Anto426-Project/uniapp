package com.anto426.uniapp.project.data

import com.anto426.uniapp.project.model.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class GitHubProjectRepositoryTest {
    @Test
    fun loadsAllPagesAndOnlyUniAppContributorsWithoutAuthentication() = runTest {
        val requests = mutableListOf<String>()
        val client = HttpClient(MockEngine { request ->
            val url = request.url.toString()
            requests += url
            assertNull(request.headers["Authorization"])
            assertNull(request.headers["Cookie"])
            when {
                url.contains("contributors") && url.contains("page=2") -> respond("""[{"name":"Unlinked author","email":"not-stored@example.test","contributions":2}]""")
                url.contains("contributors") -> respond("""[{"id":1,"login":"Anto426","contributions":16}]""",
                    headers = headersOf("Link", "<https://api.github.com/repos/Anto426-Project/uniapp/contributors?per_page=100&page=2>; rel=\"next\""))
                url.contains("/users/Anto426/repos") && url.contains("page=2") -> respond("""[{"id":2,"name":"two","full_name":"Anto426/two","html_url":"https://github.com/Anto426/two"}]""")
                url.contains("/users/Anto426/repos") -> respond("""[{"id":1,"name":"one","full_name":"Anto426/one","html_url":"https://github.com/Anto426/one"}]""",
                    headers = headersOf("Link", "<https://api.github.com/users/Anto426/repos?per_page=100&page=2>; rel=\"next\""))
                url.endsWith("/users/Anto426") -> respond("""{"login":"Anto426","name":"アントネッロ","bio":null,"html_url":"https://github.com/Anto426"}""")
                else -> respond("""{"id":3,"name":"uniapp","full_name":"Anto426-Project/uniapp","html_url":"https://github.com/Anto426-Project/uniapp","license":null}""")
            }
        })
        val repository = GitHubProjectRepository(client) { 1234 }
        try {
            val result = repository.load()
            assertEquals(2, result.repositories.size)
            assertEquals(2, result.contributors.size)
            assertEquals("Unlinked author", result.contributors[1].name)
            assertEquals("アントネッロ", result.author?.name)
            assertNull(result.project?.license)
            assertFalse(result.incomplete)
            assertEquals(1234L, result.fetchedAt)
            assertTrue(requests.filter { it.contains("contributors") }.all { it.contains("Anto426-Project/uniapp/") })
        } finally { repository.close() }
    }

    @Test
    fun keepsCachedContributorsOnRateLimitAndMarksSnapshotIncomplete() = runTest {
        val cached = GitHubProjectSnapshot(
            author = GitHubAuthor("Anto426", url = "https://github.com/Anto426"),
            contributors = listOf(GitHubContributor(login = "Existing", contributions = 4)), fetchedAt = 100,
        )
        val repository = GitHubProjectRepository(HttpClient(MockEngine { respond("{}", HttpStatusCode.Forbidden) }))
        try {
            val result = repository.load(cached)
            assertTrue(result.incomplete)
            assertEquals(cached.contributors, result.contributors)
            assertEquals(100L, result.fetchedAt)
        } finally { repository.close() }
    }

    @Test
    fun cancellationIsPropagated() = runTest {
        val repository = GitHubProjectRepository(HttpClient(MockEngine { throw CancellationException("cancelled") }))
        try { assertFailsWith<CancellationException> { repository.load() } }
        finally { repository.close() }
    }
}
