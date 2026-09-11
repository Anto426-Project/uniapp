package com.anto426.uniapp.demo

import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.UniAppPortraitSharer
import com.anto426.unisdk.backend.model.*
import com.anto426.unisdk.transport.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

/** Account-local simulation: no backend/client dependency and no real university operations. */
internal class DemoAppDataSource(
    private val identity: () -> Pair<String, String?> = { DemoAccount.developerName(null) to DemoAccount.avatarFallback },
    private val imageLoader: suspend (String) -> ByteArray = { byteArrayOf() },
    private val portraitSharer: (suspend (String, ByteArray) -> ByteArray)? = null,
) : UniAppDataSource, UniAppPortraitSharer {
    private val lock = Mutex()
    private var rounds: List<ExamRoundData>? = null
    private var transport: TransportData? = null
    private var surveys: List<SurveyCourseData>? = null
    private suspend fun feedback() = getString(Res.string.ui_demo_action)
    override suspend fun loadCareer(forceRefresh: Boolean) = DemoCatalog.load().career
    override suspend fun loadProfessorDashboard(forceRefresh: Boolean) = ProfessorDashboardData()
    override suspend fun loadStudyPlan(forceRefresh: Boolean) = DemoCatalog.load().plan
    override suspend fun loadExamRounds(forceRefresh: Boolean) = lock.withLock { rounds ?: DemoCatalog.load().exams }
    override suspend fun loadCourseSyllabus(adsceId: String, forceRefresh: Boolean) = DemoCatalog.load().syllabuses.first { it.adsceId == adsceId }
    override suspend fun bookExamRound(round: ExamRoundData): String {
        lock.withLock { rounds = (rounds ?: DemoCatalog.load().exams).map { if (it.appId == round.appId) it.copy(booked = true) else it } }
        return feedback()
    }
    override suspend fun cancelExamRound(round: ExamRoundData): String {
        lock.withLock { rounds = (rounds ?: DemoCatalog.load().exams).map { if (it.appId == round.appId) it.copy(booked = false) else it } }
        return feedback()
    }
    override suspend fun loadTaxes(forceRefresh: Boolean) = DemoCatalog.load().taxes
    override suspend fun loadStudentDetails(forceRefresh: Boolean): StudentDetailsData {
        val (name, avatar) = identity()
        return DemoCatalog.load().student.copy(fullName = name, photoUrl = avatar)
    }
    override suspend fun loadProfileImage(source: String, forceRefresh: Boolean): ByteArray = imageLoader(source)
    override suspend fun sharePortrait(source: String, bytes: ByteArray): ByteArray = portraitSharer?.invoke(source, bytes) ?: bytes
    override suspend fun loadConnectedDevices(forceRefresh: Boolean) = DemoCatalog.load().devices
    override suspend fun disconnectDevice(targetToken: String) = feedback()
    override suspend fun disconnectAllOtherDevices() = feedback()
    override suspend fun loadAttendanceHistory(forceRefresh: Boolean) = DemoCatalog.load().attendance
    override suspend fun registerAttendance(qrCode: String, deviceLatitude: Double?, deviceLongitude: Double?, deviceAccuracyMeters: Double?) = feedback()
    override suspend fun loadUniversityNews(forceRefresh: Boolean) = DemoCatalog.load().news
    override suspend fun loadUniversityContacts(forceRefresh: Boolean) = DemoCatalog.load().contacts
    override suspend fun loadSurveyCourses(forceRefresh: Boolean) = lock.withLock { surveys ?: DemoCatalog.load().surveys }
    override suspend fun loadSurveyFirstPage(courseId: String, tagList: String): SurveyFirstPageData {
        val fixture = DemoCatalog.load()
        return SurveyFirstPageData("demo-user", "demo-questionnaire", listOf(SurveyPageData("demo-page", listOf(
            SurveyQuestionData("demo-question", fixture.surveyQuestion,
                fixture.surveyAnswers.mapIndexed { i, text -> SurveyAnswerOptionData("demo-answer-$i", text) }, pageId = "demo-page")
        ))))
    }
    override suspend fun saveSurvey(courseId: String, request: SurveySaveRequest): String {
        lock.withLock { surveys = (surveys ?: DemoCatalog.load().surveys).map { if (it.courseId == courseId) it.copy(completed = true) else it } }
        return feedback()
    }
    override suspend fun loadSurveyCompilationStatus(adCod: String, forceRefresh: Boolean) = loadSurveyCourses(false).firstOrNull { it.adCod == adCod }?.completed == true
    override suspend fun loadTransportData(forceRefresh: Boolean) = lock.withLock { transport ?: DemoCatalog.load().transport }
    override suspend fun bookTransport(request: TransportBookingRequest): TransportActionResult = lock.withLock {
        val current = transport ?: DemoCatalog.load().transport
        val booking = TransportBooking("demo-booking-${current.bookings.size + 1}", "DEMO-${current.bookings.size + 1}",
            request.routeCode, request.date.toString(), request.direction.name, request.direction == TransportDirection.RETURN,
            "UNIAPP-DEMO-NOT-A-VALID-TICKET")
        transport = current.copy(bookings = current.bookings + booking, totalCount = current.totalCount + 1)
        TransportActionResult.Completed
    }
    override suspend fun deleteTransportBooking(bookingId: String): TransportActionResult = lock.withLock {
        val current = transport ?: DemoCatalog.load().transport
        val bookings = current.bookings.filterNot { it.id == bookingId }
        transport = current.copy(bookings = bookings, totalCount = bookings.size)
        TransportActionResult.Completed
    }

}
