package com.anto426.uniapp.data.runtime

import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.UniAppCachePolicies
import com.anto426.uniapp.data.UniAppCachePolicy
import com.anto426.unisdk.backend.model.*
import com.anto426.unisdk.transport.TransportData
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

/** A dataset has one identity, cache policy and loader, independently of its consumers. */
class UniAppDataRequest<T : Any> internal constructor(
    val id: String,
    internal val cacheKey: String? = null,
    internal val serializer: KSerializer<T>? = null,
    internal val policy: UniAppCachePolicy? = null,
    internal val load: suspend UniAppDataSource.(Boolean) -> T,
)

object UniAppDataRequests {
    val Career = UniAppDataRequest("career", "career-v2", CareerData.serializer(), UniAppCachePolicies.Career, UniAppDataSource::loadCareer)
    val Professor = UniAppDataRequest("professor", "professor-dashboard", ProfessorDashboardData.serializer(), UniAppCachePolicies.ProfessorDashboard, UniAppDataSource::loadProfessorDashboard)
    val StudyPlan = UniAppDataRequest("study-plan", "study-plan", StudyPlanData.serializer(), UniAppCachePolicies.StudyPlan, UniAppDataSource::loadStudyPlan)
    val Exams = UniAppDataRequest("exam-rounds", "exam-rounds", ListSerializer(ExamRoundData.serializer()), UniAppCachePolicies.ExamRounds, UniAppDataSource::loadExamRounds)
    val Taxes = UniAppDataRequest("taxes", "taxes", TaxesData.serializer(), UniAppCachePolicies.Taxes, UniAppDataSource::loadTaxes)
    val Student = UniAppDataRequest("student", "student-details", StudentDetailsData.serializer(), UniAppCachePolicies.StudentDetails, UniAppDataSource::loadStudentDetails)
    val Devices = UniAppDataRequest("devices", "connected-devices", ListSerializer(ConnectedDeviceData.serializer()), UniAppCachePolicies.ConnectedDevices, UniAppDataSource::loadConnectedDevices)
    val Attendance = UniAppDataRequest("attendance", "attendance", ListSerializer(AttendanceRecord.serializer()), UniAppCachePolicies.Attendance, UniAppDataSource::loadAttendanceHistory)
    val News = UniAppDataRequest("news", "university-news", ListSerializer(UniversityNews.serializer()), UniAppCachePolicies.News, UniAppDataSource::loadUniversityNews)
    val Contacts = UniAppDataRequest("contacts", "university-contacts", ListSerializer(UniversityContact.serializer()), UniAppCachePolicies.Contacts, UniAppDataSource::loadUniversityContacts)
    val Surveys = UniAppDataRequest("surveys", "survey-courses", ListSerializer(SurveyCourseData.serializer()), UniAppCachePolicies.SurveyCourses, UniAppDataSource::loadSurveyCourses)
    val Transport = UniAppDataRequest("transport", "transport-data", TransportData.serializer(), UniAppCachePolicies.Transport, UniAppDataSource::loadTransportData)
    fun syllabus(id: String) = UniAppDataRequest("syllabus/$id", "course-syllabus-${id.hashCode()}", CourseSyllabusData.serializer(), UniAppCachePolicies.CourseSyllabus) { force -> loadCourseSyllabus(id, force) }
    fun surveyStatus(code: String) = UniAppDataRequest("survey-status/$code", "survey-status-${code.hashCode()}", Boolean.serializer(), UniAppCachePolicies.SurveyStatus) { force -> loadSurveyCompilationStatus(code, force) }
    fun surveyPage(course: String, tags: String) = UniAppDataRequest<SurveyFirstPageData>("survey-page/${course.length}:$course$tags", policy = UniAppCachePolicies.SurveyPage) { loadSurveyFirstPage(course, tags) }
    fun portrait(source: String) = UniAppDataRequest<ByteArray>("portrait/$source", policy = UniAppCachePolicies.ProfileImage) { force -> loadProfileImage(source, force) }
}
