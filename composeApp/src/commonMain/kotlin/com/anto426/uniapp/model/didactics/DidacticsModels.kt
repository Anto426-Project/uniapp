package com.anto426.uniapp.model.didactics

import androidx.compose.ui.graphics.vector.ImageVector

data class AttendanceData(val course: String, val percentage: String, val count: String)

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
)

data class StudyYear(
    val yearNumber: Int = 1,
    val yearName: String,
    val courses: List<StudyCourse>,
    val icon: ImageVector? = null
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
)

data class GradeExam(val name: String, val grade: Int, val cfu: Int)

data class GradeSimulationPreset(val name: String, val cfu: Int, val initialGrade: Int)
