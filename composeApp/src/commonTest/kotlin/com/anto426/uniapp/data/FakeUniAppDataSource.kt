package com.anto426.uniapp.data

import com.anto426.unisdk.model.*
import com.anto426.unisdk.transport.*

open class FakeUniAppDataSource : UniAppDataSource {
    override suspend fun loadCareer(forceRefresh: Boolean) = Career("0", null, "0", 180, "1", null, "", emptyList())
    override suspend fun loadProfessorDashboard(forceRefresh: Boolean) = ProfessorDashboard()
    override suspend fun loadExamRounds(forceRefresh: Boolean) = emptyList<ExamRound>()
    override suspend fun loadCourseSyllabus(adsceId: String, forceRefresh: Boolean) = error("Unused")
    override suspend fun bookExamRound(round: ExamRound) = "OK"
    override suspend fun cancelExamRound(round: ExamRound) = "OK"
    override suspend fun loadTaxes(forceRefresh: Boolean) = Taxes("0", emptyList())
    override suspend fun loadStudentDetails(forceRefresh: Boolean) = StudentDetails("")
    override suspend fun loadProfileImage(source: String, forceRefresh: Boolean) = byteArrayOf()
    override suspend fun loadConnectedDevices(forceRefresh: Boolean) = emptyList<ConnectedDevice>()
    override suspend fun disconnectDevice(targetToken: String) = "OK"
    override suspend fun disconnectAllOtherDevices() = "OK"
    override suspend fun loadAttendanceHistory(forceRefresh: Boolean) = emptyList<AttendanceRecord>()
    override suspend fun registerAttendance(
        qrCode: String,
        deviceLatitude: Double?,
        deviceLongitude: Double?,
        deviceAccuracyMeters: Double?,
    ): String = "OK"
    override suspend fun loadUniversityNews(forceRefresh: Boolean) = emptyList<UniversityNews>()
    override suspend fun loadUniversityContacts(forceRefresh: Boolean) = emptyList<UniversityContact>()
    override suspend fun loadStudyPlan(forceRefresh: Boolean) = StudyPlan(emptyList())
    override suspend fun loadSurveyCourses(forceRefresh: Boolean) = emptyList<SurveyCourse>()
    override suspend fun loadSurveyFirstPage(
        courseId: String,
        tagList: String,
    ): SurveyForm = error("Unused")
    override suspend fun saveSurvey(courseId: String, request: SurveySaveRequest) = "OK"
    override suspend fun loadSurveyCompilationStatus(adCod: String, forceRefresh: Boolean) = false
    override suspend fun loadTransportData(forceRefresh: Boolean) = TransportData("", bookings = emptyList(), totalCount = 0)
    override suspend fun bookTransport(request: TransportBookingRequest): TransportActionResult = TransportActionResult.Completed
    override suspend fun deleteTransportBooking(bookingId: String) = TransportActionResult.Completed
}
