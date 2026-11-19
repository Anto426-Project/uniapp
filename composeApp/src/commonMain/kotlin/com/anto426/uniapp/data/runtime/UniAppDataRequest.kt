package com.anto426.uniapp.data.runtime

import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.UniAppCachePolicies
import com.anto426.uniapp.data.UniAppCachePolicy
import com.anto426.uniapp.data.datasetCacheKey
import com.anto426.unisdk.model.AttendanceRecord
import com.anto426.unisdk.model.Career as SdkCareer
import com.anto426.unisdk.model.ConnectedDevice
import com.anto426.unisdk.model.CourseSyllabus
import com.anto426.unisdk.model.ExamRound
import com.anto426.unisdk.model.ProfessorDashboard
import com.anto426.unisdk.model.StudentDetails
import com.anto426.unisdk.model.StudyPlan as SdkStudyPlan
import com.anto426.unisdk.model.StudyPlanCourse
import com.anto426.unisdk.model.SurveyCourse
import com.anto426.unisdk.model.SurveyForm
import com.anto426.unisdk.model.Taxes as SdkTaxes
import com.anto426.unisdk.model.UniversityContact
import com.anto426.unisdk.model.UniversityNews
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
    val Career: UniAppDataRequest<SdkCareer> = UniAppDataRequest("career", "career-v2", SdkCareer.serializer(), UniAppCachePolicies.Career, UniAppDataSource::loadCareer)
    val Professor: UniAppDataRequest<ProfessorDashboard> = UniAppDataRequest("professor", "professor-dashboard", ProfessorDashboard.serializer(), UniAppCachePolicies.ProfessorDashboard, UniAppDataSource::loadProfessorDashboard)
    val StudyPlan: UniAppDataRequest<SdkStudyPlan> = UniAppDataRequest("study-plan", "study-plan", SdkStudyPlan.serializer(), UniAppCachePolicies.StudyPlan, UniAppDataSource::loadStudyPlan)
    val Exams: UniAppDataRequest<List<ExamRound>> = UniAppDataRequest("exam-rounds", "exam-rounds", ListSerializer(ExamRound.serializer()), UniAppCachePolicies.ExamRounds, UniAppDataSource::loadExamRounds)
    val Taxes: UniAppDataRequest<SdkTaxes> = UniAppDataRequest("taxes", "taxes", SdkTaxes.serializer(), UniAppCachePolicies.Taxes, UniAppDataSource::loadTaxes)
    val Student: UniAppDataRequest<StudentDetails> = UniAppDataRequest("student", "student-details-v2", StudentDetails.serializer(), UniAppCachePolicies.StudentDetails, UniAppDataSource::loadStudentDetails)
    val Devices = UniAppDataRequest("devices", "connected-devices", ListSerializer(ConnectedDevice.serializer()), UniAppCachePolicies.ConnectedDevices, UniAppDataSource::loadConnectedDevices)
    val Attendance = UniAppDataRequest("attendance", "attendance", ListSerializer(AttendanceRecord.serializer()), UniAppCachePolicies.Attendance, UniAppDataSource::loadAttendanceHistory)
    val News = UniAppDataRequest("news", "university-news", ListSerializer(UniversityNews.serializer()), UniAppCachePolicies.News, UniAppDataSource::loadUniversityNews)
    val Contacts = UniAppDataRequest("contacts", "university-contacts-v2", ListSerializer(UniversityContact.serializer()), UniAppCachePolicies.Contacts, UniAppDataSource::loadUniversityContacts)
    val Surveys = UniAppDataRequest("surveys", "survey-courses", ListSerializer(SurveyCourse.serializer()), UniAppCachePolicies.SurveyCourses, UniAppDataSource::loadSurveyCourses)
    val Transport = UniAppDataRequest("transport", "transport-data", TransportData.serializer(), UniAppCachePolicies.Transport, UniAppDataSource::loadTransportData)
    fun syllabus(id: String) = UniAppDataRequest("syllabus/$id", datasetCacheKey("course-syllabus", id), CourseSyllabus.serializer(), UniAppCachePolicies.CourseSyllabus) { force -> loadCourseSyllabus(id, force) }
    fun surveyStatus(code: String) = UniAppDataRequest("survey-status/$code", datasetCacheKey("survey-status", code), Boolean.serializer(), UniAppCachePolicies.SurveyStatus) { force -> loadSurveyCompilationStatus(code, force) }
    fun surveyPage(course: String, tags: String) = UniAppDataRequest<SurveyForm>("survey-page/${course.length}:$course$tags", policy = UniAppCachePolicies.SurveyPage) { loadSurveyFirstPage(course, tags) }
    fun portrait(source: String) = UniAppDataRequest<ByteArray>("portrait/$source", policy = UniAppCachePolicies.ProfileImage) { force -> loadProfileImage(source, force) }
}
