package com.anto426.uniapp.demo

import com.anto426.unisdk.backend.model.*
import com.anto426.unisdk.transport.TransportData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import uniapp.composeapp.generated.resources.Res

@Serializable
internal data class DemoFixture(
    val student: StudentDetailsData, val career: CareerData, val plan: StudyPlanData,
    val exams: List<ExamRoundData>, val taxes: TaxesData, val devices: List<ConnectedDeviceData>,
    val attendance: List<AttendanceRecord>, val news: List<UniversityNews>, val contacts: List<UniversityContact>,
    val transport: TransportData, val syllabuses: List<CourseSyllabusData>, val surveys: List<SurveyCourseData>,
    val surveyQuestion: String, val surveyAnswers: List<String>,
)

internal object DemoCatalog {
    private val lock = Mutex()
    private var fixture: DemoFixture? = null
    suspend fun load(): DemoFixture = withContext(Dispatchers.Default) {
        lock.withLock {
            fixture ?: run {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val source = Res.readBytes("files/demo-data.json").decodeToString()
                val dated = Regex("@date:(-?\\d+)@").replace(source) { match ->
                    val date = today.plus(match.groupValues[1].toInt(), DateTimeUnit.DAY)
                    "${date.day.toString().padStart(2, '0')}/${date.month.number.toString().padStart(2, '0')}/${date.year}"
                }
                Json.decodeFromString<DemoFixture>(dated).also { fixture = it }
            }
        }
    }
}
