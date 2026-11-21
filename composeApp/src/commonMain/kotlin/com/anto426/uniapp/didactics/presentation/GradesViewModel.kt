package com.anto426.uniapp.didactics.presentation

import com.anto426.uniapp.localization.readAppString
import uniapp.composeapp.generated.resources.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.liquidmonet.components.charts.model.LiquidChartEntry
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.observeIn
import com.anto426.uniapp.data.toGradeExams
import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.didactics.data.GradeSimulationRepository
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.model.didactics.GradeExam
import com.anto426.uniapp.model.didactics.SimulationItem
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.round

enum class TargetFeasibility {
    EASY,
    FEASIBLE,
    HARD,
    IMPOSSIBLE,
}

data class GradeTierInfo(
    val label: String,
    val examCount: Float,
    val percentage: Int,
)

data class GradesUiState(
    val selectedTab: Int = 0,
    val officialAverage: Double? = null,
    val officialDegreeBase: Double? = null,
    val chartBaselineCount: Int? = null,
    val chartBaselineAverage: Double? = null,
    val currentExams: List<GradeExam> = emptyList(),
    val simulationItems: List<SimulationItem> = emptyList(),
    val targetDegree: Int = 110,
    val thesisPoints: Int = 5,
    val bonusPoints: Int = 0,
    val degreeCfuTarget: Int = 180,
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
) {
    /** Charts use a snapshot: hypothetical exams never mutate the recorded career. */
    fun chartState(projected: Boolean): GradesUiState = if (!projected) {
        copy(simulationItems = emptyList())
    } else {
        copy(
            currentExams = currentExams + activeSimulatedItems.map { GradeExam("(Sim.) " + it.name, it.grade, it.cfu) },
            simulationItems = emptyList(),
            chartBaselineCount = currentExams.size,
            chartBaselineAverage = currentAverage,
            officialAverage = projectedAverage,
            officialDegreeBase = projectedGraduationBase,
        )
    }

    val highestGradeCourses: String get() = currentExams.filter { it.grade == highestGrade }
        .joinToString(limit = 2) { it.name }
    val recentTrend: Float get() {
        val entries = weightedAverageEntries
        return if (entries.size < 2) 0f else entries.last().value - entries[(entries.lastIndex - 3).coerceAtLeast(0)].value
    }

    val totalCurrentCfu: Int get() = currentExams.sumOf { it.cfu }
    val currentWeightedSum: Int get() = currentExams.sumOf { it.grade.coerceAtMost(30) * it.cfu }
    val currentAverage: Double
        get() = officialAverage?.takeIf { it.isFinite() && it > 0.0 }
            ?: if (totalCurrentCfu == 0) 0.0 else currentWeightedSum.toDouble() / totalCurrentCfu

    val currentArithmeticAverage: Double
        get() = if (currentExams.isEmpty()) 0.0 else currentExams.map { it.grade.coerceAtMost(30) }.average()

    val currentGraduationBase: Double get() = officialDegreeBase?.takeIf { it.isFinite() && it > 0.0 }
        ?: (currentAverage * 110.0) / 30.0

    val lodeCount: Int get() = currentExams.count { it.grade >= 31 }
    val highestGrade: Int get() = currentExams.maxOfOrNull { it.grade } ?: 0
    val lowestGrade: Int get() = currentExams.minOfOrNull { it.grade } ?: 0

    val activeSimulatedItems: List<SimulationItem> get() = simulationItems.filter { it.isEnabled && it.grade >= 18 }
    val activeSimulatedCount: Int get() = activeSimulatedItems.size
    val activeSimulatedCfu: Int get() = activeSimulatedItems.sumOf { it.cfu }
    val totalProjectedCfu: Int get() = totalCurrentCfu + activeSimulatedCfu

    val projectedAverage: Double
        get() {
            val totalCfu = totalProjectedCfu
            if (totalCfu == 0) return currentAverage
            val simulatedWeightedSum = activeSimulatedItems.sumOf { it.grade.coerceAtMost(30) * it.cfu }
            return (currentAverage * totalCurrentCfu + simulatedWeightedSum) / totalCfu
        }

    val projectedGraduationBase: Double get() = currentGraduationBase + (projectedAverage - currentAverage) * 110.0 / 30.0
    val deltaAverage: Double get() = projectedAverage - currentAverage
    val deltaGraduationBase: Double get() = projectedGraduationBase - currentGraduationBase

    val careerProgress: Float
        get() = if (degreeCfuTarget <= 0) 0f else (totalProjectedCfu.toFloat() / degreeCfuTarget.toFloat()).coerceIn(0f, 1f)

    val remainingDegreeCfu: Int get() = (degreeCfuTarget - totalCurrentCfu).coerceAtLeast(0)

    val targetScoreEffective: Double get() = if (targetDegree >= 110) 110.0 else targetDegree.toDouble()
    val neededGraduationBase: Double get() = (targetScoreEffective - thesisPoints - bonusPoints).coerceAtLeast(66.0)
    val neededFinalAverage: Double get() = (neededGraduationBase * 30.0) / 110.0

    val requiredRemainingAverage: Double
        get() {
            val remaining = remainingDegreeCfu
            if (remaining <= 0) return currentAverage
            if (totalCurrentCfu <= 0) return neededFinalAverage
            val targetTotalWeightedSum = neededFinalAverage * degreeCfuTarget
            val currentSum = currentAverage * totalCurrentCfu
            return ((targetTotalWeightedSum - currentSum) / remaining).coerceAtLeast(18.0)
        }

    val isTargetFeasible: Boolean get() = requiredRemainingAverage <= 30.0

    val feasibilityLevel: TargetFeasibility
        get() = when {
            remainingDegreeCfu == 0 && (currentGraduationBase + thesisPoints + bonusPoints >= targetScoreEffective) -> TargetFeasibility.EASY
            requiredRemainingAverage <= (if (currentAverage > 0.0) currentAverage else 30.0) -> TargetFeasibility.EASY
            requiredRemainingAverage <= 28.5 -> TargetFeasibility.FEASIBLE
            requiredRemainingAverage <= 30.0 -> TargetFeasibility.HARD
            else -> TargetFeasibility.IMPOSSIBLE
        }

    val gradeMin: Float
        get() {
            val all = currentExams.map { it.grade.toFloat().coerceAtMost(30f) } +
                activeSimulatedItems.map { it.grade.toFloat().coerceAtMost(30f) } + weightedChartValues
            return ((all.minOrNull() ?: 18f) - 1f).coerceAtLeast(17f)
        }

    val gradeMax: Float
        get() {
            val all = currentExams.map { it.grade.toFloat().coerceAtMost(30f) } +
                activeSimulatedItems.map { it.grade.toFloat().coerceAtMost(30f) } + weightedChartValues
            return ((all.maxOrNull() ?: 30f) + 1.2f).coerceAtMost(32f)
        }

    val cfuMax: Float
        get() {
            val allCfu = (currentExams.map { it.cfu } + activeSimulatedItems.map { it.cfu }).filter { it > 0 }
            val cfuGroups = allCfu.groupBy { it }
            return (cfuGroups.values.maxOfOrNull { it.size } ?: 0).toFloat()
        }

    private val chartExams: List<GradeExam> get() = currentExams + activeSimulatedItems.map {
        GradeExam("(Sim.) " + it.name, it.grade, it.cfu)
    }

    /** The last recorded point and all following projections share the official baseline. */
    private val weightedChartValues: List<Float> get() {
        val anchorCount = chartBaselineCount ?: currentExams.size
        val anchorAverage = (chartBaselineAverage ?: officialAverage)?.takeIf { it.isFinite() && it > 0.0 }
        var weightedSum = 0.0
        var credits = 0
        return chartExams.mapIndexed { index, exam ->
            credits += exam.cfu
            weightedSum += exam.grade.coerceAtMost(30) * exam.cfu
            if (index + 1 == anchorCount && anchorAverage != null) {
                weightedSum = anchorAverage * credits
            }
            if (credits > 0) (weightedSum / credits).toFloat() else 0f
        }
    }

    val singleGradeEntries: List<LiquidChartEntry> get() {
        val weighted = weightedChartValues
        return chartExams.mapIndexed { index, exam ->
            LiquidChartEntry(exam.name, exam.grade.coerceAtMost(30).toFloat(), weighted[index])
        }
    }

    val weightedAverageEntries: List<LiquidChartEntry> get() {
        val weighted = weightedChartValues
        return chartExams.mapIndexed { index, exam -> LiquidChartEntry(exam.name, weighted[index]) }
    }

    val arithmeticAverageEntries: List<LiquidChartEntry>
        get() {
            val list = mutableListOf<LiquidChartEntry>()
            var runningSum = 0
            var count = 0
            currentExams.forEach { exam ->
                runningSum += exam.grade.coerceAtMost(30)
                count++
                val avg = runningSum.toFloat() / count
                list += LiquidChartEntry(label = exam.name, value = avg)
            }
            activeSimulatedItems.forEach { item ->
                runningSum += item.grade.coerceAtMost(30)
                count++
                val avg = runningSum.toFloat() / count
                list += LiquidChartEntry(label = "(Sim.) " + item.name, value = avg)
            }
            return list
        }

    val cfuEntries: List<LiquidChartEntry>
        get() {
            val allCfu = (currentExams.map { it.cfu } + activeSimulatedItems.map { it.cfu }).filter { it > 0 }
            val cfuGroups = allCfu.groupBy { it }.entries.sortedBy { it.key }
            return cfuGroups.map { (cfu, list) ->
                LiquidChartEntry(label = "$cfu CFU", value = list.size.toFloat())
            }
        }

    val gradeTiers: List<GradeTierInfo>
        get() {
            val total = currentExams.size
            if (total == 0) return emptyList()
            val t30 = currentExams.count { it.grade >= 30 }
            val t27 = currentExams.count { it.grade in 27..29 }
            val t24 = currentExams.count { it.grade in 24..26 }
            val t18 = currentExams.count { it.grade in 18..23 }
            return listOf(
                GradeTierInfo("30 e 30L", t30.toFloat(), ((t30.toFloat() / total) * 100).toInt()),
                GradeTierInfo("27 - 29", t27.toFloat(), ((t27.toFloat() / total) * 100).toInt()),
                GradeTierInfo("24 - 26", t24.toFloat(), ((t24.toFloat() / total) * 100).toInt()),
                GradeTierInfo("18 - 23", t18.toFloat(), ((t18.toFloat() / total) * 100).toInt()),
            )
        }
}

