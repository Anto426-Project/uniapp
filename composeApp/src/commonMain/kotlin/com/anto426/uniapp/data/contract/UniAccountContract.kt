package com.anto426.uniapp.data.contract

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.anto426.liquidmonet.components.cards.statuscard.LiquidStatusType
import com.anto426.uniapp.account.model.UniProfileRole
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable

// ============================================================================
// 1. Root Account Contract & Cryptographic Ownership Boundary
// ============================================================================

@Immutable
data class UniAccountContract(
    val owner: UniContractOwner = UniContractOwner(),
    val identity: UniIdentityContract = UniIdentityContract(),
    val academic: UniAcademicContract = UniAcademicContract(),
    val exams: UniExamsContract = UniExamsContract(),
    val professor: UniProfessorContract = UniProfessorContract(),
    val taxes: UniTaxesContract = UniTaxesContract(),
    val surveys: UniSurveysContract = UniSurveysContract(),
    val attendance: UniAttendanceContract = UniAttendanceContract(),
    val transport: UniTransportContract = UniTransportContract(),
    val library: UniLibraryContract = UniLibraryContract(),
    val devices: UniDevicesContract = UniDevicesContract(),
    val campus: UniCampusContract = UniCampusContract(),
)

@Immutable
data class UniContractOwner(
    val accountId: String = "",
    val profileId: String? = null,
    val role: UniProfileRole = UniProfileRole.STUDENT,
    val generation: Long = 0L,
    val isGuest: Boolean = false,
) {
    val isProfessor: Boolean get() = role == UniProfileRole.PROFESSOR

    fun verifyMatches(
        expectedAccountId: String,
        expectedProfileId: String?,
        expectedGeneration: Long? = null,
    ) {
        if (accountId.isNotEmpty() &&
            (accountId != expectedAccountId || profileId != expectedProfileId)
        ) {
            throw CancellationException("The contract belongs to a previous account or career profile")
        }
        if (expectedGeneration != null && generation != expectedGeneration) {
            throw CancellationException("The contract belongs to an expired session generation")
        }
    }
}

// ============================================================================
// 2. Identity & Badge Sub-Contract
// ============================================================================

@Immutable
data class UniIdentityContract(
    val isProfessor: Boolean = false,
    val fullName: String = "",
    val initials: String = "",
    val username: String = "",
    val matricola: String = "",
    val email: String = "",
    val degreeName: String = "",
    val departmentName: String = "",
    val departmentId: String = "",
    val teacherId: String = "",
    val degreeSummary: String = "",
    val primaryStudentId: String? = null,
    val badgeCode: String = "",
    val badgeQrValue: String = "",
    val badgeBarcodeValue: String = "",
    val photoUrl: String? = null,
    val photoData: ByteArray? = null,
) {
    val badgeDisplayValue: String get() = badgeCode.ifBlank { matricola }
}

// ============================================================================
// 3. Academic (Career + Study Plan + Grades) Sub-Contract
// ============================================================================

