package com.anto426.uniapp.didactics.data

import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.data.local.UniAppDataKeys
import com.anto426.uniapp.data.local.UniLocalDataStore
import com.anto426.uniapp.model.didactics.SimulationItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class GradeSimulationRepository(
    private val localDataStore: UniLocalDataStore,
    private val appScope: CoroutineScope,
) {
    private val access = Mutex()

    suspend fun load(owner: LocalDataScope): List<SimulationItem> = access.withLock {
        localDataStore.read(owner, UniAppDataKeys.GradeSimulation)
    }

    // Enqueue before returning so reopening the route reads after the latest submitted edit.
    fun save(owner: LocalDataScope, items: List<SimulationItem>): Deferred<Unit> =
        appScope.async(start = CoroutineStart.UNDISPATCHED) {
            access.withLock { localDataStore.write(owner, UniAppDataKeys.GradeSimulation, items) }
        }
}
