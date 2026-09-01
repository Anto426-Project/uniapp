package com.anto426.uniapp.data

import com.anto426.unisdk.backend.model.*
import com.anto426.unisdk.transport.*

open class FakeUniAppDataSource : UniAppDataSource {
    override suspend fun loadCareer(forceRefresh: Boolean) = CareerData("0", null, "0", 180, "1", null, "", emptyList())
    override suspend fun loadProfessorDashboard(forceRefresh: Boolean) = ProfessorDashboardData()
    override suspend fun loadExamRounds(forceRefresh: Boolean) = emptyList<ExamRoundData>()
    override suspend fun loadCourseSyllabus(adsceId: String, forceRefresh: Boolean) = error("Unused")
    override suspend fun bookExamRound(round: ExamRoundData) = "OK"
    override suspend fun cancelExamRound(round: ExamRoundData) = "OK"
    override suspend fun loadTaxes(forceRefresh: Boolean) = TaxesData("0", emptyList())
    override suspend fun loadStudentDetails(forceRefresh: Boolean) = StudentDetailsData("")
    override suspend fun loadProfileImage(source: String, forceRefresh: Boolean) = byteArrayOf()
    override suspend fun loadConnectedDevices(forceRefresh: Boolean) = emptyList<ConnectedDeviceData>()
    override suspend fun disconnectDevice(targetToken: String) = "OK"
    override suspend fun disconnectAllOtherDevices() = "OK"
    override suspend fun loadAttendanceHistory(forceRefresh: Boolean) = emptyList<AttendanceRecord>()
    override suspend fun loadUniversityNews(forceRefresh: Boolean) = emptyList<UniversityNews>()
    override suspend fun loadUniversityContacts(forceRefresh: Boolean) = emptyList<UniversityContact>()
    override suspend fun loadStudyPlan(forceRefresh: Boolean) = StudyPlanData(emptyList())
    override suspend fun loadSurveyCourses(forceRefresh: Boolean) = emptyList<SurveyCourseData>()
    override suspend fun loadSurveyFirstPage(
        courseId: String,
        tagList: String,
    ): SurveyFirstPageData = error("Unused")
    override suspend fun saveSurvey(courseId: String, request: SurveySaveRequest) = "OK"
    override suspend fun loadSurveyCompilationStatus(adCod: String, forceRefresh: Boolean) = false
    override suspend fun loadTransportData(forceRefresh: Boolean) = TransportData("", bookings = emptyList(), totalCount = 0)
    override suspend fun bookTransport(request: TransportBookingRequest) = TransportActionResult.Completed
    override suspend fun deleteTransportBooking(bookingId: String) = TransportActionResult.Completed
    override suspend fun readPreference(key: String): String? = null
    override suspend fun writePreference(key: String, value: String) = Unit
}
