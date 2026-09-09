package com.anto426.uniapp.data.images

import com.anto426.uniapp.data.local.FakeUniLocalDataStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ApplicationImageStoreTest {
    @Test
    fun concurrentAvatarsShareBytesAndDecodeKeyAndPersistAcrossStoreRecreation() = runTest {
        val disk = FakeUniLocalDataStore()
        val gate = CompletableDeferred<Unit>()
        var calls = 0
        val first = ApplicationImageStore(disk, backgroundScope, HttpClient(MockEngine {
            calls++
            gate.await()
            respond(byteArrayOf(1, 2, 3))
        }), nowMillis = { 1_000L })
        val a = async { first.load(URL) }
        val b = async { first.load(URL) }
        gate.complete(Unit)
        val image = assertNotNull(a.await())
        assertSame(image, b.await())
        assertEquals(1, calls)
        first.close()
        val reopened = ApplicationImageStore(disk, backgroundScope, HttpClient(MockEngine {
            calls++
            error("Fresh disk cache must avoid the network")
        }), nowMillis = { 2_000L })
        assertContentEquals(image.bytes, reopened.load(URL)?.bytes)
        assertEquals(1, calls)
        reopened.close()
    }

    @Test
    fun changedImageAfterExpiryGetsANewDecodeKeyAndOfflineRetainsTheLastImage() = runTest {
        var now = 1_000L
        var calls = 0
        var offline = false
        val store = ApplicationImageStore(FakeUniLocalDataStore(), backgroundScope, HttpClient(MockEngine {
            calls++
            if (offline) respond("", HttpStatusCode.ServiceUnavailable) else respond(byteArrayOf(calls.toByte()))
        }), nowMillis = { now })
        val first = assertNotNull(store.load(URL))
        now += 8 * 24 * 60 * 60 * 1_000L
        val second = assertNotNull(store.load(URL))
        assertNotEquals(first.cacheKey, second.cacheKey)
        assertContentEquals(byteArrayOf(2), second.bytes)
        offline = true
        now += 8 * 24 * 60 * 60 * 1_000L
        assertSame(second, store.load(URL))
        store.close()
    }

    @Test
    fun invalidSourcesAndOversizedResponsesAreRejected() = runTest {
        var calls = 0
        val store = ApplicationImageStore(FakeUniLocalDataStore(), backgroundScope, HttpClient(MockEngine {
            calls++
            respond(ByteArray(1_048_577))
        }))
        assertNull(store.load("http://avatars.githubusercontent.com/u/1"))
        assertNull(store.load("https://example.invalid/avatar"))
        assertEquals(0, calls)
        assertNull(store.load(URL))
        assertEquals(1, calls)
        store.close()
    }

    private companion object { const val URL = "https://avatars.githubusercontent.com/u/1?v=4" }
}
