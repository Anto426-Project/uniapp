package com.anto426.uniapp.model.didactics

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

data class SingleAttendanceEntry(
    val id: String = "",
    val date: String = "",
    val time: String = "",
    val room: String = "",
    val teacher: String = "",
    val hours: Double? = null,
    val status: String = "",
)

data class AttendanceData(
    val course: String,
    val percentage: String,
    val count: String,
    val attendedHours: Double = 0.0,
    val totalHours: Double? = null,
    val records: List<SingleAttendanceEntry> = emptyList(),
)

data class ExamRecord(
    val name: String,
    val grade: String,
    val cfu: String,
    val date: String,
    val year: Int = 1,
    val code: String = "",
    val lode: Boolean = false,
)

enum class CourseStatus { COMPLETED, ACTIVE, PLANNED }

data class StudyCourse(
    val id: String = "",
    val name: String,
    val cfu: String,
    val status: CourseStatus,
    val professor: String = "",
    val description: String = "",
    val semester: String = "",
    val grade: String? = null,
    val examDate: String? = null,
    val year: Int = 0,
    val ssd: String = "",
    val taf: String = "",
    val lingua: String = "",
    val obiettivi: String = "",
    val contenuti: String = "",
    val testiRiferimento: String = "",
    val metodiDidattici: String = "",
    val modalitaVerifica: String = "",
    val prerequisiti: String = "",
)

data class StudyYear(
    val yearNumber: Int = 1,
    val yearName: String,
    val courses: List<StudyCourse>,
    val icon: ImageVector? = null,
)

enum class QuestionnaireStatus { PENDING, COMPLETED, UNAVAILABLE }

data class QuestionnaireData(
    val course: String,
    val prof: String,
    val code: String,
    val status: QuestionnaireStatus,
    val courseId: String = "",
    val tagList: String = "",
)

data class QuestionnaireAnswerOption(
    val answerId: String,
    val answerText: String,
    val freeText: Boolean = false,
)

data class QuestionnaireQuestion(
    val questionId: String,
    val questionText: String,
    val answers: List<QuestionnaireAnswerOption> = emptyList(),
    val required: Boolean = true,
    val multipleChoice: Boolean = false,
    val freeTextEnabled: Boolean = false,
    val pageId: String = "",
)

data class QuestionnairePage(
    val pageId: String,
    val questions: List<QuestionnaireQuestion> = emptyList(),
)

data class QuestionnaireForm(
    val userCompId: String? = null,
    val questCompId: String? = null,
    val pages: List<QuestionnairePage> = emptyList(),
)

enum class PastExamStatus { VERBALIZED, BOOKED_PAST }

data class PastExam(
    val name: String,
    val date: String,
    val grade: String = "",
    val status: PastExamStatus = PastExamStatus.VERBALIZED,
    val id: String = "",
    val time: String = "",
    val cfu: Int? = null,
    val code: String? = null,
    val room: String? = null,
    val type: String? = null,
    val professor: String? = null,
    val bookingOpenDate: String? = null,
    val bookingCloseDate: String? = null,
    val bookedUsersCount: Int? = null,
    val availableSlots: Int? = null,
    val notes: String? = null,
)

data class ExamSession(
    val name: String,
    val date: String,
    val time: String = "",
    val room: String = "",
    val bookingOpenDate: String = "",
    val bookingCloseDate: String = "",
    val type: String = "",
    val professor: String = "",
    val bookedUsersCount: Int = 0,
    val availableSlots: Int = 0,
    val notes: String = "",
    val canBook: Boolean = true,
    val isBooked: Boolean = false,
    val id: String = "",
    val isBookingFuture: Boolean = false,
    val calendarUrl: String? = null,
)

data class GradeExam(val name: String, val grade: Int, val cfu: Int)

@Serializable
data class SimulationItem(
    val id: String,
    val name: String,
    val cfu: Int,
    val grade: Int,
    val isEnabled: Boolean = false,
    val isCustom: Boolean = false,
)

data class AcademicDetailField(
    val label: String,
    val value: String,
)

data class AcademicExamBookingEntry(
    val studentName: String,
    val matricola: String? = null,
    val grade: String? = null,
)

data class AcademicCommissionMember(
    val displayName: String,
    val role: String? = null,
)

data class AcademicContentItem(
    val id: String? = null,
    val title: String,
    val subtitle: String? = null,
    val detail: String? = null,
    val code: String? = null,
    val date: String? = null,
    val fields: List<AcademicDetailField> = emptyList(),
    val bookings: List<AcademicExamBookingEntry> = emptyList(),
    val commission: List<AcademicCommissionMember> = emptyList(),
) {
    fun itemKey(): String =
        listOf(id, code, date, title, subtitle, detail)
            .joinToString("|") { it.orEmpty() }
            .hashCode()
            .toString()
}

data class AcademicDashboardData(
    val teachings: List<AcademicContentItem> = emptyList(),
    val examRounds: List<AcademicContentItem> = emptyList(),
    val theses: List<AcademicContentItem> = emptyList(),
    val reports: List<AcademicContentItem> = emptyList(),
    val unavailableSections: List<String> = emptyList(),
)

data class ThesisData(
    val candidateName: String,
    val thesisTitle: String? = null,
    val relatore: String? = null,
    val correlatore: String? = null,
    val matricola: String? = null,
    val cds: String? = null,
    val date: String? = null,
    val sede: String? = null,
    val voto: String? = null,
    val sessione: String? = null,
    val extraFields: List<Pair<String, String>> = emptyList(),
)
