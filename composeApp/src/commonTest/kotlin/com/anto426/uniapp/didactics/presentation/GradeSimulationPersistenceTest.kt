package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.FakeUniAppDataSource
import com.anto426.uniapp.data.local.FakeUniLocalDataStore
import com.anto426.uniapp.data.local.LocalDataKey
import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.data.local.UniLocalDataStore
import com.anto426.uniapp.didactics.data.GradeSimulationRepository
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.unisdk.model.StudyPlan
import com.anto426.unisdk.model.StudyPlanCourse
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GradeSimulationPersistenceTest {
    private val source = object : FakeUniAppDataSource() {
        override suspend fun loadStudyPlan(forceRefresh: Boolean) =
            StudyPlan(listOf(StudyPlanCourse("Analisi", year = 1, adsceId = "A", cfu = 9)))
    }
    private val owner = LocalDataScope.Profile("account", "career-a")

    @Test
    fun restoresEditsAndCustomExamsOnlyForTheOwningCareer() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val storage = FakeUniLocalDataStore()
        val firstRepository = GradeSimulationRepository(storage, backgroundScope)
        val first = model(firstRepository)
        val models = mutableListOf(first)
        fun reopen(scope: LocalDataScope) =
            model(GradeSimulationRepository(storage, backgroundScope), scope).also { models += it }
        try {
            val planned = first.awaitContent().simulationItems.single()
            first.updateSimulatedGrade(planned.id, 31)
            first.addCustomExam("Tirocinio", cfu = 6, grade = 27)
            val custom = first.uiState.value.simulationItems.single { it.isCustom }
            first.updateSimulatedCfu(custom.id, 8)
            // Wait for queued writes, then restore using a fresh repository.
            assertEquals(2, firstRepository.load(owner).size)
            first.viewModelScope.cancel()

            val reopened = reopen(owner)
            val restored = reopened.awaitContent()
            assertEquals(listOf(planned.copy(grade = 31, isEnabled = true), custom.copy(cfu = 8)), restored.simulationItems)
            assertTrue(restored.currentExams.isEmpty())
            assertEquals(listOf(planned), reopen(owner.copy(profileId = "career-b")).awaitContent().simulationItems)
            assertEquals(listOf(planned), reopen(owner.copy(accountId = "other-account")).awaitContent().simulationItems)

            reopened.resetSimulation()
            reopened.removeCustomExam(custom.id)
            val saved = firstRepository.load(owner).single()
            assertFalse(saved.isEnabled)
            assertEquals(31, saved.grade)
        } finally {
            models.forEach { it.viewModelScope.cancel() }
            Dispatchers.resetMain()
        }
    }

    @Test
    fun reopeningWaitsForTheLastEditEvenAfterTheOriginalViewModelCloses() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val allowWrite = CompletableDeferred<Unit>()
        val backing = FakeUniLocalDataStore()
        val storage = object : UniLocalDataStore by backing {

            override suspend fun <T> write(scope: LocalDataScope, key: LocalDataKey<T>, value: T) {
                allowWrite.await()
                backing.write(scope, key, value)
            }
        }
        val repository = GradeSimulationRepository(storage, backgroundScope)
        val first = model(repository)
        var reopened: GradesViewModel? = null
        try {
            val planned = first.awaitContent().simulationItems.single()
            first.updateSimulatedGrade(planned.id, 24)
            first.updateSimulatedGrade(planned.id, 30)
            first.viewModelScope.cancel()

            val nextModel = model(repository).also { reopened = it }
            val restoring = async { nextModel.awaitContent() }
            runCurrent()
            assertFalse(restoring.isCompleted)
            allowWrite.complete(Unit)
            assertEquals(30, restoring.await().simulationItems.single().grade)
        } finally {
            first.viewModelScope.cancel()
            reopened?.viewModelScope?.cancel()
            Dispatchers.resetMain()
        }
    }

    private fun model(
        repository: GradeSimulationRepository,
        scope: LocalDataScope = owner,
    ) = GradesViewModel(source, repository, scope, AppToastSink.None)

    private suspend fun GradesViewModel.awaitContent(): GradesUiState =
        uiState.first { it.loadState == FeatureLoadState.Content }
}
