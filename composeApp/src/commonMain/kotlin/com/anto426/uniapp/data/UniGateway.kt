package com.anto426.uniapp.data

import com.anto426.uniapp.data.contract.LibraryBookDetail
import com.anto426.uniapp.data.contract.LibraryBookLoanDraft
import com.anto426.uniapp.data.contract.LibraryBookingOption
import com.anto426.uniapp.data.contract.LibraryBookingOverview
import com.anto426.uniapp.data.contract.LibraryCatalogPage
import com.anto426.uniapp.data.contract.LibraryLoanSubmissionResult
import com.anto426.uniapp.data.contract.LibraryMovementItem
import com.anto426.uniapp.data.contract.LibrarySpaceBookingOutcome
import com.anto426.uniapp.data.contract.LibrarySpaceCalendar
import com.anto426.uniapp.data.contract.LibrarySpaceSelection
import com.anto426.unisdk.model.AttendanceRecord
import com.anto426.unisdk.model.Career
import com.anto426.unisdk.model.ConnectedDevice
import com.anto426.unisdk.model.CourseSyllabus
import com.anto426.unisdk.model.ExamRound
import com.anto426.unisdk.model.ProfessorDashboard
import com.anto426.unisdk.model.StudentDetails
import com.anto426.unisdk.model.StudyPlan
import com.anto426.unisdk.model.SurveyCourse
import com.anto426.unisdk.model.SurveyForm
import com.anto426.unisdk.model.SurveySaveRequest
import com.anto426.unisdk.model.Taxes
import com.anto426.unisdk.model.UniversityContact
import com.anto426.unisdk.model.UniversityNews
import com.anto426.unisdk.transport.TransportActionResult
import com.anto426.unisdk.transport.TransportBookingRequest
import com.anto426.unisdk.transport.TransportData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate

/** Account-aware, encrypted and policy-driven data boundary used by UniCacheEngine and UniRepository. */
interface UniGateway {
    suspend fun loadCareer(forceRefresh: Boolean = false): Career
    fun streamCareer(forceRefresh: Boolean = false): Flow<Career> = flow { emit(loadCareer(forceRefresh)) }
    suspend fun loadProfessorDashboard(forceRefresh: Boolean = false): ProfessorDashboard
    fun streamProfessorDashboard(forceRefresh: Boolean = false): Flow<ProfessorDashboard> = flow {
        emit(loadProfessorDashboard(forceRefresh))
    }
    suspend fun loadStudyPlan(forceRefresh: Boolean = false): StudyPlan
    suspend fun loadExamRounds(forceRefresh: Boolean = false): List<ExamRound>
    fun streamExamRounds(forceRefresh: Boolean = false): Flow<List<ExamRound>> = flow {
        emit(loadExamRounds(forceRefresh))
    }
    suspend fun loadCourseSyllabus(adsceId: String, forceRefresh: Boolean = false): CourseSyllabus
    suspend fun bookExamRound(round: ExamRound): String
    suspend fun cancelExamRound(round: ExamRound): String
    suspend fun loadTaxes(forceRefresh: Boolean = false): Taxes
    suspend fun loadStudentDetails(forceRefresh: Boolean = false): StudentDetails
    suspend fun loadProfileImage(source: String, forceRefresh: Boolean = false): ByteArray
    suspend fun loadConnectedDevices(forceRefresh: Boolean = false): List<ConnectedDevice>
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
    fun streamUniversityNews(forceRefresh: Boolean = false): Flow<List<UniversityNews>> = flow {
        emit(loadUniversityNews(forceRefresh))
    }
    suspend fun loadUniversityContacts(forceRefresh: Boolean = false): List<UniversityContact>
    suspend fun loadSurveyCourses(forceRefresh: Boolean = false): List<SurveyCourse>
    suspend fun loadSurveyFirstPage(courseId: String, tagList: String): SurveyForm
    suspend fun saveSurvey(courseId: String, request: SurveySaveRequest): String
    suspend fun loadSurveyCompilationStatus(adCod: String, forceRefresh: Boolean = false): Boolean
    suspend fun loadTransportData(forceRefresh: Boolean = false): TransportData
    fun streamTransportData(forceRefresh: Boolean = false): Flow<TransportData> = flow {
        emit(loadTransportData(forceRefresh))
    }
    suspend fun loadTransportTicketImage(bookingId: String, source: String, forceRefresh: Boolean = false): ByteArray {
        throw UnsupportedOperationException("Ticket image download is unavailable")
    }
    suspend fun bookTransport(request: TransportBookingRequest): TransportActionResult
    suspend fun deleteTransportBooking(bookingId: String): TransportActionResult

    suspend fun searchLibraryBooks(query: String, page: Int = 1): LibraryCatalogPage
    suspend fun loadLibraryBook(id: String): LibraryBookDetail
    suspend fun prepareLibraryBookRequest(bookId: String, actionId: String): LibraryBookLoanDraft
    suspend fun confirmLibraryBookRequest(draftId: String): LibraryLoanSubmissionResult
    suspend fun loadLibraryMovements(forceRefresh: Boolean = false): List<LibraryMovementItem>
    suspend fun cancelLibraryMovement(movementToken: String)
    suspend fun renewLibraryMovement(movementToken: String): String
    suspend fun loadLibraryBookingOverview(): LibraryBookingOverview
    suspend fun loadLibraryResourceSelection(contextId: String, typeId: String): LibrarySpaceSelection
    suspend fun loadLibraryResourcesForOwner(ownerId: String): List<LibraryBookingOption>
    suspend fun loadLibraryCalendar(
        contextId: String,
        resourceId: String,
        fromDate: LocalDate? = null,
        toDate: LocalDate? = null,
    ): LibrarySpaceCalendar
    suspend fun loadLibraryCalendarPage(calendarId: String, page: Int): LibrarySpaceCalendar
    suspend fun bookLibrarySpace(
        calendarId: String,
        slotIds: List<String>,
        note: String = "",
        deviceAddresses: Map<String, String> = emptyMap(),
    ): LibrarySpaceBookingOutcome
    suspend fun cancelLibrarySpaceBooking(bookingToken: String)
}

internal interface UniPortraitSharer {
    suspend fun sharePortrait(source: String, bytes: ByteArray): ByteArray
}
