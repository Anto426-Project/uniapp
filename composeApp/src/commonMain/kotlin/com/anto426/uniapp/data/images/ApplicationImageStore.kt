package com.anto426.uniapp.data.images

import com.anto426.uniapp.account.platform.generateAccountStorageIdentifier
import com.anto426.uniapp.data.local.LocalDataKey
import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.data.local.UniLocalDataStore
import com.anto426.unisdk.platform.currentEpochMillis
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.Url
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

internal data class ApplicationImage(val bytes: ByteArray, val cacheKey: String)

/** Public project portraits belong to the application, independently of university accounts. */
internal class ApplicationImageStore(
    private val storage: UniLocalDataStore,
    parentScope: CoroutineScope,
    private val client: HttpClient = HttpClient { install(HttpTimeout) { requestTimeoutMillis = 15_000 } },
    private val nowMillis: () -> Long = ::currentEpochMillis,
) {
    private val scope = CoroutineScope(parentScope.coroutineContext + SupervisorJob(parentScope.coroutineContext[Job]))
    private val guard = Mutex()
    private val downloads = Semaphore(2)
    private val inFlight = mutableMapOf<String, Deferred<ApplicationImage?>>()
    private val cached = linkedMapOf<String, Pair<Long, ApplicationImage>>()
    private val writes = Mutex()

    suspend fun load(url: String): ApplicationImage? {
        val parsed = try { Url(url) } catch (_: Exception) { return null }
        if (parsed.protocol.name != "https" || parsed.host != "avatars.githubusercontent.com") return null
        val task = guard.withLock {
            cached[url]?.takeIf { nowMillis() - it.first in 0..MAX_AGE }?.let { return it.second }
            inFlight[url] ?: scope.async(start = CoroutineStart.LAZY) {
                try {
                    val record = loadRecord(url) ?: return@async null
                    guard.withLock {
                        val previous = cached[url]?.second
                        val image = previous?.takeIf { it.bytes.contentEquals(record.bytes) }
                            ?: ApplicationImage(record.bytes, "project-avatar:${generateAccountStorageIdentifier()}")
                        if (cached.size >= MAX_IMAGES && url !in cached) cached.remove(cached.keys.first())
                        cached[url] = record.savedAt to image
                        image
                    }
                } finally {
                    withContext(NonCancellable) { guard.withLock { inFlight.remove(url) } }
                }
            }.also { inFlight[url] = it; it.start() }
        }
        return task.await()
    }

    private suspend fun loadRecord(url: String): CachedApplicationImage? {
        val key = imageKey(url)
        val previous = try { storage.read(LocalDataScope.Application, key).takeIf { it.source == url } }
            catch (error: CancellationException) { throw error } catch (_: Exception) { null }
        return try {
            if (previous != null && nowMillis() - previous.savedAt in 0..MAX_AGE) previous
            else downloads.withPermit {
                val response = client.get(url) { this.url.parameters["s"] = "256" }
                check(response.status.value in 200..299)
                val channel = response.bodyAsChannel()
                val bytes = try { channel.readRemaining(MAX_BYTES + 1).readByteArray() }
                    finally { channel.cancel(null) }
                check(bytes.isNotEmpty() && bytes.size <= MAX_BYTES)
                val record = CachedApplicationImage(url, nowMillis(), bytes)
                try {
                    writes.withLock {
                        val index = storage.read(LocalDataScope.Application, IndexKey)
                        storage.write(LocalDataScope.Application, key, record)
                        val retained = (index.filterNot { it == url } + url).takeLast(MAX_IMAGES)
                        storage.write(LocalDataScope.Application, IndexKey, retained)
                        (index - retained.toSet()).forEach { storage.remove(LocalDataScope.Application, imageKey(it)) }
                    }
                } catch (error: CancellationException) { throw error } catch (_: Exception) { /* The successful response remains usable. */ }
                record
            }
        } catch (error: CancellationException) { throw error } catch (_: Exception) { previous }
    }

    fun close() { scope.cancel(); client.close() }

    private fun imageKey(url: String): LocalDataKey<CachedApplicationImage> {
        var fingerprint = 0xcbf29ce484222325uL
        url.encodeToByteArray().forEach { fingerprint = (fingerprint xor it.toUByte().toULong()) * 0x100000001b3uL }
        return LocalDataKey("project.image.${fingerprint.toString(16)}", CachedApplicationImage.serializer(), CachedApplicationImage())
    }

    private companion object {
        const val MAX_IMAGES = 32
        const val MAX_BYTES = 1_048_576L
        const val MAX_AGE = 7 * 24 * 60 * 60 * 1000L
        val IndexKey = LocalDataKey("project.images.index", ListSerializer(String.serializer()), emptyList())
    }
}

@Serializable
private data class CachedApplicationImage(val source: String = "", val savedAt: Long = 0, val bytes: ByteArray = byteArrayOf())
