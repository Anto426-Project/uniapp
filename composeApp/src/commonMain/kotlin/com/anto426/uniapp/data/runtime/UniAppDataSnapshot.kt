package com.anto426.uniapp.data.runtime

/** Values remain visible during refresh; errors belong to their own dataset. */
data class UniAppDataValue<T : Any>(
    val value: T? = null,
    val refreshing: Boolean = false,
    val error: Throwable? = null,
    val revision: Long = 0,
) {
    val resolved: Boolean get() = value != null || error != null
}

class UniAppDataSnapshot internal constructor(private val values: Map<String, UniAppDataValue<*>>) {
    val resolved: Boolean get() = values.values.all { it.resolved }
    val refreshing: Boolean get() = values.values.any { it.refreshing }
    val firstError: Throwable? get() = values.values.firstNotNullOfOrNull { it.error }
    fun throwIfFailed() { firstError?.let { throw it } }
    internal val version get() = values.mapValues { (_, state) -> Triple(state.revision, state.value != null, state.error != null) }
    fun <T : Any> value(request: UniAppDataRequest<T>): T? = state(request).value
    fun <T : Any> require(request: UniAppDataRequest<T>): T =
        state(request).let { it.value ?: throw (it.error ?: IllegalStateException("Dataset ${request.id} is not ready")) }
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> state(request: UniAppDataRequest<T>): UniAppDataValue<T> =
        values[request.id] as? UniAppDataValue<T> ?: UniAppDataValue()
}