@Immutable
data class UniAcademicContract(
    val averageLabel: String = "—",
    val averageValue: Double? = null,
    val degreeBaseLabel: String = "—",
    val degreeBaseValue: Double? = null,
    val acquiredCfu: Int = 0,
    val gradedCfu: Int = 0,
    val targetCfu: Int = 180,
    val remainingCfu: Int = 180,
    val progress: Float = 0f,
    val currentYear: Int = 1,
    val totalYears: Int = 1,
    val academicYearLabel: String = "",
    val statusLabel: String = "",
    val completedExamsCount: Int = 0,
    val completedStudyCoursesCount: Int = 0,
    val totalStudyCoursesCount: Int = 0,
    val plannedActivitiesCount: Int = 0,
    val lodeCount: Int = 0,
    val studyPlanYears: List<Int> = emptyList(),
    val availableYears: List<Int> = emptyList(),
    val transcriptExams: List<ExamRecord> = emptyList(),
    val examsByYear: Map<Int, List<ExamRecord>> = emptyMap(),
    val studyYears: List<StudyYear> = emptyList(),
    val gradedExams: List<GradeExam> = emptyList(),
    val simulatableCourses: List<SimulationItem> = emptyList(),
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

data class GradeExam(
    val name: String,
    val grade: Int,
    val cfu: Int,
)

@Serializable
data class SimulationItem(
    val id: String,
    val name: String,
    val cfu: Int,
    val grade: Int,
    val isEnabled: Boolean = false,
    val isCustom: Boolean = false,
)

data class ResolvedCourseDetail(
    val course: StudyCourse,
    val professorContact: ContactData? = null,
)

// ============================================================================
// 4. Exam Rounds & Exam History Sub-Contract
// ============================================================================

@Immutable
data class UniExamsContract(
    val upcomingRounds: List<ExamSession> = emptyList(),
    val bookableRounds: List<ExamSession> = emptyList(),
    val bookedRounds: List<ExamSession> = emptyList(),
    val openBookableCount: Int = 0,
    val bookedCount: Int = 0,
    val nextOpenExamLabel: String = "",
    val history: List<PastExam> = emptyList(),
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

// ============================================================================
// 5. Professor Didactics Sub-Contract
// ============================================================================

@Immutable
data class UniProfessorContract(
    val teachings: List<AcademicContentItem> = emptyList(),
    val examRounds: List<AcademicContentItem> = emptyList(),
    val theses: List<AcademicContentItem> = emptyList(),
    val reports: List<AcademicContentItem> = emptyList(),
    val unavailableSections: List<String> = emptyList(),
    val teachingCount: Int = 0,
    val openExamRoundsCount: Int = 0,
    val thesisCount: Int = 0,
    val reportCount: Int = 0,
    val nextExamLabel: String = "",
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

// ============================================================================
// 6. Taxes Sub-Contract
// ============================================================================

@Immutable
data class UniTaxesContract(
    val dueAmountLabel: String = "€ 0.00",
    val rawDueAmount: String = "0",
    val paidTotalLabel: String = "€ 0.00",
    val paidProgress: Float = 0f,
    val nextDueDate: String? = null,
    val nextUnpaidInstallmentLabel: String = "",
    val allPayments: List<TaxPaymentData> = emptyList(),
    val pendingPayments: List<TaxPaymentData> = emptyList(),
    val paidPayments: List<TaxPaymentData> = emptyList(),
)

data class TaxPaymentData(
    val title: String,
    val date: String,
    val amount: String,
    val isPaid: Boolean,
    val iuv: String,
)

// ============================================================================
// 7. Surveys & Attendance Sub-Contracts
// ============================================================================

@Immutable
data class UniSurveysContract(
    val all: List<QuestionnaireData> = emptyList(),
    val pending: List<QuestionnaireData> = emptyList(),
    val completed: List<QuestionnaireData> = emptyList(),
    val unavailable: List<QuestionnaireData> = emptyList(),
    val pendingCount: Int = 0,
    val totalCount: Int = 0,
    val completedProgress: Float = 0f,
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

@Immutable
data class UniAttendanceContract(
    val courses: List<AttendanceData> = emptyList(),
    val totalCoursesCount: Int = 0,
    val totalAttendedLectures: Int = 0,
    val averageAttendancePercent: Int = 0,
)

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

// ============================================================================
// 8. Transport Sub-Contract
// ============================================================================

const val TRANSPORT_BOOKING_DAYS_AHEAD = 15

@Immutable
data class UniTransportContract(
    val reservations: List<TransportReservation> = emptyList(),
    val days: List<TransportDayGroup> = emptyList(),
    val routes: List<TransportRouteOption> = emptyList(),
    val bookedRequests: Set<TripBookingRequest> = emptySet(),
    val totalCount: Int = 0,
)

data class TransportDayGroup(
    val date: String,
    val reservations: List<TransportReservation>,
    val directionSummary: String,
)

enum class TripDirection { ANDATA, RITORNO }

enum class TripBookingDirection { OUTBOUND, RETURN, ROUND_TRIP }

enum class TripBookingOutcome { COMPLETED, ALREADY_EXISTS }

data class TripBookingRequest(
    val routeCode: String,
    val date: LocalDate,
    val direction: TripBookingDirection = TripBookingDirection.OUTBOUND,
)

data class TransportRouteStops(
    val label: String,
    val departureStop: String,
    val arrivalStop: String,
)

data class TransportRouteOption(
    val code: String,
    val label: String,
    val outboundStops: TransportRouteStops,
    val returnStops: TransportRouteStops,
)

data class TransportReservation(
    val id: String,
    val route: String,
    val date: String,
    val time: String,
    val direction: TripDirection = TripDirection.ANDATA,
    val qrCodeData: String = "",
    val departureStop: String = "",
    val arrivalStop: String = "",
    val busNumber: String = "",
    val ticketUrl: String = "",
    val ticketNumber: String = "",
)

fun transportBookingWindow(today: LocalDate): List<LocalDate> =
    (1..TRANSPORT_BOOKING_DAYS_AHEAD).map { today.plus(it, DateTimeUnit.DAY) }

fun isTransportBookingDateAllowed(date: LocalDate, today: LocalDate): Boolean =
    date > today &&
        date <= today.plus(TRANSPORT_BOOKING_DAYS_AHEAD, DateTimeUnit.DAY) &&
        date.dayOfWeek.ordinal < 5

fun transportBookingRequests(
    routeCode: String,
    dates: List<LocalDate>,
    direction: TripBookingDirection,
): List<TripBookingRequest> = dates.distinct().sorted().flatMap { date ->
    val directions = if (direction == TripBookingDirection.ROUND_TRIP) {
        listOf(TripBookingDirection.OUTBOUND, TripBookingDirection.RETURN)
    } else {
        listOf(direction)
    }
    directions.map { trip -> TripBookingRequest(routeCode, date, trip) }
}

fun isTransportDateBooked(
    date: LocalDate,
    routeCode: String,
    direction: TripBookingDirection,
    bookedRequests: Set<TripBookingRequest>,
): Boolean = routeCode.isNotBlank() &&
    transportBookingRequests(routeCode.trim().uppercase(), listOf(date), direction)
        .any(bookedRequests::contains)

// ============================================================================
// 9. Library Sub-Contract
// ============================================================================

@Immutable
data class UniLibraryContract(
    val movements: List<LibraryMovementItem> = emptyList(),
    val spaceBookings: List<LibrarySpaceBooking> = emptyList(),
    val spaceTypes: List<LibraryBookingOption> = emptyList(),
)

data class LibraryCatalogBook(
    val id: String,
    val title: String,
    val subtitle: String,
    val metadata: String = "",
    val branches: List<String> = emptyList(),
)

data class LibraryCatalogPage(
    val query: String,
    val books: List<LibraryCatalogBook>,
    val page: Int,
    val total: Int,
) {
    val hasNextPage: Boolean get() = page * PAGE_SIZE < total

    companion object {
        const val PAGE_SIZE = 10
    }
}

data class LibraryBookCopyAction(
    val actionId: String,
    val label: String,
    val libraryCode: String,
    val branchName: String,
    val movementType: String,
) {
    val libraryDisplayName: String get() = branchName
}

data class LibraryBookDetail(
    val id: String,
    val title: String,
    val author: String? = null,
    val year: String? = null,
    val hasAvailableDocument: Boolean = false,
    val branches: List<String> = emptyList(),
    val inventory: String? = null,
    val shelfmark: String? = null,
    val notes: String? = null,
    val description: String = "",
    val actions: List<LibraryBookCopyAction> = emptyList(),
) {
    val isAvailable: Boolean get() = hasAvailableDocument || actions.isNotEmpty()
}

data class LibraryBookLoanDraft(
    val draftId: String,
    val description: String,
)

data class LibraryLoanSubmissionResult(
    val description: String,
    val requiresVerification: Boolean = true,
)

data class LibraryMovementItem(
    val id: String? = null,
    val title: String,
    val detail: String,
    val branchName: String? = null,
    val dueDateLabel: String? = null,
    val canCancel: Boolean = false,
    val canRenew: Boolean = false,
    val actionToken: String? = null,
)

data class LibraryBookingOption(
    val id: String,
    val label: String,
)

data class LibrarySpaceBooking(
    val id: String? = null,
    val title: String,
    val description: String,
    val dateLabel: String? = null,
    val timeLabel: String? = null,
    val canCancel: Boolean = false,
    val bookingToken: String? = null,
)

data class LibraryBookingOverview(
    val contextId: String,
    val types: List<LibraryBookingOption>,
    val bookings: List<LibrarySpaceBooking>,
)

data class LibrarySpaceSelection(
    val owners: List<LibraryBookingOption>,
    val resources: List<LibraryBookingOption>,
)

data class LibrarySpaceSlot(
    val slotId: String,
    val label: String,
    val timeLabel: String,
    val description: String,
    val dateLabel: String? = null,
    val seatsLabel: String? = null,
    val requiresDeviceAddress: Boolean = false,
)

data class LibrarySpaceCalendar(
    val calendarId: String,
    val resourceId: String,
    val page: Int = 1,
    val description: String = "",
    val slots: List<LibrarySpaceSlot> = emptyList(),
)

data class LibrarySpaceBookingOutcome(
    val confirmed: Boolean,
    val serverCode: Int,
)

// ============================================================================
// 10. Connected Devices Sub-Contract
// ============================================================================

@Immutable
data class UniDevicesContract(
    val devices: List<DeviceInfo> = emptyList(),
    val currentDevice: DeviceInfo? = null,
    val otherDevices: List<DeviceInfo> = emptyList(),
    val canDisconnectAll: Boolean = false,
)

enum class DeviceType { PHONE, PC, TABLET }

data class DeviceInfo(
    val name: String,
    val location: String,
    val lastSeen: String,
    val appVersion: String? = null,
    val type: DeviceType,
    val isCurrent: Boolean,
    val id: String = "",
    val revocationToken: String? = null,
)

// ============================================================================
// 11. Campus (News & Contacts) Sub-Contract
// ============================================================================

@Immutable
data class UniCampusContract(
    val news: NewsFeed = NewsFeed(),
    val contacts: List<ContactData> = emptyList(),
)

@Immutable
data class NewsItem(
    val title: String,
    val description: String,
    val fullContent: String,
    val type: LiquidStatusType = LiquidStatusType.Info,
    val category: String = "",
    val publishedAt: String = "",
    val key: String = "",
    val sourceUrl: String? = null,
)

data class NewsFeed(
    val items: List<NewsItem> = emptyList(),
    val byCategory: Map<String, List<NewsItem>> = emptyMap(),
)

enum class ContactCategory {
    TEACHERS,
    SECRETARIAT,
    SERVICES,
}

data class ContactData(
    val name: String,
    val role: String,
    val initials: String,
    val email: String = "",
    val phone: String = "",
    val category: ContactCategory = ContactCategory.TEACHERS,
    val department: String = "",
    val office: String = "",
    val officeHours: String = "",
    val city: String = "",
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phones: List<String> = emptyList(),
    val address: String = "",
    val building: String = "",
) {
    val phoneNumbers: List<String>
        get() = (phones + phone).map(String::trim).filter(String::isNotBlank).distinct()

    val detailKey: String
        get() = listOf(id, name, email, department, address, building, city, office, *phoneNumbers.toTypedArray())
            .joinToString(separator = "|", prefix = "contact:") { "${it.length}:$it" }
}

/** Only complete, unambiguous numbers can become a call action. */
internal fun String.contactDialUri(): String? {
    val value = trim()
    if (value.count(Char::isDigit) < 7 || " / " in value) return null
    if (!Regex("^\\+?[0-9() ./-]+$").matches(value)) return null
    val normalized = value.filter(Char::isDigit)
    return "tel:${if (value.startsWith('+')) "+" else ""}$normalized"
}