internal class GradesViewModel(
    dataSource: UniGateway,
    private val simulationRepository: GradeSimulationRepository,
    private val simulationOwner: LocalDataScope,
    private val toastSink: AppToastSink,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(GradesUiState())
    private var simulationDraft: Map<String, SimulationItem>? = null
    private var simulationWriteRevision = 0L
    val uiState: StateFlow<GradesUiState> = mutableUiState.asStateFlow()

    private val repository = dataSource.asUniRepository(viewModelScope, simulations = simulationRepository)
    private val sharedData = repository.engine
    private val dataRequests = listOf(UniDatasets.Career, UniDatasets.StudyPlan, UniDatasets.Exams)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        mutableUiState.value = mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
        try {
            val career = snapshot.require(UniDatasets.Career)
            val studyPlan = runCatching { snapshot.require(UniDatasets.StudyPlan) }.getOrNull()
            val examRounds = runCatching { snapshot.require(UniDatasets.Exams) }.getOrNull().orEmpty()

            val currentExams = career.toGradeExams()
            val targetCfu = career.cfuTarget ?: 180

            val draft = simulationDraft ?: simulationRepository.load(simulationOwner)
                .associateBy { it.id }.also { simulationDraft = it }
            val passedTitles = currentExams.map { it.name.normalizedTitle() }.toSet()

            val planItems = studyPlan?.courses.orEmpty()
                .filterNot { it.completed || it.title.normalizedTitle() in passedTitles }
                .map { course ->
                    val id = course.adsceId?.takeIf { it.isNotBlank() }?.let { "plan-id:$it" }
                        ?: "plan-title:${course.title.normalizedTitle()}"
                    suggestedSimulation(id, course.title, course.cfu ?: 6, draft)
                }
                .distinctBy { it.id }

            val suggested = planItems.ifEmpty {
                examRounds.filterNot { it.booked || it.courseName.normalizedTitle() in passedTitles }
                    .map { round ->
                        suggestedSimulation("round:${round.courseName.normalizedTitle()}", round.courseName, 6, draft)
                    }
                    .distinctBy { it.id }
            }

            mutableUiState.update { state ->
                state.copy(
                    currentExams = currentExams,
                    officialAverage = career.averageValue,
                    officialDegreeBase = career.degreeBaseValue,
                    simulationItems = suggested + draft.values.filter { it.isCustom },
                    degreeCfuTarget = targetCfu,
                    loadState = FeatureLoadState.Content,
                )
            }
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage { readAppString(Res.string.msg_impossibile_caricare_voti_e_simulazione) },
            )
        }

    }

    fun refresh(force: Boolean = false) {
        sharedData.refresh(dataRequests, force)
    }

    fun selectTab(index: Int) {
        mutableUiState.value = mutableUiState.value.copy(selectedTab = index.coerceIn(0, 3))
    }

    private fun editSimulation(transform: (List<SimulationItem>) -> List<SimulationItem>) {
        val draft = simulationDraft ?: return
        val previous = mutableUiState.value.simulationItems
        val updated = transform(previous)
        if (previous == updated) return

        val previousById = previous.associateBy { it.id }
        val removedIds = previousById.keys - updated.map { it.id }.toSet()
        val edits = updated.filter { previousById[it.id] != it }.associateBy { it.id }
        val nextDraft = (draft - removedIds) + edits
        simulationDraft = nextDraft
        mutableUiState.update { it.copy(simulationItems = updated) }

        val revision = ++simulationWriteRevision
        val write = simulationRepository.save(simulationOwner, nextDraft.values.toList())
        viewModelScope.launch {
            try {
                write.await()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                val message = readAppString(Res.string.msg_impossibile_salvare_la_simulazione)
                if (revision == simulationWriteRevision) toastSink.error(message)
            }
        }
    }

    fun toggleSimulationItem(id: String) = editSimulation { items ->
        items.map { if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it }
    }

    fun updateSimulatedGrade(id: String, grade: Int) = editSimulation { items ->
        val safeGrade = grade.coerceIn(18, 31)
        items.map { if (it.id == id) it.copy(grade = safeGrade, isEnabled = true) else it }
    }

    fun updateSimulatedCfu(id: String, cfu: Int) = editSimulation { items ->
        val safeCfu = cfu.coerceIn(1, 30)
        items.map { if (it.id == id) it.copy(cfu = safeCfu) else it }
    }

    fun addCustomExam(name: String, cfu: Int, grade: Int) = editSimulation { items ->
        items + SimulationItem(
            id = "custom:${kotlin.random.Random.nextLong()}",
            name = name.trim().ifBlank { "Esame Personalizzato" },
            cfu = cfu.coerceIn(1, 30),
            grade = grade.coerceIn(18, 31),
            isEnabled = true,
            isCustom = true,
        )
    }

    fun removeCustomExam(id: String) = editSimulation { items ->
        items.filterNot { it.id == id && it.isCustom }
    }

    fun setAllSimulatedGrades(grade: Int) = editSimulation { items ->
        val safeGrade = grade.coerceIn(18, 31)
        items.map { it.copy(grade = safeGrade, isEnabled = true) }
    }

    fun applyCurrentAveragePreset() {
        val avg = mutableUiState.value.currentAverage
        val nearestGrade = if (avg > 0.0) round(avg).toInt().coerceIn(18, 30) else 27
        setAllSimulatedGrades(nearestGrade)
    }

    fun resetSimulation() = editSimulation { items ->
        items.map { it.copy(isEnabled = false) }
    }

    fun updateTargetDegree(target: Int) {
        mutableUiState.value = mutableUiState.value.copy(targetDegree = target.coerceIn(66, 111))
    }

    fun updateThesisPoints(points: Int) {
        mutableUiState.value = mutableUiState.value.copy(thesisPoints = points.coerceIn(0, 15))
    }

    fun updateBonusPoints(points: Int) {
        mutableUiState.value = mutableUiState.value.copy(bonusPoints = points.coerceIn(0, 10))
    }
}

private fun suggestedSimulation(
    id: String,
    name: String,
    credits: Int,
    draft: Map<String, SimulationItem>,
): SimulationItem {
    val saved = draft[id]
    return SimulationItem(
        id = id,
        name = name,
        cfu = saved?.cfu ?: credits,
        grade = saved?.grade ?: 28,
        isEnabled = saved?.isEnabled ?: false,
    )
}

private fun String.normalizedTitle(): String = trim().lowercase()

private fun String?.academicNumber(): Double? = this?.trim()?.replace(',', '.')
    ?.substringBefore('/')?.trim()?.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 }
