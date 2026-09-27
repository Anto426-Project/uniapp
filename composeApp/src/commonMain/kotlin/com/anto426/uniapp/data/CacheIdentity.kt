package com.anto426.uniapp.data

/** Versioned, domain-separated cache addresses; previous hashCode keys are intentionally ignored. */
internal fun profileCacheKey(profileId: String?, key: String): String {
    val owner = profileId?.let { "value:${it.length}:$it" } ?: "absent"
    return "profile-v2-${cacheIdentityDigest(owner)}-${cacheIdentityDigest(key)}"
}

internal fun datasetCacheKey(kind: String, id: String): String =
    "$kind-v2-${cacheIdentityDigest(id)}"

internal expect fun cacheIdentityDigest(value: String): String
