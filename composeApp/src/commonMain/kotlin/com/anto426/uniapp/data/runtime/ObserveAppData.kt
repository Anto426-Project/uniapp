package com.anto426.uniapp.data.runtime

import com.anto426.uniapp.data.UniAppDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/** Production uses the runtime's coordinator; isolated data-source fakes retain the same contract. */
fun UniAppDataSource.sharedData(scope: CoroutineScope): UniAppDataCoordinator =
    this as? UniAppDataCoordinator ?: UniAppDataCoordinator(this, scope)

fun UniAppDataCoordinator.observeIn(
    scope: CoroutineScope,
    requests: List<UniAppDataRequest<*>>,
    partial: Boolean = false,
    subscriptions: StateFlow<Int>? = null,
    render: suspend (UniAppDataSnapshot) -> Unit,
) = scope.launch {
    observe(requests, subscriptions?.map { it > 0 } ?: flowOf(true))
        .filter { partial || it.resolved }
        .distinctUntilChangedBy { it.version }
        .collect { render(it) }
}
