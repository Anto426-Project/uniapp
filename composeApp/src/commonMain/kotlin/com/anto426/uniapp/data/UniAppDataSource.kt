package com.anto426.uniapp.data

import com.anto426.unisdk.backend.model.AttendanceRecord
import com.anto426.unisdk.backend.model.CareerData
import com.anto426.unisdk.backend.model.ConnectedDeviceData
import com.anto426.unisdk.backend.model.CourseSyllabusData
import com.anto426.unisdk.backend.model.ExamRoundData
import com.anto426.unisdk.backend.model.StudyPlanData
import com.anto426.unisdk.backend.model.SurveyCourseData
import com.anto426.unisdk.backend.model.SurveyFirstPageData
import com.anto426.unisdk.backend.model.SurveySaveRequest
import com.anto426.unisdk.backend.model.StudentDetailsData
import com.anto426.unisdk.backend.model.TaxesData
import com.anto426.unisdk.backend.model.UniversityContact
import com.anto426.unisdk.backend.model.UniversityNews
import com.anto426.unisdk.backend.model.ProfessorDashboardData
import com.anto426.unisdk.transport.TransportActionResult
import com.anto426.unisdk.transport.TransportBookingRequest
import com.anto426.unisdk.transport.TransportData

/** Account-aware, encrypted and policy-driven data boundary used by presentation code. */
interface UniAppDataSource {
    suspend fun loadCareer(forceRefresh: Boolean = false): CareerData
    suspend fun loadProfessorDashboard(forceRefresh: Boolean = false): ProfessorDashboardData
    suspend fun loadStudyPlan(forceRefresh: Boolean = false): StudyPlanData
    suspend fun loadExamRounds(forceRefresh: Boolean = false): List<ExamRoundData>
    suspend fun loadCourseSyllabus(adsceId: String, forceRefresh: Boolean = false): CourseSyllabusData
    suspend fun bookExamRound(round: ExamRoundData): String
    suspend fun cancelExamRound(round: ExamRoundData): String
    suspend fun loadTaxes(forceRefresh: Boolean = false): TaxesData
    suspend fun loadStudentDetails(forceRefresh: Boolean = false): StudentDetailsData
    suspend fun loadProfileImage(source: String, forceRefresh: Boolean = false): ByteArray
    suspend fun loadConnectedDevices(forceRefresh: Boolean = false): List<ConnectedDeviceData>
    suspend fun disconnectDevice(targetToken: String): String
    suspend fun disconnectAllOtherDevices(): String
    suspend fun loadAttendanceHistory(forceRefresh: Boolean = false): List<AttendanceRecord>
    suspend fun registerAttendance(
        qrCode: String,
        deviceLatitude: Double? = null,
        deviceLongitude: Double? = null,
        deviceAccuracyMeters: Double? = null,
    ): String
    suspend fun loadUniversityNews(forceRefresh: Boolean = false): List<UniversityNews>
    suspend fun loadUniversityContacts(forceRefresh: Boolean = false): List<UniversityContact>
    suspend fun loadSurveyCourses(forceRefresh: Boolean = false): List<SurveyCourseData>
    suspend fun loadSurveyFirstPage(courseId: String, tagList: String): SurveyFirstPageData
    suspend fun saveSurvey(courseId: String, request: SurveySaveRequest): String
    suspend fun loadSurveyCompilationStatus(adCod: String, forceRefresh: Boolean = false): Boolean
    suspend fun loadTransportData(forceRefresh: Boolean = false): TransportData
    suspend fun loadTransportTicketImage(bookingId: String, source: String, forceRefresh: Boolean = false): ByteArray {
        throw UnsupportedOperationException("Ticket image download is unavailable")
    }
    suspend fun bookTransport(request: TransportBookingRequest): TransportActionResult
    suspend fun deleteTransportBooking(bookingId: String): TransportActionResult
}

internal interface UniAppPortraitSharer {
    suspend fun sharePortrait(source: String, bytes: ByteArray): ByteArray
}
