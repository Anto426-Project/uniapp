package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.liquidmonet.components.charts.LiquidChartEntry
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.toGradeExams
import com.anto426.uniapp.model.didactics.GradeExam
import com.anto426.uniapp.model.didactics.SimulationItem
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val currentExams: List<GradeExam> = emptyList(),
    val simulationItems: List<SimulationItem> = emptyList(),
    val targetDegree: Int = 110,
    val thesisPoints: Int = 5,
    val bonusPoints: Int = 0,
    val degreeCfuTarget: Int = 180,
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
) {
    val totalCurrentCfu: Int get() = currentExams.sumOf { it.cfu }
    val currentWeightedSum: Int get() = currentExams.sumOf { it.grade.coerceAtMost(30) * it.cfu }
    val currentAverage: Double
        get() = if (totalCurrentCfu == 0) 0.0 else currentWeightedSum.toDouble() / totalCurrentCfu

    val currentArithmeticAverage: Double
        get() = if (currentExams.isEmpty()) 0.0 else currentExams.map { it.grade.coerceAtMost(30) }.average()

    val currentGraduationBase: Double get() = (currentAverage * 110.0) / 30.0

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
            return (currentWeightedSum + simulatedWeightedSum).toDouble() / totalCfu
        }

    val projectedGraduationBase: Double get() = (projectedAverage * 110.0) / 30.0
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
                activeSimulatedItems.map { it.grade.toFloat().coerceAtMost(30f) }
            return ((all.minOrNull() ?: 18f) - 1f).coerceAtLeast(17f)
        }

    val gradeMax: Float
        get() {
            val all = currentExams.map { it.grade.toFloat().coerceAtMost(30f) } +
                activeSimulatedItems.map { it.grade.toFloat().coerceAtMost(30f) }
            return ((all.maxOrNull() ?: 30f) + 1.2f).coerceAtMost(32f)
        }

    val cfuMax: Float
        get() {
            val all = currentExams.map { it.cfu } + simulationItems.map { it.cfu }
            return ((all.maxOrNull() ?: 6) * 1.25f).coerceAtLeast(1f)
        }

    val singleGradeEntries: List<LiquidChartEntry>
        get() {
            val list = mutableListOf<LiquidChartEntry>()
            var runningWeightedSum = 0
            var runningCfu = 0
            currentExams.forEach { exam ->
                runningWeightedSum += exam.grade.coerceAtMost(30) * exam.cfu
                runningCfu += exam.cfu
                val weighted = if (runningCfu > 0) runningWeightedSum.toFloat() / runningCfu else 0f
                list += LiquidChartEntry(
                    label = exam.name,
                    value = exam.grade.coerceAtMost(30).toFloat(),
                    secondaryValue = weighted,
                )
            }
            activeSimulatedItems.forEach { item ->
                runningWeightedSum += item.grade.coerceAtMost(30) * item.cfu
                runningCfu += item.cfu
                val weighted = if (runningCfu > 0) runningWeightedSum.toFloat() / runningCfu else 0f
                list += LiquidChartEntry(
                    label = "(Sim.) " + item.name,
                    value = item.grade.coerceAtMost(30).toFloat(),
                    secondaryValue = weighted,
                )
            }
            return list
        }

    val weightedAverageEntries: List<LiquidChartEntry>
        get() {
            val list = mutableListOf<LiquidChartEntry>()
            var runningWeightedSum = 0
            var runningCfu = 0
            currentExams.forEach { exam ->
                runningWeightedSum += exam.grade.coerceAtMost(30) * exam.cfu
                runningCfu += exam.cfu
                val avg = if (runningCfu > 0) runningWeightedSum.toFloat() / runningCfu else 0f
                list += LiquidChartEntry(label = exam.name, value = avg)
            }
            activeSimulatedItems.forEach { item ->
                runningWeightedSum += item.grade.coerceAtMost(30) * item.cfu
                runningCfu += item.cfu
                val avg = if (runningCfu > 0) runningWeightedSum.toFloat() / runningCfu else 0f
                list += LiquidChartEntry(label = "(Sim.) " + item.name, value = avg)
            }
            return list
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
            val list = mutableListOf<LiquidChartEntry>()
            currentExams.forEach { exam ->
                list += LiquidChartEntry(label = exam.name, value = exam.cfu.toFloat())
            }
            activeSimulatedItems.forEach { item ->
                list += LiquidChartEntry(label = "(Sim.) " + item.name, value = item.cfu.toFloat())
            }
            return list
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

class GradesViewModel(
    private val dataSource: UniAppDataSource,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(GradesUiState())
    val uiState: StateFlow<GradesUiState> = mutableUiState.asStateFlow()

    init { refresh() }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(loadState = FeatureLoadState.Loading, errorMessage = null)
            try {
                val career = dataSource.loadCareer(force)
                val studyPlan = runCatching { dataSource.loadStudyPlan(force) }.getOrNull()
                val examRounds = runCatching { dataSource.loadExamRounds(force) }.getOrNull().orEmpty()

                val currentExams = career.toGradeExams()
                val targetCfu = career.cfuTarget ?: 180

                val existingCustomItems = mutableUiState.value.simulationItems.filter { it.isCustom }
                val passedTitles = currentExams.map { it.name.trim().lowercase() }.toSet()

                val planSimulationItems = studyPlan?.courses
                    ?.filterNot { it.completed || it.title.trim().lowercase() in passedTitles }
                    ?.map { course ->
                        val cfuVal = course.cfu ?: 6
                        SimulationItem(
                            id = course.adsceId?.takeIf { it.isNotBlank() } ?: "plan_${course.title.hashCode()}",
                            name = course.title,
                            cfu = cfuVal,
                            grade = 28,
                            isEnabled = false,
                            isCustom = false,
                        )
                    }.orEmpty()

                val fallbackItems = if (planSimulationItems.isEmpty()) {
                    examRounds.filterNot { it.booked || it.courseName.trim().lowercase() in passedTitles }
                        .distinctBy { it.courseName }
                        .map { round ->
                            SimulationItem(
                                id = "round_${round.courseName.hashCode()}",
                                name = round.courseName,
                                cfu = 6,
                                grade = 28,
                                isEnabled = false,
                                isCustom = false,
                            )
                        }
                } else emptyList()

                val combinedSimulationItems = (planSimulationItems.ifEmpty { fallbackItems }) + existingCustomItems

                mutableUiState.value = mutableUiState.value.copy(
                    currentExams = currentExams,
                    simulationItems = combinedSimulationItems,
                    degreeCfuTarget = targetCfu,
                    loadState = if (currentExams.isEmpty() && combinedSimulationItems.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(
                    loadState = FeatureLoadState.Error,
                    errorMessage = error.userMessage("Impossibile caricare voti e simulazione."),
                )
            }
        }
    }

    fun selectTab(index: Int) {
        mutableUiState.value = mutableUiState.value.copy(selectedTab = index.coerceIn(0, 2))
    }

    fun toggleSimulationItem(id: String) {
        mutableUiState.value = mutableUiState.value.copy(
            simulationItems = mutableUiState.value.simulationItems.map { item ->
                if (item.id == id) item.copy(isEnabled = !item.isEnabled) else item
            }
        )
    }

    fun updateSimulatedGrade(id: String, grade: Int) {
        val safeGrade = grade.coerceIn(18, 31)
        mutableUiState.value = mutableUiState.value.copy(
            simulationItems = mutableUiState.value.simulationItems.map { item ->
                if (item.id == id) item.copy(grade = safeGrade, isEnabled = true) else item
            }
        )
    }

    fun updateSimulatedCfu(id: String, cfu: Int) {
        val safeCfu = cfu.coerceIn(1, 30)
        mutableUiState.value = mutableUiState.value.copy(
            simulationItems = mutableUiState.value.simulationItems.map { item ->
                if (item.id == id) item.copy(cfu = safeCfu) else item
            }
        )
    }

    fun addCustomExam(name: String, cfu: Int, grade: Int) {
        val cleanName = name.trim().ifBlank { "Esame Personalizzato" }
        val newItem = SimulationItem(
            id = "custom_${cleanName.hashCode()}_${kotlin.random.Random.nextInt(1000, 9999)}",
            name = cleanName,
            cfu = cfu.coerceIn(1, 30),
            grade = grade.coerceIn(18, 31),
            isEnabled = true,
            isCustom = true,
        )
        mutableUiState.value = mutableUiState.value.copy(
            simulationItems = mutableUiState.value.simulationItems + newItem,
        )
    }

    fun removeCustomExam(id: String) {
        mutableUiState.value = mutableUiState.value.copy(
            simulationItems = mutableUiState.value.simulationItems.filterNot { it.id == id },
        )
    }

    fun setAllSimulatedGrades(grade: Int) {
        val safeGrade = grade.coerceIn(18, 31)
        mutableUiState.value = mutableUiState.value.copy(
            simulationItems = mutableUiState.value.simulationItems.map { item ->
                item.copy(grade = safeGrade, isEnabled = true)
            }
        )
    }

    fun applyCurrentAveragePreset() {
        val avg = mutableUiState.value.currentAverage
        val nearestGrade = if (avg > 0.0) round(avg).toInt().coerceIn(18, 30) else 27
        setAllSimulatedGrades(nearestGrade)
    }

    fun resetSimulation() {
        mutableUiState.value = mutableUiState.value.copy(
            simulationItems = mutableUiState.value.simulationItems.map { item ->
                item.copy(isEnabled = false)
            }
        )
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
