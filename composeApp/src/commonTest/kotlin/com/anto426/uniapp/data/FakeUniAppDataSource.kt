package com.anto426.uniapp.data

import com.anto426.uniapp.library.model.*
import com.anto426.unisdk.model.*
import com.anto426.unisdk.transport.*

open class FakeUniAppDataSource : UniGateway {
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
    override suspend fun searchLibraryBooks(query: String, page: Int) = LibraryCatalogPage(query, emptyList(), page, 0)
    override suspend fun loadLibraryBook(id: String) = LibraryBookDetail(id, id)
    override suspend fun prepareLibraryBookRequest(bookId: String, actionId: String) = LibraryBookLoanDraft("draft", "OK")
    override suspend fun confirmLibraryBookRequest(draftId: String) = LibraryLoanSubmissionResult("OK")
    override suspend fun loadLibraryMovements(forceRefresh: Boolean) = emptyList<LibraryMovementItem>()
    override suspend fun cancelLibraryMovement(movementToken: String) = Unit
    override suspend fun renewLibraryMovement(movementToken: String) = "OK"
    override suspend fun loadLibraryBookingOverview() = LibraryBookingOverview("ctx", emptyList(), emptyList())
    override suspend fun loadLibraryResourceSelection(contextId: String, typeId: String) = LibrarySpaceSelection(emptyList(), emptyList())
    override suspend fun loadLibraryResourcesForOwner(ownerId: String) = emptyList<LibraryBookingOption>()
    override suspend fun loadLibraryCalendar(
        contextId: String,
        resourceId: String,
        fromDate: kotlinx.datetime.LocalDate?,
        toDate: kotlinx.datetime.LocalDate?,
    ) = LibrarySpaceCalendar("cal", resourceId)
    override suspend fun loadLibraryCalendarPage(calendarId: String, page: Int) = LibrarySpaceCalendar("cal", "", page = page)
    override suspend fun bookLibrarySpace(
        calendarId: String,
        slotIds: List<String>,
        note: String,
        deviceAddresses: Map<String, String>,
    ) = LibrarySpaceBookingOutcome(true, 1)
    override suspend fun cancelLibrarySpaceBooking(bookingToken: String) = Unit
}
