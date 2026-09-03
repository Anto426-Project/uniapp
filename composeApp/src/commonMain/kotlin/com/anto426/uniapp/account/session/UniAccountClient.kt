package com.anto426.uniapp.account.session

import com.anto426.unisdk.backend.model.AttendanceRecord
import com.anto426.unisdk.backend.model.CareerData
import com.anto426.unisdk.backend.model.ConnectedDeviceData
import com.anto426.unisdk.backend.model.CourseSyllabusData
import com.anto426.unisdk.backend.model.ExamRoundData
import com.anto426.unisdk.backend.model.StudyPlanData
import com.anto426.unisdk.backend.model.SurveyCourseData
import com.anto426.unisdk.backend.model.StudentDetailsData
import com.anto426.unisdk.backend.model.SurveyFirstPageData
import com.anto426.unisdk.backend.model.SurveySaveRequest
import com.anto426.unisdk.backend.model.TaxesData
import com.anto426.unisdk.backend.model.UniversityContact
import com.anto426.unisdk.backend.model.UniversityNews
import com.anto426.unisdk.backend.model.ProfessorDashboardData

/**
 * Account-bound facade used by UniApp features and future ViewModels.
 *
 * It deliberately exposes neither [com.anto426.unisdk.session.UniCredentials] nor the underlying
 * SDK session capability. Credentials are decrypted only around the requested operation.
 */
class UniAccountClient internal constructor(
    val accountId: String,
    private val coordinator: UniSessionCoordinator,
) {
    suspend fun isSessionValid(): Boolean =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            isSessionValid(session, credentials)
        }

    suspend fun loadCareer(): CareerData =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            loadCareer(session, credentials)
        }

    suspend fun loadProfessorDashboard(): ProfessorDashboardData =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            loadProfessorDashboard(session, credentials)
        }

    suspend fun loadStudyPlan(): StudyPlanData =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            loadStudyPlan(session, credentials)
        }

    suspend fun loadExamRounds(): List<ExamRoundData> =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            loadExamRounds(session, credentials)
        }

    suspend fun loadCourseSyllabus(adsceId: String): CourseSyllabusData =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            loadCourseSyllabus(session, credentials, adsceId)
        }

    suspend fun bookExamRound(round: ExamRoundData): String =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            bookExamRound(session, credentials, round)
        }

    suspend fun cancelExamRound(round: ExamRoundData): String =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            cancelExamRound(session, credentials, round)
        }

    suspend fun loadTaxes(): TaxesData =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            loadTaxes(session, credentials)
        }

    suspend fun loadStudentDetails(): StudentDetailsData =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            loadStudentDetails(session, credentials)
        }

    suspend fun loadProfileImage(source: String): ByteArray =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            loadProfileImage(session, credentials, source)
        }

    suspend fun loadConnectedDevices(): List<ConnectedDeviceData> =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            loadConnectedDevices(session, credentials)
        }

    suspend fun disconnectDevice(targetToken: String): String =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            disconnectDevice(session, credentials, targetToken)
        }

    suspend fun disconnectAllOtherDevices(): String =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            disconnectAllOtherDevices(session, credentials)
        }

    suspend fun registerAttendance(
        qrCode: String,
        deviceLatitude: Double? = null,
        deviceLongitude: Double? = null,
        deviceAccuracyMeters: Double? = null,
    ): String =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            registerAttendance(
                session = session,
                credentials = credentials,
                qrCode = qrCode,
                deviceLatitude = deviceLatitude,
                deviceLongitude = deviceLongitude,
                deviceAccuracyMeters = deviceAccuracyMeters,
            )
        }

    suspend fun loadAttendanceHistory(): List<AttendanceRecord> =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            loadAttendanceHistory(session, credentials)
        }

    suspend fun loadUniversityNews(): List<UniversityNews> =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            loadUniversityNews(session, credentials)
        }

    suspend fun loadUniversityContacts(): List<UniversityContact> =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            loadUniversityContacts(session, credentials)
        }

    suspend fun loadSurveyCourses(): List<SurveyCourseData> =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            loadSurveyCourses(session, credentials)
        }

    suspend fun loadSurveyFirstPage(
        courseId: String,
        tagList: String,
    ): SurveyFirstPageData =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            loadSurveyFirstPage(session, credentials, courseId, tagList)
        }

    suspend fun loadSurveyQuestions(
        courseId: String,
        request: SurveySaveRequest,
    ): SurveyFirstPageData =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            loadSurveyQuestions(session, credentials, courseId, request)
        }

    suspend fun loadSurveyCompilationStatus(adCod: String): Boolean =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            loadSurveyCompilationStatus(session, credentials, adCod)
        }

    suspend fun saveSurvey(
        courseId: String,
        request: SurveySaveRequest,
    ): String =
        coordinator.callAuthenticated(accountId) { session, credentials ->
            saveSurvey(session, credentials, courseId, request)
        }

    suspend fun <T> withTransportSession(
        block: suspend com.anto426.unisdk.transport.TransportService.(
            com.anto426.unisdk.transport.TransportSession,
        ) -> T,
    ): T = coordinator.withTransportSession(accountId, block)

    suspend fun loadTransportData(): com.anto426.unisdk.transport.TransportData =
        withTransportSession { session -> loadTransportData(session) }

    suspend fun bookTransport(
        request: com.anto426.unisdk.transport.TransportBookingRequest,
    ): com.anto426.unisdk.transport.TransportActionResult =
        withTransportSession { session -> bookTransport(session, request) }

    suspend fun deleteTransportBooking(
        bookingId: String,
    ): com.anto426.unisdk.transport.TransportActionResult =
        withTransportSession { session -> deleteTransportBooking(session, bookingId) }
}
