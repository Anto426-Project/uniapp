package com.anto426.uniapp.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CacheIdentityTest {
    @Test
    fun digestUsesSha256OnEveryPlatform() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            cacheIdentityDigest("abc"),
        )
    }

    @Test
    fun formerHashCodeCollisionsRemainSeparate() {
        assertNotEquals(profileCacheKey("Aa", "news"), profileCacheKey("BB", "news"))
        assertNotEquals(datasetCacheKey("course-syllabus", "Aa"), datasetCacheKey("course-syllabus", "BB"))
        assertNotEquals(profileCacheKey(null, "news"), profileCacheKey("default", "news"))
    }
}
