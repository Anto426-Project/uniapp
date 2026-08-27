package com.anto426.uniapp.android.ui.models

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import com.anto426.liquidmonet.components.cards.LiquidStatusType

data class AttendanceData(val course: String, val percentage: String, val count: String)

data class ExamRecord(val name: String, val grade: String, val cfu: String, val date: String)

enum class CourseStatus { COMPLETED, ACTIVE, PLANNED }
data class StudyCourse(
    val id: String = "",
    val name: String,
    val cfu: String,
    val status: CourseStatus,
    val professor: String = "",
    val description: String = "",
    val semester: String = ""
)
data class StudyYear(val yearName: String, val courses: List<StudyCourse>, val icon: ImageVector)

enum class QuestionnaireStatus { PENDING, COMPLETED }
data class QuestionnaireData(val course: String, val prof: String, val code: String, val status: QuestionnaireStatus)

enum class DeviceType { PHONE, PC, TABLET }
data class DeviceInfo(
    val name: String,
    val location: String,
    val lastSeen: String,
    val type: DeviceType,
    val isCurrent: Boolean
)

data class NewsItem(
    val title: String,
    val description: String,
    val fullContent: String,
    val type: com.anto426.liquidmonet.components.cards.LiquidStatusType = com.anto426.liquidmonet.components.cards.LiquidStatusType.Info
)

data class QuickActionItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

data class ContactData(
    val name: String,
    val role: String,
    val initials: String,
    val email: String = "",
    val phone: String = ""
)

data class LanguageInfo(val name: String, val region: String, val code: String)

data class PastExam(val name: String, val date: String, val grade: String, val status: String)

data class ExamSession(
    val name: String,
    val date: String,
    val time: String,
    val room: String,
    val bookingOpenDate: String,
    val bookingCloseDate: String,
    val type: String,
    val professor: String,
    val bookedUsersCount: Int,
    val isBooked: Boolean
)

data class GradeExam(val name: String, val grade: Int, val cfu: Int)

data class GradeSimulationPreset(val name: String, val cfu: Int, val initialGrade: Int)

data class TaxPaymentData(
    val title: String,
    val date: String,
    val amount: String,
    val isPaid: Boolean,
    val iuv: String
)

data class ServiceData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badgeCount: Int? = null
)

data class TransportRoute(val route: String, val time: String, val countdown: String)

data class TransportTicket(
    val id: String,
    val title: String,
    val price: String,
    val validity: String,
    val type: String,
    val icon: ImageVector
)

data class TransportReservation(
    val id: String,
    val route: String,
    val date: String,
    val time: String,
    val status: String,
    val qrCodeData: String
)

data class ChangelogItemData(
    val tag: String,
    val tagColor: Color,
    val title: String,
    val description: String
)

data class ChangelogVersionData(
    val version: String,
    val date: String,
    val items: List<ChangelogItemData>
)

data class LegalSectionData(val title: String, val content: String)
