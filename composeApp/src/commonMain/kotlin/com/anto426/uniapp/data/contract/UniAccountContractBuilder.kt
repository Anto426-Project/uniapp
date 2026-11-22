package com.anto426.uniapp.data.contract

import com.anto426.liquidmonet.components.cards.statuscard.LiquidStatusType
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.account.model.UniProfileRole
import com.anto426.uniapp.data.datasetCacheKey
import com.anto426.uniapp.didactics.presentation.AcademicSection
import com.anto426.unisdk.model.AttendanceRecord
import com.anto426.unisdk.model.Career
import com.anto426.unisdk.model.CareerExam
import com.anto426.unisdk.model.ConnectedDevice
import com.anto426.unisdk.model.CourseSyllabus
import com.anto426.unisdk.model.ExamRound
import com.anto426.unisdk.model.ProfessorContentItem
import com.anto426.unisdk.model.ProfessorDashboard
import com.anto426.unisdk.model.ProfessorDetailField
import com.anto426.unisdk.model.StudentDetails
import com.anto426.unisdk.model.StudyPlan
import com.anto426.unisdk.model.StudyPlanCourse
import com.anto426.unisdk.model.SurveyAnswerSubmission
import com.anto426.unisdk.model.SurveyCourse
import com.anto426.unisdk.model.SurveyForm
import com.anto426.unisdk.model.SurveyPageSubmission
import com.anto426.unisdk.model.SurveySaveRequest
import com.anto426.unisdk.model.Taxes
import com.anto426.unisdk.model.UniversityContact
import com.anto426.unisdk.model.UniversityNews
import com.anto426.unisdk.model.ExamRoundBookingState
import com.anto426.unisdk.model.parseExamRoundDateOrNull
import com.anto426.unisdk.model.parseExamRoundDateTimeOrNull
import com.anto426.unisdk.model.resolveExamRoundBookingState
import com.anto426.unisdk.transport.TransportActionResult
import com.anto426.unisdk.transport.TransportBooking
import com.anto426.unisdk.transport.TransportBookingRequest
import com.anto426.unisdk.transport.TransportData
import com.anto426.unisdk.transport.TransportDirection
import com.anto426.unisdk.transport.TransportRoute
import com.anto426.unisdk.transport.displayFor
import com.anto426.unisdk.transport.parseTransportTravelDate
import io.ktor.http.Url
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToLong
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

// ============================================================================
// 1. Identity Contract Builder
// ============================================================================

internal fun buildIdentityContract(
    account: UniAccountSummary? = null,
    student: StudentDetails? = null,
    photoData: ByteArray? = null,
): UniIdentityContract {
    if (account?.isProfessor == true) {
        val profile = account.profiles.firstOrNull { it.profileId == account.activeProfileId }
            ?: account.profiles.firstOrNull { it.type == UniProfileRole.PROFESSOR }
        val fullName = account.displayName.trim()
        return UniIdentityContract(
            isProfessor = true,
            fullName = fullName,
            initials = fullName.computeInitials(),
            username = account.serverUserId,
            matricola = account.serverUserId,
            email = account.email.orEmpty(),
            degreeName = profile?.departmentName.orEmpty(),
            departmentName = profile?.departmentName.orEmpty(),
            departmentId = profile?.dipId.orEmpty(),
            teacherId = profile?.teacherId.orEmpty(),
            degreeSummary = profile?.departmentName.orEmpty(),
            photoUrl = account.photoUrl,
            photoData = photoData,
        )
    }

    val activeProfile = account?.profiles?.firstOrNull { it.profileId == account.activeProfileId }
    val fullName = student?.fullName?.trim()?.ifBlank { null }
        ?: activeProfile?.displayName?.trim()?.ifBlank { null }
        ?: account?.displayName?.trim().orEmpty()
    val matricola = student?.matricola?.trim()?.ifBlank { null }
        ?: activeProfile?.matricola?.trim()?.ifBlank { null }
        ?: account?.matricola?.trim().orEmpty()
    val degreeName = student?.degreeName?.trim()?.ifBlank { null }
        ?: activeProfile?.degreeName?.trim()?.ifBlank { null }
        ?: account?.degreeName?.trim().orEmpty()
    val departmentName = student?.departmentName?.trim()?.ifBlank { null }
        ?: activeProfile?.departmentName?.trim().orEmpty()
    val badgeCode = student?.badgeCode?.trim().orEmpty()
    val qrValue = student?.badgeQrValue?.trim()?.takeIf(String::isNotBlank) ?: run {
        val parts = fullName.split(' ').filter(String::isNotBlank)
        val firstName = parts.firstOrNull()?.uppercase().orEmpty()
        val lastName = parts.drop(1).joinToString(" ").uppercase()
        listOf(matricola, lastName, firstName, degreeName)
            .filter(String::isNotBlank)
            .joinToString("|")
    }
    val barcodeValue = student?.badgeBarcodeValue?.trim()?.takeIf(String::isNotBlank)
        ?: badgeCode.takeIf(String::isNotBlank)
        ?: matricola
    val primaryStudentId = listOfNotNull(
        student?.stuId,
        student?.matId,
        student?.matricola,
        activeProfile?.stuId,
        activeProfile?.matId,
        activeProfile?.matricola,
        account?.matricola,
    ).firstOrNull { it.isNotBlank() }

    return UniIdentityContract(
        isProfessor = false,
        fullName = fullName,
        initials = fullName.computeInitials(),
        username = account?.serverUserId.orEmpty(),
        matricola = matricola,
        email = student?.email?.trim()?.ifBlank { null } ?: account?.email.orEmpty(),
        degreeName = degreeName,
        departmentName = departmentName,
        departmentId = student?.dipId?.trim()?.ifBlank { null } ?: activeProfile?.dipId.orEmpty(),
        degreeSummary = listOfNotNull(
            departmentName.takeIf(String::isNotBlank),
            matricola.takeIf(String::isNotBlank)?.let { "Matricola $it" },
        ).joinToString(" • "),
        primaryStudentId = primaryStudentId,
        badgeCode = badgeCode,
        badgeQrValue = qrValue,
        badgeBarcodeValue = barcodeValue,
        photoUrl = student?.photoUrl?.takeIf(String::isNotBlank) ?: account?.photoUrl,
        photoData = photoData,
    )
}

internal fun String.computeInitials(): String =
    trim().split(' ').filter(String::isNotBlank).take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")

// ============================================================================
// 2. Academic Contract Builder (Single Unified Calculation for All Screens)
// ============================================================================

private val AcademicNumberPattern = Regex("""\d+(?:[.,]\d+)?""")
private val LodePattern = Regex("""\b30\s*(?:e\s*lode|lode|l)\b""", RegexOption.IGNORE_CASE)
private val IdoneitaTokens = setOf("id", "idoneo", "idonea", "idoneita", "idoneità", "superato", "approvato")

internal fun String.firstAcademicIntegerOrNull(): Int? =
    AcademicNumberPattern.find(this)?.value?.substringBefore('.')?.substringBefore(',')?.toIntOrNull()

internal fun String?.academicDecimalOrNull(): Double? =
    this?.let { AcademicNumberPattern.find(it)?.value?.replace(',', '.')?.toDoubleOrNull() }

fun Double.toFixedTwoDecimals(): String {
    if (!isFinite()) return "0.00"
    val scaled = (this * 100.0).roundToLong()
    val sign = if (scaled < 0) "-" else ""
    val absScaled = abs(scaled)
    val integerPart = absScaled / 100
    val fractionalPart = (absScaled % 100).toString().padStart(2, '0')
    return "$sign$integerPart.$fractionalPart"
}

internal fun String.numericGradeOrNull(): Int? {
    val clean = trim()
    if (clean.isEmpty()) return null
    if (LodePattern.containsMatchIn(clean)) return 31
    val value = clean.substringBefore('/').trim().toIntOrNull() ?: return null
    return value.takeIf { it in 18..31 }
}

internal fun String.isValidExamGrade(): Boolean {
    val normalized = trim().lowercase()
    return numericGradeOrNull() != null || normalized in IdoneitaTokens
}

/**
 * Single canonical check for whether a [CareerExam] row represents a passed exam/idoneità
 * rather than an unpassed/planned libretto row (`grade = "-"`, `date = "-"`).
 */
internal fun CareerExam.isPassedCareerExam(): Boolean {
    val cleanGrade = grade.trim()
    val cleanDate = date.trim()
    return cleanGrade.isValidExamGrade() || (cleanDate.isNotBlank() && cleanDate != "-")
}

internal fun List<CareerExam>.passedCareerExams(): List<CareerExam> =
    filter(CareerExam::isPassedCareerExam)
        .distinctBy { exam ->
            val id = exam.adsceId?.trim()?.takeIf(String::isNotBlank)
            id ?: "${exam.name.normalizeAcademicTitle()}|${exam.grade.trim().lowercase()}|${exam.date.trim()}"
        }

internal fun String.toDisplayExamGrade(): String {
    val clean = trim()
    val normalized = clean.lowercase()
    return when {
        clean.isEmpty() || clean == "-" -> "Idoneo"
        normalized == "id" || normalized == "idoneo" || normalized == "idonea" ||
            normalized == "idoneita" || normalized == "idoneità" -> "Idoneo"
        normalized == "superato" -> "Superato"
        normalized == "approvato" -> "Approvato"
        else -> clean
    }
}

internal fun buildAcademicContract(
    career: Career? = null,
    studyPlan: StudyPlan? = null,
    examRounds: List<ExamRound> = emptyList(),
    defaultTargetCfu: Int = 180,
): UniAcademicContract {
    val passedExams = career?.exams.orEmpty().passedCareerExams()
    val gradedExams = career?.toGradeExams().orEmpty()
    val lodeCount = passedExams.count { exam ->
        exam.grade.contains("lode", ignoreCase = true) || exam.grade.contains("30L", ignoreCase = true)
    }

    val gradedCfu = gradedExams.sumOf { it.cfu }
    val computedAverage = if (gradedCfu > 0) {
        gradedExams.sumOf { it.grade.coerceAtMost(30) * it.cfu }.toDouble() / gradedCfu
    } else null

    val averageValue = career?.average.academicDecimalOrNull()?.takeIf { it > 0.0 } ?: computedAverage
    val degreeBaseValue = career?.degreeBase.academicDecimalOrNull()?.takeIf { it > 0.0 }
        ?: averageValue?.takeIf { it > 0.0 }?.let { (it * 110.0) / 30.0 }

    val averageLabel = career?.average?.trim()?.takeIf { it.isNotBlank() && it != "-" && it != "0" && it != "0.0" }
        ?: averageValue?.takeIf { it > 0.0 }?.toFixedTwoDecimals()
        ?: "—"
    val degreeBaseLabel = career?.degreeBase?.trim()?.takeIf { it.isNotBlank() && it != "-" && it != "0" && it != "0.0" }
        ?: degreeBaseValue?.takeIf { it > 0.0 }?.toFixedTwoDecimals()
        ?: "—"

    val studyYears = studyPlan?.toStudyYears(career).orEmpty()
    val allStudyCourses = studyYears.flatMap { it.courses }
    val completedStudyCourses = allStudyCourses.filter { it.status == CourseStatus.COMPLETED }
    val completedStudyCoursesCount = completedStudyCourses.size
    val totalStudyCoursesCount = allStudyCourses.size

    val careerAcquiredCfu = career?.cfu?.firstAcademicIntegerOrNull()
    val passedExamsCfu = passedExams.sumOf { it.cfu ?: 0 }
    val planCompletedCfu = completedStudyCourses.sumOf { it.cfu.firstAcademicIntegerOrNull() ?: 0 }
    val acquiredCfu = careerAcquiredCfu?.takeIf { it > 0 }
        ?: maxOf(passedExamsCfu, planCompletedCfu)

    val inlineTargetCfu = career?.cfu?.substringAfter('/', missingDelimiterValue = "")
        ?.firstAcademicIntegerOrNull()
        ?.takeIf { it > 0 }
    val plannedCfu = allStudyCourses.sumOf { it.cfu.firstAcademicIntegerOrNull() ?: 0 }.takeIf { it >= 120 }
    val rawTargetCfu = career?.cfuTarget?.takeIf { it > 0 }
        ?: inlineTargetCfu
        ?: plannedCfu
        ?: defaultTargetCfu
    val targetCfu = if (rawTargetCfu > 0) rawTargetCfu.coerceAtLeast(acquiredCfu) else 0
    val remainingCfu = (targetCfu - acquiredCfu).coerceAtLeast(0)
    val progress = if (targetCfu > 0) (acquiredCfu.toFloat() / targetCfu.toFloat()).coerceIn(0f, 1f) else 0f

    val currentYear = career?.year?.firstAcademicIntegerOrNull()?.coerceAtLeast(1) ?: 1
    val maxPlanYear = allStudyCourses.mapNotNull { it.year.takeIf { y -> y > 0 } }.maxOrNull() ?: 0
    val totalYears = maxOf(currentYear, maxPlanYear, 1)

    val transcriptExams = career?.toExamRecords(studyPlan).orEmpty()
    val completedExamsCount = transcriptExams.size.takeIf { it > 0 } ?: completedStudyCoursesCount
    val plannedActivitiesCount = totalStudyCoursesCount.coerceAtLeast(completedExamsCount)

    val examsByYear = transcriptExams.groupBy { it.year }
    val studyPlanYears = allStudyCourses
        .mapNotNull { it.year.takeIf { y -> y > 0 } }
        .distinct()
        .sorted()
    val availableYears = (studyPlanYears + examsByYear.keys).distinct()
        .sortedWith(compareBy<Int> { it == 0 }.thenBy { it })

    val passedIds = passedExams.mapNotNull { it.adsceId?.trim()?.takeIf(String::isNotBlank) }.toSet()
    val passedTitles = passedExams.map { it.name.normalizeAcademicTitle() }.toSet()
    val planSimulations = allStudyCourses
        .filter { it.status != CourseStatus.COMPLETED }
        .map { course ->
            val id = course.id.takeIf(String::isNotBlank)?.let { "plan-id:$it" }
                ?: "plan-title:${course.name.normalizeAcademicTitle()}"
            SimulationItem(
                id = id,
                name = course.name,
                cfu = (course.cfu.firstAcademicIntegerOrNull() ?: 6).coerceIn(1, 30),
                grade = 26,
                isEnabled = false,
                isCustom = false,
            )
        }
        .distinctBy { it.id }
    val simulatableCourses = planSimulations.ifEmpty {
        examRounds.filterNot {
            it.booked ||
                ((it.adsceId ?: it.adId)?.trim() in passedIds) ||
                (it.courseName.normalizeAcademicTitle() in passedTitles)
        }
            .map { round ->
                SimulationItem(
                    id = "round:${round.courseName.normalizeAcademicTitle()}",
                    name = round.courseName,
                    cfu = 6,
                    grade = 26,
                    isEnabled = false,
                    isCustom = false,
                )
            }
            .distinctBy { it.id }
    }

    return UniAcademicContract(
        averageLabel = averageLabel,
        averageValue = averageValue,
        degreeBaseLabel = degreeBaseLabel,
        degreeBaseValue = degreeBaseValue,
        acquiredCfu = acquiredCfu,
        gradedCfu = gradedCfu,
        targetCfu = targetCfu,
        remainingCfu = remainingCfu,
        progress = progress,
        currentYear = currentYear,
        totalYears = totalYears,
        academicYearLabel = career?.year.orEmpty(),
        statusLabel = career?.status.orEmpty(),
        completedExamsCount = completedExamsCount,
        completedStudyCoursesCount = completedStudyCoursesCount,
        totalStudyCoursesCount = totalStudyCoursesCount,
        plannedActivitiesCount = plannedActivitiesCount,
        lodeCount = lodeCount,
        studyPlanYears = studyPlanYears,
        availableYears = availableYears,
        transcriptExams = transcriptExams,
        examsByYear = examsByYear,
        studyYears = studyYears,
        gradedExams = gradedExams,
        simulatableCourses = simulatableCourses,
    )
}

private fun String.normalizeAcademicTitle(): String =
    lowercase().replace(Regex("\\s+"), " ").trim()

internal fun Career.toExamRecords(studyPlan: StudyPlan? = null): List<ExamRecord> {
    val planCourses = studyPlan?.courses.orEmpty()
    val yearsByCourseId = planCourses
        .mapNotNull { course ->
            val id = course.adsceId?.trim()?.takeIf(String::isNotBlank) ?: return@mapNotNull null
            val year = course.year?.takeIf { it > 0 } ?: return@mapNotNull null
            id to year
        }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, years) -> years.distinct().singleOrNull() ?: 0 }

    val yearsByCourseName = planCourses
        .mapNotNull { course ->
            val year = course.year?.takeIf { it > 0 } ?: return@mapNotNull null
            course.title.normalizeAcademicTitle() to year
        }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, years) -> years.distinct().singleOrNull() ?: 0 }

    return exams.passedCareerExams().map { exam ->
        val lode = exam.grade.contains("lode", ignoreCase = true) ||
            exam.grade.contains("30L", ignoreCase = true)
        val cleanAdsceId = exam.adsceId?.trim()?.takeIf(String::isNotBlank)
        val year = if (cleanAdsceId != null) {
            yearsByCourseId[cleanAdsceId] ?: 0
        } else {
            yearsByCourseName[exam.name.normalizeAcademicTitle()] ?: 0
        }

        ExamRecord(
            name = exam.name,
            grade = exam.grade.toDisplayExamGrade(),
            cfu = exam.cfu?.let { "$it CFU" } ?: "—",
            date = exam.date,
            year = year,
            code = cleanAdsceId.orEmpty(),
            lode = lode,
        )
    }
}

internal fun Career.toGradeExams(): List<GradeExam> =
    exams.passedCareerExams().mapNotNull { exam ->
        val numeric = exam.grade.numericGradeOrNull() ?: return@mapNotNull null
        val cfu = exam.cfu?.takeIf { it > 0 } ?: return@mapNotNull null
        GradeExam(
            name = exam.name,
            grade = numeric,
            cfu = cfu,
        )
    }

internal fun StudyPlan.deduplicatedCourses(): List<StudyPlanCourse> {
    if (courses.size <= 1) return courses
    val result = mutableListOf<StudyPlanCourse>()
    for (course in courses) {
        val cleanId = course.adsceId?.trim()?.takeIf(String::isNotBlank)
        val normTitle = course.title.normalizeAcademicTitle()
        val existingIndex = result.indexOfFirst { existing ->
            val existingId = existing.adsceId?.trim()?.takeIf(String::isNotBlank)
            val existingTitle = existing.title.normalizeAcademicTitle()
            val sameTitle = normTitle.isNotBlank() && normTitle == existingTitle
            if (!sameTitle) return@indexOfFirst false
            val sameId = cleanId != null && existingId != null && cleanId == existingId
            val missingYear = (course.year == null || course.year!! <= 0) || (existing.year == null || existing.year!! <= 0)
            val sameYear = course.year != null && existing.year != null && course.year == existing.year
            val librettoAndPlanPair = course.completed != existing.completed
            sameId || missingYear || sameYear || librettoAndPlanPair
        }
        if (existingIndex < 0) {
            result += course
        } else {
            val existing = result[existingIndex]
            result[existingIndex] = StudyPlanCourse(
                title = existing.title.ifBlank { course.title },
                cfu = existing.cfu?.takeIf { it > 0 } ?: course.cfu?.takeIf { it > 0 },
                year = existing.year?.takeIf { it > 0 } ?: course.year?.takeIf { it > 0 },
                completed = existing.completed || course.completed,
                adsceId = existing.adsceId?.trim()?.takeIf(String::isNotBlank)
                    ?: course.adsceId?.trim()?.takeIf(String::isNotBlank),
                category = existing.category?.trim()?.takeIf(String::isNotBlank)
                    ?: course.category?.trim()?.takeIf(String::isNotBlank),
                completionDate = existing.completionDate?.trim()?.takeIf { it.isNotBlank() && it != "-" }
                    ?: course.completionDate?.trim()?.takeIf { it.isNotBlank() && it != "-" },
            )
        }
    }
    return result
}

internal fun StudyPlan.toStudyYears(career: Career? = null): List<StudyYear> {
    val mergedCourses = deduplicatedCourses()
    val passedExams = career?.exams.orEmpty().passedCareerExams()
    val remainingExams = passedExams.toMutableList()
    val matchedExamsByCourseIndex = arrayOfNulls<CareerExam>(mergedCourses.size)

    // Pass 1: 1-to-1 match by non-blank adsceId
    mergedCourses.forEachIndexed { index, course ->
        val cleanCourseId = course.adsceId?.trim()?.takeIf(String::isNotBlank) ?: return@forEachIndexed
        val examIdx = remainingExams.indexOfFirst { it.adsceId?.trim() == cleanCourseId }
        if (examIdx >= 0) {
            matchedExamsByCourseIndex[index] = remainingExams.removeAt(examIdx)
        }
    }

    // Pass 2: 1-to-1 match by normalized course title
    mergedCourses.forEachIndexed { index, course ->
        if (matchedExamsByCourseIndex[index] != null) return@forEachIndexed
        val normTitle = course.title.normalizeAcademicTitle()
        val examIdx = remainingExams.indexOfFirst { it.name.normalizeAcademicTitle() == normTitle }
        if (examIdx >= 0) {
            matchedExamsByCourseIndex[index] = remainingExams.removeAt(examIdx)
        }
    }

    val useAuthoritativeCareerMatches = matchedExamsByCourseIndex.any { it != null }

    return mergedCourses
        .mapIndexed { index, course ->
            val cleanCourseId = course.adsceId?.trim()?.takeIf(String::isNotBlank)
            val exam = matchedExamsByCourseIndex[index]
            val isCompleted = if (useAuthoritativeCareerMatches) {
                exam != null
            } else {
                course.completed
            }
            val effectiveCfu = course.cfu?.takeIf { it > 0 } ?: exam?.cfu?.takeIf { it > 0 }
            val grade = exam?.grade?.toDisplayExamGrade()
            val examDate = exam?.date?.takeIf { it.isNotBlank() && it != "-" }
                ?: course.completionDate?.takeIf { it.isNotBlank() && it != "-" }
            val yearNumber = course.year?.takeIf { it > 0 } ?: 0

            StudyCourse(
                id = cleanCourseId ?: exam?.adsceId?.trim().orEmpty(),
                name = course.title,
                cfu = effectiveCfu?.let { "$it CFU" }.orEmpty(),
                status = if (isCompleted) CourseStatus.COMPLETED else CourseStatus.PLANNED,
                professor = "",
                description = course.category.orEmpty(),
                semester = course.completionDate.orEmpty(),
                grade = grade,
                examDate = examDate,
                year = yearNumber,
                taf = course.category.orEmpty(),
            ) to yearNumber
        }
        .groupBy(Pair<StudyCourse, Int>::second)
        .toList()
        .sortedWith(compareBy<Pair<Int, List<Pair<StudyCourse, Int>>>> { it.first == 0 }.thenBy { it.first })
        .map { (yearNumber, items) ->
            StudyYear(
                yearNumber = yearNumber,
                yearName = if (yearNumber > 0) "${yearNumber}° Anno" else "Anno non specificato",
                courses = items.map(Pair<StudyCourse, Int>::first),
            )
        }
}

internal fun CourseSyllabus.toStudyCourse(
    studyPlanCourse: StudyPlanCourse? = null,
    careerExam: CareerExam? = null,
): StudyCourse {
    val passedExam = careerExam?.takeIf(CareerExam::isPassedCareerExam)
    val effectiveCfu = studyPlanCourse?.cfu ?: cfu ?: passedExam?.cfu
    val isCompleted = studyPlanCourse?.completed == true || passedExam != null
    val effectiveGrade = passedExam?.grade?.toDisplayExamGrade()
    val effectiveTaf = studyPlanCourse?.category?.takeIf(String::isNotBlank) ?: tipoAttivita.orEmpty()

    return StudyCourse(
        id = adsceId,
        name = adDes,
        cfu = effectiveCfu?.let { "$it CFU" }.orEmpty(),
        status = if (isCompleted) CourseStatus.COMPLETED else CourseStatus.ACTIVE,
        professor = professorsLabel ?: professors.joinToString(", "),
        description = listOfNotNull(obiettivi, contenuti).filter(String::isNotBlank).joinToString("\n\n"),
        semester = aaOffId.orEmpty(),
        grade = effectiveGrade,
        examDate = passedExam?.date?.takeIf { it.isNotBlank() && it != "-" },
        year = studyPlanCourse?.year ?: 0,
        ssd = settore.orEmpty(),
        taf = effectiveTaf,
        lingua = lingua.orEmpty(),
        obiettivi = obiettivi.orEmpty(),
        contenuti = contenuti.orEmpty(),
        testiRiferimento = testiRiferimento.orEmpty(),
        metodiDidattici = metodiDidattici.orEmpty(),
        modalitaVerifica = modalitaVerifica.orEmpty(),
        prerequisiti = prerequisiti.orEmpty(),
    )
}

internal fun resolveCourseDetailContract(
    courseId: String,
    syllabus: CourseSyllabus,
    studyPlan: StudyPlan? = null,
    career: Career? = null,
    contacts: List<ContactData>? = null,
): ResolvedCourseDetail {
    val planCourse = studyPlan?.courses?.firstOrNull {
        it.adsceId?.trim() == courseId.trim() || it.title.normalizeAcademicTitle() == syllabus.adDes.normalizeAcademicTitle()
    }
    val careerExam = career?.exams?.filter(CareerExam::isPassedCareerExam)?.firstOrNull {
        it.adsceId?.trim() == courseId.trim() || it.name.normalizeAcademicTitle() == syllabus.adDes.normalizeAcademicTitle()
    }
    val course = syllabus.toStudyCourse(planCourse, careerExam)
    val professorContact = findMatchingProfessorContact(course.professor, contacts)
    return ResolvedCourseDetail(course = course, professorContact = professorContact)
}

private fun findMatchingProfessorContact(
    professorName: String,
    contacts: List<ContactData>?,
): ContactData? {
    if (professorName.isBlank() || contacts.isNullOrEmpty()) return null
    val cleanProf = professorName.lowercase()
        .replace("prof.ssa", "")
        .replace("prof.", "")
        .replace("dott.ssa", "")
        .replace("dott.", "")
        .replace("ing.", "")
        .trim()
    val profTokens = cleanProf.split(" ", "-", ",").map(String::trim).filter { it.length > 2 }
    if (profTokens.isEmpty()) return null

    return contacts.firstOrNull { contact ->
        val cleanContact = contact.name.lowercase()
        val contactTokens = cleanContact.split(" ", "-", ",").map(String::trim).filter { it.length > 2 }
        (profTokens.size >= 2 && profTokens.all { token -> cleanContact.contains(token) }) ||
            (contactTokens.size >= 2 && contactTokens.all { token -> cleanProf.contains(token) }) ||
            cleanContact == cleanProf
    }
}

// ============================================================================
// 3. Exams & Exam History Contract Builder
// ============================================================================

private val ExamCalendarTimeZone = TimeZone.of("Europe/Rome")

internal fun buildExamsContract(
    rounds: List<ExamRound>,
    careerExams: List<CareerExam> = emptyList(),
    today: LocalDate? = null,
): UniExamsContract {
    val upcomingSdkRounds = if (today != null) rounds.filterUpcomingExamRounds(today) else rounds
    val sessions = upcomingSdkRounds.toExamSessions(today)
    val bookable = sessions.filterNot(ExamSession::isBooked)
    val booked = sessions.filter(ExamSession::isBooked)
    val openUnbookedRounds = rounds.filter { it.open && !it.booked }
    val nextOpenRound = openUnbookedRounds.firstOrNull()
    val history = if (today != null) buildExamHistory(careerExams, rounds, today) else emptyList()

    return UniExamsContract(
        upcomingRounds = sessions,
        bookableRounds = bookable,
        bookedRounds = booked,
        openBookableCount = openUnbookedRounds.size,
        bookedCount = booked.size,
        nextOpenExamLabel = nextOpenRound?.let { "${it.courseName} • ${it.dateTime}" }.orEmpty(),
        history = history,
    )
}

internal fun ExamRound.stableUiId(): String =
    appId?.let { "${cdsId.orEmpty()}-${adId.orEmpty()}-$it" }
        ?: "${courseName.trim()}-${dateTime.trim()}-${room.trim()}"

internal fun ExamRound.calendarUrlOrNull(): String? {
    if (!booked) return null
    val raw = dateTime.trim()
    val date = parseExamRoundDateOrNull(raw) ?: return null
    val hasTime = Regex("""(?:T|\s)\d{1,2}:\d{2}""").containsMatchIn(raw)

    val dates = if (hasTime) {
        val parsedDateTime = parseExamRoundDateTimeOrNull(raw) ?: return null
        val start = parsedDateTime.toInstant(ExamCalendarTimeZone)
        val end = start.plus(1, DateTimeUnit.HOUR)
        "${start.toGoogleCalendarTimestamp()}/${end.toGoogleCalendarTimestamp()}"
    } else {
        val end = date.plus(1, DateTimeUnit.DAY)
        "${date.toCompactCalendarDate()}/${end.toCompactCalendarDate()}"
    }

    val details = buildList<String> {
        registrationTypeDescription.takeIf { it.isNotBlank() && it != "N/D" }?.let { add(it) }
        presidentFullName?.takeIf(String::isNotBlank)?.let { add(it) }
        notes?.takeIf(String::isNotBlank)?.let { add(it) }
    }.joinToString("\n")

    return buildString {
        append("https://calendar.google.com/calendar/render?action=TEMPLATE")
        append("&text=").append(courseName.trim().ifBlank { "Esame" }.encodeUrlQueryValue())
        append("&dates=").append(dates)
        room.trim().takeIf { it.isNotBlank() && it != "-" }?.let {
            append("&location=").append(it.encodeUrlQueryValue())
        }
        details.takeIf(String::isNotBlank)?.let {
            append("&details=").append(it.encodeUrlQueryValue())
        }
        append("&ctz=Europe/Rome")
    }
}

private fun kotlin.time.Instant.toGoogleCalendarTimestamp(): String {
    val dt = toLocalDateTime(TimeZone.UTC)
    return "${dt.date.toCompactCalendarDate()}T" +
        "${dt.hour.toString().padStart(2, '0')}${dt.minute.toString().padStart(2, '0')}${dt.second.toString().padStart(2, '0')}Z"
}

private fun LocalDate.toCompactCalendarDate(): String =
    "${year.toString().padStart(4, '0')}${(month.ordinal + 1).toString().padStart(2, '0')}${day.toString().padStart(2, '0')}"

private fun String.encodeUrlQueryValue(): String = buildString {
    for (byte in encodeToByteArray()) {
        val value = byte.toInt() and 0xFF
        val isUnreserved = (value in 'A'.code..'Z'.code) ||
            (value in 'a'.code..'z'.code) ||
            (value in '0'.code..'9'.code) ||
            value == '-'.code || value == '_'.code || value == '.'.code || value == '~'.code
        if (isUnreserved) append(value.toChar())
        else append('%').append(value.toString(16).uppercase().padStart(2, '0'))
    }
}

internal fun List<ExamRound>.filterUpcomingExamRounds(today: LocalDate): List<ExamRound> =
    mapIndexedNotNull { index, round ->
        val examDate = parseExamRoundDateOrNull(round.dateTime)
        if (examDate != null && examDate < today) null
        else IndexedValue(index, round to (parseExamRoundDateTimeOrNull(round.dateTime)?.toString() ?: examDate?.toString()))
    }
        .sortedWith(
            compareBy<IndexedValue<Pair<ExamRound, String?>>> { it.value.second == null }
                .thenBy { it.value.second }
                .thenBy { it.index },
        )
        .map { it.value.first }

internal fun List<ExamRound>.toExamSessions(today: LocalDate? = null): List<ExamSession> =
    map { round ->
        val parts = round.dateTime.trim().split(" ").filter(String::isNotBlank)
        val datePart = parts.firstOrNull() ?: round.dateTime
        val timePart = parts.drop(1).joinToString(" ")
        val registrationStart = round.registrationStartingDate?.trim().orEmpty()
        val registrationEnd = round.registrationEndingDate?.trim().orEmpty()
        val bookingState = if (today != null) {
            resolveExamRoundBookingState(
                registrationStartingDate = round.registrationStartingDate,
                registrationEndingDate = round.registrationEndingDate,
                fallbackOpen = round.open,
                today = today,
            )
        } else {
            round.bookingState
        }

        ExamSession(
            name = round.courseName,
            date = datePart,
            time = timePart,
            room = round.room,
            bookingOpenDate = registrationStart,
            bookingCloseDate = registrationEnd,
            type = round.registrationTypeDescription,
            professor = round.presidentFullName.orEmpty(),
            bookedUsersCount = round.totalRegistrations ?: 0,
            availableSlots = round.availableSlots,
            notes = round.notes.orEmpty(),
            canBook = bookingState == ExamRoundBookingState.BOOKABLE,
            isBooked = round.booked,
            id = round.stableUiId(),
            isBookingFuture = bookingState == ExamRoundBookingState.FUTURE,
            calendarUrl = round.calendarUrlOrNull(),
        )
    }

internal fun buildExamHistory(
    careerExams: List<CareerExam>,
    rounds: List<ExamRound>,
    today: LocalDate,
): List<PastExam> {
    val passedCareerExams = careerExams.passedCareerExams()
    val verbalized = passedCareerExams.mapIndexed { index, exam ->
        val cleanGrade = exam.grade.trim()
        val cleanDate = exam.date.trim()
        val id = listOf(exam.adsceId.orEmpty(), exam.name, cleanDate, cleanGrade)
            .joinToString("-")
            .ifBlank { "career-$index" }
        SortablePastExam(
            order = index,
            sortDate = parseExamRoundDateOrNull(cleanDate),
            exam = PastExam(
                name = exam.name,
                date = cleanDate.ifBlank { "—" },
                grade = exam.grade.toDisplayExamGrade(),
                status = PastExamStatus.VERBALIZED,
                id = "verbalized-$id",
                cfu = exam.cfu,
                code = exam.adsceId?.takeIf(String::isNotBlank),
            ),
        )
    }

    val verbalizedIds = passedCareerExams.mapNotNull { it.adsceId?.trim()?.takeIf(String::isNotBlank) }.toSet()
    val verbalizedCourseNames = passedCareerExams.map { it.name.normalizeAcademicTitle() }.toSet()
    val pastBooked = rounds.mapIndexedNotNull { index, round ->
        val examDate = parseExamRoundDateOrNull(round.dateTime) ?: return@mapIndexedNotNull null
        if (!round.booked || examDate >= today) return@mapIndexedNotNull null
        val roundCourseId = (round.adsceId ?: round.adId)?.trim()?.takeIf(String::isNotBlank)
        if ((roundCourseId != null && roundCourseId in verbalizedIds) ||
            round.courseName.normalizeAcademicTitle() in verbalizedCourseNames
        ) {
            return@mapIndexedNotNull null
        }

        val parts = round.dateTime.trim().split(" ").filter(String::isNotBlank)
        val datePart = parts.firstOrNull() ?: round.dateTime
        val timePart = parts.drop(1).joinToString(" ")
        SortablePastExam(
            order = passedCareerExams.size + index,
            sortDate = examDate,
            exam = PastExam(
                name = round.courseName,
                date = datePart,
                status = PastExamStatus.BOOKED_PAST,
                id = "booked-past-${round.stableUiId()}",
                time = timePart,
                room = round.room.takeIf(String::isNotBlank),
                type = round.registrationTypeDescription.takeIf { it.isNotBlank() && it != "N/D" },
                professor = round.presidentFullName?.takeIf(String::isNotBlank),
                bookingOpenDate = round.registrationStartingDate?.takeIf(String::isNotBlank),
                bookingCloseDate = round.registrationEndingDate?.takeIf(String::isNotBlank),
                bookedUsersCount = round.totalRegistrations,
                availableSlots = round.availableSlots,
                notes = round.notes?.takeIf(String::isNotBlank),
                code = (round.adsceId ?: round.adId)?.takeIf(String::isNotBlank),
            ),
        )
    }

    return (verbalized + pastBooked)
        .sortedWith(
            compareBy<SortablePastExam> { it.sortDate == null }
                .thenByDescending { it.sortDate }
                .thenBy { it.order },
        )
        .map(SortablePastExam::exam)
}

private data class SortablePastExam(
    val order: Int,
    val sortDate: LocalDate?,
    val exam: PastExam,
)

// ============================================================================
// 4. Professor Contract Builder
// ============================================================================

internal fun buildProfessorContract(
    dashboard: ProfessorDashboard,
): UniProfessorContract {
    val teachings = dashboard.teachings.map(ProfessorContentItem::toAcademicContentItem)
    val examRounds = dashboard.examRounds.map(ProfessorContentItem::toAcademicContentItem)
    val theses = dashboard.theses.map(ProfessorContentItem::toAcademicContentItem)
    val reports = dashboard.reports.map(ProfessorContentItem::toAcademicContentItem)
    val firstRound = dashboard.examRounds.firstOrNull()

    return UniProfessorContract(
        teachings = teachings,
        examRounds = examRounds,
        theses = theses,
        reports = reports,
        unavailableSections = dashboard.unavailableSections.map { it.name },
        teachingCount = teachings.size,
        openExamRoundsCount = examRounds.size,
        thesisCount = theses.size,
        reportCount = reports.size,
        nextExamLabel = firstRound?.subtitle ?: firstRound?.title.orEmpty(),
    )
}

internal fun ProfessorContentItem.toAcademicContentItem(): AcademicContentItem {
    val resolvedFields = fields.ifEmpty { detail.parseProfessorDetailFields() }
    return AcademicContentItem(
        id = id,
        title = title,
        subtitle = subtitle,
        detail = detail,
        code = code,
        date = date,
        fields = resolvedFields.map { AcademicDetailField(it.label, it.value) },
        bookings = bookings.map { AcademicExamBookingEntry(it.studentName, it.matricola, it.grade) },
        commission = commission.map { AcademicCommissionMember(it.displayName, it.role) },
    )
}

private fun String?.parseProfessorDetailFields(): List<ProfessorDetailField> =
    orEmpty()
        .lineSequence()
        .mapNotNull { line ->
            val separator = line.indexOf(':')
            if (separator <= 0 || separator == line.lastIndex) return@mapNotNull null
            val label = line.substring(0, separator).trim()
            val value = line.substring(separator + 1).trim()
            if (label.isBlank() || value.isBlank()) null else ProfessorDetailField(label, value)
        }
        .toList()

internal fun extractThesisData(item: AcademicContentItem): ThesisData {
    val resolved = item.fields.ifEmpty {
        item.detail.parseProfessorDetailFields().map { AcademicDetailField(it.label, it.value) }
    }
    val map = resolved.associate { it.label.lowercase().trim() to it.value.trim() }

    val thesisTitle = map["titolo tesi"] ?: map["titolo"] ?: map["argomento"] ?: map["elaborato"] ?: map["titolo elaborato"]
    val relatore = map["relatore"] ?: map["docente relatore"] ?: map["professore relatore"] ?: map["relatore tesi"]
    val correlatore = map["correlatore"] ?: map["docente correlatore"] ?: map["secondo relatore"]
    val matricola = map["matricola"] ?: map["matr."] ?: map["matr"] ?: item.code
    val cds = map["corso di laurea"] ?: map["cds"] ?: map["corso"] ?: map["percorso"] ?: item.subtitle
    val discussionDate = map["data discussione"] ?: map["discussione"] ?: map["data tesi"] ?: map["data"] ?: item.date
    val sede = map["sede"] ?: map["aula"] ?: map["luogo"]
    val voto = map["voto"] ?: map["voto finale"] ?: map["punteggio"] ?: map["esito"]
    val sessione = map["sessione"] ?: map["sessione di laurea"] ?: map["appello"]

    val standardKeys = setOf(
        "titolo tesi", "titolo", "argomento", "elaborato", "titolo elaborato",
        "relatore", "docente relatore", "professore relatore", "relatore tesi",
        "correlatore", "docente correlatore", "secondo relatore",
        "matricola", "matr.", "matr",
        "corso di laurea", "cds", "corso", "percorso",
        "data discussione", "discussione", "data tesi", "data",
        "sede", "aula", "luogo",
        "voto", "voto finale", "punteggio", "esito",
        "sessione", "sessione di laurea", "appello",
        "cognome", "nome", "studente", "candidato",
    )

    val extraFields = resolved
        .filterNot { standardKeys.contains(it.label.lowercase().trim()) }
        .map { it.label to it.value }

    return ThesisData(
        candidateName = item.title,
        thesisTitle = thesisTitle,
        relatore = relatore,
        correlatore = correlatore,
        matricola = matricola,
        cds = cds,
        date = discussionDate,
        sede = sede,
        voto = voto,
        sessione = sessione,
        extraFields = extraFields,
    )
}

internal suspend fun AcademicContentItem.orderedAcademicDetailFields(
    section: AcademicSection,
): List<Pair<String, String>> {
    val parsed = fields.map { it.label to it.value }
    if (section != AcademicSection.Theses) return parsed
    val priorities = listOf(
        "data", "discussione", "data tesi", "cognome", "nome", "matricola",
        "percorso", "cds", "corso di laurea", "argomento", "titolo tesi", "titolo", "voto",
    )
    return parsed
        .map { (label, value) ->
            val normalizedLabel = when (label.lowercase()) {
                "discussione", "data tesi" -> getString(Res.string.ui_thesis_discussion_date)
                "titolo tesi" -> getString(Res.string.ui_thesis_title)
                "cds" -> getString(Res.string.msg_corso_di_laurea_2)
                else -> label
            }
            normalizedLabel to value
        }
        .sortedBy { (label, _) ->
            priorities.indexOf(label.lowercase()).takeIf { it >= 0 } ?: Int.MAX_VALUE
        }
}

// ============================================================================
// 5. Taxes Contract Builder
// ============================================================================

internal fun buildTaxesContract(taxes: Taxes): UniTaxesContract {
    val payments = taxes.toTaxPayments()
    val pending = payments.filterNot(TaxPaymentData::isPaid)
    val paid = payments.filter(TaxPaymentData::isPaid)
    val pendingSum = pending.sumOf { parseEuroAmountValue(it.amount) }
    val paidSum = paid.sumOf { parseEuroAmountValue(it.amount) }
    val declaredDue = parseEuroAmountValue(taxes.dueAmount)
    val effectiveDue = if (pendingSum > declaredDue) pendingSum else declaredDue
    val totalAmount = effectiveDue + paidSum
    val progress = if (totalAmount > 0.0) (paidSum / totalAmount).toFloat().coerceIn(0f, 1f) else 0f
    val firstUnpaid = taxes.installments.firstOrNull { !it.paid }

    return UniTaxesContract(
        dueAmountLabel = if (pendingSum > declaredDue) formatEuroAmountLabel(pendingSum) else formatTaxCurrencyLabel(taxes.dueAmount),
        rawDueAmount = taxes.dueAmount,
        paidTotalLabel = formatEuroAmountLabel(paidSum),
        paidProgress = progress,
        nextDueDate = pending.firstOrNull()?.date,
        nextUnpaidInstallmentLabel = firstUnpaid?.let { "${it.title} • ${it.deadline}" }.orEmpty(),
        allPayments = payments,
        pendingPayments = pending,
        paidPayments = paid,
    )
}

internal fun Taxes.toTaxPayments(): List<TaxPaymentData> =
    installments.map { installment ->
        TaxPaymentData(
            title = installment.title,
            date = installment.deadline,
            amount = installment.amount,
            isPaid = installment.paid,
            iuv = "",
        )
    }

internal fun formatTaxCurrencyLabel(raw: String): String =
    formatEuroAmountLabel(parseEuroAmountValue(raw))

internal fun formatEuroAmountLabel(amount: Double): String =
    "€ ${amount.coerceAtLeast(0.0).toFixedTwoDecimals()}"

internal fun parseEuroAmountValue(raw: String): Double {
    val clean = raw.replace("€", "").replace("EUR", "", ignoreCase = true).trim()
    if (clean.isEmpty()) return 0.0
    val normalized = when {
        clean.contains(',') && clean.contains('.') -> {
            if (clean.lastIndexOf(',') > clean.lastIndexOf('.')) {
                clean.replace(".", "").replace(',', '.')
            } else {
                clean.replace(",", "")
            }
        }
        clean.contains(',') -> clean.replace(',', '.')
        else -> clean
    }
    return normalized.toDoubleOrNull() ?: 0.0
}

// ============================================================================
// 6. Surveys & Attendance Contract Builders
// ============================================================================

internal fun buildSurveysContract(courses: List<SurveyCourse>): UniSurveysContract {
    val all = courses.toQuestionnaireDataList()
    val pending = all.filter { it.status == QuestionnaireStatus.PENDING }
    val completed = all.filter { it.status == QuestionnaireStatus.COMPLETED }
    val unavailable = all.filter { it.status == QuestionnaireStatus.UNAVAILABLE }
    val totalCount = pending.size + completed.size
    val progress = if (totalCount == 0) 0f else completed.size.toFloat() / totalCount

    return UniSurveysContract(
        all = all,
        pending = pending,
        completed = completed,
        unavailable = unavailable,
        pendingCount = courses.count { it.enabled && !it.completed },
        totalCount = totalCount,
        completedProgress = progress,
    )
}

internal fun List<SurveyCourse>.toQuestionnaireDataList(): List<QuestionnaireData> =
    map { course ->
        QuestionnaireData(
            course = course.title,
            prof = course.professor.orEmpty(),
            code = course.adCod.orEmpty(),
            status = when {
                course.completed -> QuestionnaireStatus.COMPLETED
                course.enabled -> QuestionnaireStatus.PENDING
                else -> QuestionnaireStatus.UNAVAILABLE
            },
            courseId = course.courseId,
            tagList = course.tagList,
        )
    }

internal fun SurveyForm.toQuestionnaireForm(): QuestionnaireForm =
    QuestionnaireForm(
        userCompId = userCompId,
        questCompId = questCompId,
        pages = pages.map { page ->
            QuestionnairePage(
                pageId = page.pageId,
                questions = page.questions.map { q ->
                    QuestionnaireQuestion(
                        questionId = q.questionId,
                        questionText = q.questionText,
                        answers = q.answers.map { a ->
                            QuestionnaireAnswerOption(
                                answerId = a.answerId,
                                answerText = a.answerText,
                                freeText = a.freeText,
                            )
                        },
                        required = q.required,
                        multipleChoice = q.multipleChoice,
                        freeTextEnabled = q.freeTextEnabled,
                        pageId = q.pageId,
                    )
                },
            )
        },
    )

internal fun buildSurveySaveRequest(
    studentId: String,
    userCompId: String,
    questCompId: String,
    form: QuestionnaireForm,
    selectedAnswers: Map<String, Set<String>>,
    freeTextAnswers: Map<String, String>,
): SurveySaveRequest =
    SurveySaveRequest(
        studentId = studentId,
        userCompId = userCompId,
        questCompId = questCompId,
        surveyPageDtos = form.pages.map { page ->
            SurveyPageSubmission(
                pageId = page.pageId,
                ansBodyDto = page.questions.flatMap { question ->
                    val selected = selectedAnswers[question.questionId].orEmpty()
                    selected.map { answerId ->
                        val isFreeText = question.answers.any { it.answerId == answerId && it.freeText }
                        SurveyAnswerSubmission(
                            questionId = question.questionId,
                            answerId = answerId,
                            answerBody = if (isFreeText) {
                                freeTextAnswers[question.questionId].orEmpty().trim()
                            } else {
                                ""
                            },
                        )
                    }
                },
            )
        },
    )

internal fun buildAttendanceContract(records: List<AttendanceRecord>): UniAttendanceContract {
    val courses = records.toAttendanceData()
    val totalCoursesCount = courses.size
    val totalLectures = courses.sumOf { it.records.size.takeIf { s -> s > 0 } ?: 1 }
    val validPercentages = courses.mapNotNull { it.percentage.replace("%", "").trim().toIntOrNull() }
    val averagePercent = if (validPercentages.isNotEmpty()) validPercentages.average().toInt() else 0

    return UniAttendanceContract(
        courses = courses,
        totalCoursesCount = totalCoursesCount,
        totalAttendedLectures = totalLectures,
        averageAttendancePercent = averagePercent,
    )
}

internal fun List<AttendanceRecord>.toAttendanceData(): List<AttendanceData> =
    groupBy(AttendanceRecord::courseName).map { (course, records) ->
        val attendedHours = records.sumOf { it.durationHours ?: 0.0 }
        val totalHours = records.mapNotNull(AttendanceRecord::courseTotalHours).maxOrNull()
        val percentage = totalHours?.takeIf { it > 0.0 }
            ?.let { ((attendedHours / it) * 100.0).coerceIn(0.0, 100.0).toInt() }
        AttendanceData(
            course = course,
            percentage = percentage?.let { "$it%" } ?: "—",
            count = "${records.size} presenze",
            attendedHours = attendedHours,
            totalHours = totalHours,
            records = records.map { rec ->
                SingleAttendanceEntry(
                    id = rec.id.orEmpty(),
                    date = rec.occurredAt.orEmpty(),
                    time = listOfNotNull(rec.startTime, rec.endTime).filter(String::isNotBlank).joinToString(" - "),
                    room = rec.room.orEmpty(),
                    teacher = rec.teacherName.orEmpty(),
                    hours = rec.durationHours,
                    status = rec.status.orEmpty(),
                )
            },
        )
    }

// ============================================================================
// 7. Transport Contract Builder
// ============================================================================

internal fun buildTransportContract(data: TransportData): UniTransportContract {
    val reservations = data.toReservations()
    val days = reservations.groupBy(TransportReservation::date).map { (date, list) ->
        val outgoing = list.count { it.direction == TripDirection.ANDATA }
        val returning = list.count { it.direction == TripDirection.RITORNO }
        val summary = buildList {
            if (outgoing > 0) add("$outgoing Andata")
            if (returning > 0) add("$returning Ritorno")
        }.joinToString(" • ")
        TransportDayGroup(
            date = date,
            reservations = list,
            directionSummary = summary.ifBlank { "${list.size} corse" },
        )
    }
    return UniTransportContract(
        reservations = reservations,
        days = days,
        routes = data.availableRoutes.map(TransportRoute::toTransportRouteOption),
        bookedRequests = bookedTransportRequests(data.bookings),
        totalCount = data.totalCount,
    )
}

internal fun TripBookingDirection.toSdkDirection(): TransportDirection =
    when (this) {
        TripBookingDirection.OUTBOUND -> TransportDirection.OUTBOUND
        TripBookingDirection.RETURN -> TransportDirection.RETURN
        TripBookingDirection.ROUND_TRIP -> TransportDirection.ROUND_TRIP
    }

internal fun TransportActionResult.toTripBookingOutcome(): TripBookingOutcome =
    when (this) {
        TransportActionResult.Completed -> TripBookingOutcome.COMPLETED
        TransportActionResult.AlreadyExists -> TripBookingOutcome.ALREADY_EXISTS
    }

internal fun TripBookingRequest.toSdkRequest(): TransportBookingRequest =
    TransportBookingRequest(
        routeCode = routeCode,
        date = date,
        direction = direction.toSdkDirection(),
    )

internal fun TransportRoute.toTransportRouteOption(): TransportRouteOption {
    val out = displayFor(TransportDirection.OUTBOUND)
    val ret = displayFor(TransportDirection.RETURN)
    return TransportRouteOption(
        code = code,
        label = label,
        outboundStops = TransportRouteStops(out.label, out.departureStop, out.arrivalStop),
        returnStops = TransportRouteStops(ret.label, ret.departureStop, ret.arrivalStop),
    )
}

internal fun resolveTransportRouteStops(
    routeLabel: String,
    direction: TripBookingDirection,
): TransportRouteStops {
    val sdkDisplay = TransportRoute(code = "", label = routeLabel).displayFor(direction.toSdkDirection())
    return TransportRouteStops(
        label = sdkDisplay.label,
        departureStop = sdkDisplay.departureStop,
        arrivalStop = sdkDisplay.arrivalStop,
    )
}

internal fun TransportData.toReservations(): List<TransportReservation> =
    bookings
        .sortedWith(
            compareBy<TransportBooking> { parseTransportTravelDate(it.date)?.sortKey?.div(10_000L) ?: Long.MAX_VALUE }
                .thenBy { it.isReturn }
                .thenBy { parseTransportTravelDate(it.date)?.sortKey ?: Long.MAX_VALUE }
                .thenBy { it.id },
        )
        .map { booking ->
            val travel = parseTransportTravelDate(booking.date)
            val route = displayFor(booking)
            TransportReservation(
                id = booking.id,
                route = route.label.ifBlank { booking.routeCode },
                date = travel?.displayDate.orEmpty(),
                time = travel?.time.orEmpty(),
                direction = if (booking.isReturn) TripDirection.RITORNO else TripDirection.ANDATA,
                qrCodeData = "",
                departureStop = route.departureStop,
                arrivalStop = route.arrivalStop,
                busNumber = "",
                ticketUrl = booking.ticketUrl,
                ticketNumber = booking.number,
            )
        }

internal fun bookedTransportRequests(bookings: List<TransportBooking>): Set<TripBookingRequest> =
    bookings.mapNotNull { booking ->
        val date = parseTransportTravelDate(booking.date)?.date ?: return@mapNotNull null
        val routeCode = booking.routeCode.trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
        TripBookingRequest(
            routeCode = routeCode.uppercase(),
            date = date,
            direction = if (booking.isReturn) TripBookingDirection.RETURN else TripBookingDirection.OUTBOUND,
        )
    }.toSet()

// ============================================================================
// 8. Sebina Library Text Parser & Contract Helpers
// ============================================================================

internal object LibraryTextParser {
    private val branchCodeLabels = mapOf(
        "MOLBU" to "Campobasso",
        "MOLBA" to "Pesche",
        "MOLBT" to "Termoli",
    )

    private val fullBranchLabels = mapOf(
        "MOLBU" to "Biblioteca di Ateneo · Campobasso",
        "MOLBA" to "Biblioteca di Ateneo · Pesche",
        "MOLBT" to "Biblioteca di Ateneo · Termoli",
    )

    private val yearRegex = Regex("""\b(19|20)\d{2}\b""")
    private val dateRegex = Regex("""\b(\d{1,2}/\d{1,2}/\d{4}|\d{4}-\d{2}-\d{2})\b""")
    private val timeRangeRegex = Regex("""\b(\d{1,2}:\d{2}\s*[–-]\s*\d{1,2}:\d{2})\b""")

    fun branchDisplayName(code: String): String =
        fullBranchLabels[code.trim().uppercase()] ?: code.trim()

    fun shortBranchName(code: String): String =
        branchCodeLabels[code.trim().uppercase()] ?: code.trim()

    fun parseCatalogBook(
        id: String,
        title: String,
        rawDescription: String,
    ): LibraryCatalogBook {
        val cleaned = normalizeText(rawDescription)
            .removePrefix("Libro moderno copertina ")
            .removePrefix("eBook copertina ")
            .trim()
        val afterTitle = stripTitlePrefix(cleaned, title)
        val splitIndex = afterTitle.indexOf("Lo trovi in", ignoreCase = true)
        val beforeLocations = if (splitIndex >= 0) afterTitle.substring(0, splitIndex).trim() else afterTitle
        val locationsRaw = if (splitIndex >= 0) afterTitle.substring(splitIndex + "Lo trovi in".length).trim() else ""

        val year = yearRegex.find(beforeLocations)?.value.orEmpty()
        val authorPart = if (year.isNotEmpty()) {
            beforeLocations.substringBefore(year).trim().trim(',', '-', ';')
        } else {
            beforeLocations.trim(',', '-', ';')
        }
        val branches = parseBranchNames(locationsRaw)

        val subtitle = authorPart.takeIf(String::isNotBlank)
            ?: branches.takeIf { it.isNotEmpty() }?.joinToString(" · ")
            ?: title

        return LibraryCatalogBook(
            id = id,
            title = title.trim(),
            subtitle = subtitle,
            metadata = year,
            branches = branches,
        )
    }

    fun parseBookDetail(
        id: String,
        title: String,
        rawDescription: String,
        actions: List<LibraryBookCopyAction>,
    ): LibraryBookDetail {
        val normalized = normalizeText(rawDescription)
        val cleaned = cleanBookDescription(normalized, title)
        val parsedSummary = parseCatalogBook(id, title, normalized)
        val hasAvailableDocument = normalized.contains("Documento disponibile", ignoreCase = true)

        val inventory = extractLabeledValue(
            normalized,
            label = "Inventario",
            stopLabels = listOf("Collocazione", "Note", "Scaffale"),
        )
        val shelfmark = extractLabeledValue(
            normalized,
            label = "Collocazione",
            stopLabels = listOf("Note", "Scaffale", "Biblioteca"),
        )
        val notes = extractLabeledValue(
            normalized,
            label = "Note",
            stopLabels = listOf("Scaffale", "Biblioteca", "Documento"),
        )
        val branches = (parsedSummary.branches + actions.map { shortBranchName(it.libraryCode) })
            .distinct()
            .filter(String::isNotBlank)

        return LibraryBookDetail(
            id = id,
            title = title.trim(),
            author = parsedSummary.subtitle.takeIf { it != title.trim() && it.isNotBlank() },
            year = parsedSummary.metadata.takeIf(String::isNotBlank),
            hasAvailableDocument = hasAvailableDocument,
            branches = branches,
            inventory = inventory,
            shelfmark = shelfmark,
            notes = notes,
            description = cleaned,
            actions = actions,
        )
    }

    fun parseMovement(
        id: String?,
        rawDescription: String,
        canCancel: Boolean,
        canRenew: Boolean,
        actionToken: String?,
    ): LibraryMovementItem {
        val text = normalizeText(rawDescription)
            .replace(" Cancella", "")
            .replace(" Proroga", "")
            .trim()
        val branch = when {
            text.contains("Campobasso", ignoreCase = true) -> "Campobasso"
            text.contains("Pesche", ignoreCase = true) -> "Pesche"
            text.contains("Termoli", ignoreCase = true) -> "Termoli"
            else -> null
        }
        val withoutBranch = if (branch != null) {
            text.removePrefix("Biblioteca $branch").removePrefix(branch).trim(' ', '·', '-', ':')
        } else {
            text
        }
        val dueDate = dateRegex.find(text)?.value
        return LibraryMovementItem(
            id = id,
            title = withoutBranch.ifBlank { text },
            detail = text,
            branchName = branch,
            dueDateLabel = dueDate,
            canCancel = canCancel,
            canRenew = canRenew,
            actionToken = actionToken,
        )
    }

    fun parseSpaceBooking(
        id: String?,
        rawDescription: String,
        canCancel: Boolean,
        bookingToken: String?,
    ): LibrarySpaceBooking {
        val text = normalizeText(rawDescription)
            .replace(" Annulla", "")
            .trim()
        val date = dateRegex.find(text)?.value
        val time = timeRangeRegex.find(text)?.value?.replace(Regex("""\s*[–-]\s*"""), " – ")
        val title = text
            .let { if (date != null) it.substringBefore(date) else it }
            .trim(' ', '·', '-', ':')
            .ifBlank { text }

        return LibrarySpaceBooking(
            id = id,
            title = title,
            description = text,
            dateLabel = date,
            timeLabel = time,
            canCancel = canCancel,
            bookingToken = bookingToken,
        )
    }

    fun parseSlot(
        slotId: String,
        label: String,
        rawDescription: String,
        requiresDeviceAddress: Boolean,
    ): LibrarySpaceSlot {
        val cleanDesc = normalizeText(rawDescription)
        val date = dateRegex.find(cleanDesc)?.value
        val timeRange = timeRangeRegex.find(cleanDesc)?.value?.replace(Regex("""\s*[–-]\s*"""), " – ")
        val primaryTime = timeRange ?: label.trim()
        val afterTime = if (timeRange != null) {
            cleanDesc.substringAfter(timeRangeRegex.find(cleanDesc)!!.value).trim()
        } else {
            ""
        }
        val tokens = afterTime.split(' ').filter(String::isNotBlank)
        val seatsLabel = tokens.firstOrNull()?.toIntOrNull()?.let { available ->
            val total = tokens.getOrNull(1)?.toIntOrNull()
            if (total != null && total >= available) "$available / $total posti" else "$available posti"
        }

        return LibrarySpaceSlot(
            slotId = slotId,
            label = label.trim(),
            timeLabel = primaryTime,
            description = cleanDesc,
            dateLabel = date,
            seatsLabel = seatsLabel,
            requiresDeviceAddress = requiresDeviceAddress,
        )
    }

    private fun parseBranchNames(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        val found = mutableListOf<String>()
        if (raw.contains("Campobasso", ignoreCase = true) || raw.contains("MOLBU", ignoreCase = true)) found += "Campobasso"
        if (raw.contains("Pesche", ignoreCase = true) || raw.contains("MOLBA", ignoreCase = true)) found += "Pesche"
        if (raw.contains("Termoli", ignoreCase = true) || raw.contains("MOLBT", ignoreCase = true)) found += "Termoli"
        return found.distinct()
    }

    private fun stripTitlePrefix(text: String, title: String): String {
        val cleanTitle = normalizeText(title)
        return if (cleanTitle.isNotEmpty() && text.startsWith(cleanTitle, ignoreCase = true)) {
            text.substring(cleanTitle.length).trim().trimStart('/', '-', ':', '·', ',')
        } else {
            text
        }
    }

    private fun cleanBookDescription(text: String, title: String): String {
        var result = text
            .removePrefix("Libro moderno copertina ")
            .removePrefix("eBook copertina ")
            .trim()
        result = stripTitlePrefix(result, title)
        val removePhrases = listOf(
            "Condividi su Facebook",
            "Condividi su Twitter",
            "Condividi su WhatsApp",
            "Aggiungi ai preferiti",
            "Invia per email",
            "Esporta citazione",
            "Stampa",
        )
        for (phrase in removePhrases) {
            result = result.replace(phrase, "", ignoreCase = true)
        }
        return normalizeText(result)
    }

    private fun extractLabeledValue(
        text: String,
        label: String,
        stopLabels: List<String>,
    ): String? {
        val pattern = Regex("""\b${Regex.escape(label)}\s*:?\s+""", RegexOption.IGNORE_CASE)
        val match = pattern.find(text) ?: return null
        val after = text.substring(match.range.last + 1).trim()
        var end = after.length
        for (stop in stopLabels) {
            val stopMatch = Regex("""\b${Regex.escape(stop)}\b""", RegexOption.IGNORE_CASE).find(after)
            if (stopMatch != null && stopMatch.range.first in 0 until end) {
                end = stopMatch.range.first
            }
        }
        return after.substring(0, end).trim().trim(',', ';').takeIf { it.isNotBlank() && it.length <= 80 }
    }

    private fun normalizeText(value: String): String =
        value.replace(Regex("""\s+"""), " ").trim()
}

// ============================================================================
// 9. Connected Devices Contract Builder
// ============================================================================

internal fun List<ConnectedDevice>.toDeviceInfo(): List<DeviceInfo> =
    map { device ->
        val displayName = listOfNotNull(device.manufacturer, device.model).joinToString(" ").ifBlank { "Dispositivo" }
        val locationLabel = listOfNotNull(device.platform, device.osVersion).joinToString(" • ")
        val type = when {
            device.platform?.contains("desktop", ignoreCase = true) == true -> DeviceType.PC
            device.model?.contains("tablet", ignoreCase = true) == true ||
                device.model?.contains("pad", ignoreCase = true) == true -> DeviceType.TABLET
            else -> DeviceType.PHONE
        }
        DeviceInfo(
            name = displayName,
            location = locationLabel,
            lastSeen = device.lastLogin,
            appVersion = device.appVersion,
            type = type,
            isCurrent = device.isCurrentDevice,
            id = device.token ?: device.serialCode.orEmpty(),
            revocationToken = device.token,
        )
    }

internal fun buildDevicesContract(
    devices: List<ConnectedDevice>,
    hasError: Boolean = false,
): UniDevicesContract {
    val mapped = devices.toDeviceInfo()
    val current = mapped.firstOrNull { it.isCurrent }
    val others = mapped.filterNot { it.isCurrent }
    return UniDevicesContract(
        devices = mapped,
        currentDevice = current,
        otherDevices = others,
        canDisconnectAll = !hasError && others.isNotEmpty(),
    )
}

// ============================================================================
// 10. Campus (News & Contacts) Contract Builders
// ============================================================================

internal fun List<UniversityContact>.toContacts(): List<ContactData> =
    map { contact ->
        val cleanId = contact.id?.trim().orEmpty()
        val cleanFirstName = contact.firstName?.trim().orEmpty()
        val cleanLastName = contact.lastName?.trim().orEmpty()
        val cleanName = contact.displayName.trim().ifBlank {
            listOfNotNull(contact.firstName, contact.lastName).map(String::trim).filter(String::isNotBlank).joinToString(" ")
        }
        val cleanEmail = contact.email?.trim().orEmpty()
        val cleanDepartment = contact.organization?.trim().orEmpty()
        val cleanRole = contact.role?.trim()?.takeIf(String::isNotBlank) ?: cleanDepartment
        val cleanCity = contact.city?.trim().orEmpty()
        val cleanAddress = contact.address?.trim().orEmpty()
        val cleanBuilding = contact.building?.trim().orEmpty()
        val cleanOfficeHours = contact.officeHours?.trim().orEmpty()
        val officeLabel = listOfNotNull(
            cleanBuilding.takeIf(String::isNotBlank),
            cleanAddress.takeIf(String::isNotBlank),
        ).joinToString(" • ")

        val rawPhones = contact.phones.map(String::trim).filter(String::isNotBlank).distinct()
        val (completePhones, extensions) = rawPhones.partition { phone -> phone.count(Char::isDigit) >= 7 }
        val sortedPhones = completePhones + extensions
        val primaryPhone = sortedPhones.firstOrNull().orEmpty()

        val category = when {
            cleanDepartment.contains("segreter", ignoreCase = true) -> ContactCategory.SECRETARIAT
            cleanDepartment.contains("serviz", ignoreCase = true) ||
                cleanDepartment.contains("ufficio", ignoreCase = true) -> ContactCategory.SERVICES
            else -> ContactCategory.TEACHERS
        }

        ContactData(
            name = cleanName,
            role = cleanRole,
            initials = cleanName.computeInitials(),
            email = cleanEmail,
            phone = primaryPhone,
            city = cleanCity,
            category = category,
            department = cleanDepartment,
            office = officeLabel,
            officeHours = cleanOfficeHours,
            id = cleanId,
            firstName = cleanFirstName,
            lastName = cleanLastName,
            phones = sortedPhones,
            address = cleanAddress,
            building = cleanBuilding,
        )
    }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

internal fun List<UniversityNews>.toNewsItems(): List<NewsItem> {
    val occurrences = mutableMapOf<String, Int>()
    return map { news ->
        val cleanTitle = NewsContentFormatter.formatTitle(news.title)
        val category = news.category?.trim().orEmpty()
        val publishedAt = news.publishedAt?.trim().orEmpty()
        val rawId = news.id?.trim()
        val identityParts = if (rawId.isNullOrBlank()) listOf(category.lowercase(), publishedAt, news.title)
        else listOf(category.lowercase(), rawId)
        val identity = identityParts.joinToString("|") { "${it.length}:$it" }
        val occurrence = occurrences.getOrElse(identity) { 0 }
        occurrences[identity] = occurrence + 1

        val formatted = NewsContentFormatter.formatArticle(
            title = cleanTitle,
            rawDescription = news.summary.orEmpty(),
            rawContent = news.content.orEmpty(),
            sourceUrl = news.sourceUrl,
        )
        val rawSummary = news.summary.orEmpty()
        val rawBody = news.content.orEmpty()
        val resolvedSourceUrl = NewsContentFormatter.resolveAssetUrl(news.sourceUrl)
            ?: news.sourceUrl?.trim()?.takeIf(String::isNotBlank)

        val item = NewsItem(
            title = cleanTitle,
            description = formatted.summaryText,
            fullContent = rawBody.ifBlank { rawSummary },
            type = news.category.toNewsType(),
            category = category,
            publishedAt = publishedAt,
            key = "${datasetCacheKey("news", identity)}#$occurrence",
            sourceUrl = resolvedSourceUrl,
        )
        item to publishedAt.newsDateSortKey()
    }.sortedWith(compareByDescending<Pair<NewsItem, Long>> { it.second }.thenBy { it.first.title })
        .map { it.first }
}

private fun String?.toNewsType(): LiquidStatusType =
    when {
        this?.contains("scad", ignoreCase = true) == true -> LiquidStatusType.Warning
        this?.contains("success", ignoreCase = true) == true -> LiquidStatusType.Success
        else -> LiquidStatusType.Info
    }

data class FormattedNewsAttachment(
    val label: String,
    val url: String,
    val isDocument: Boolean,
)

data class FormattedNewsContent(
    val summaryText: String,
    val distinctSummary: String?,
    val articleMarkdown: String,
    val attachments: List<FormattedNewsAttachment>,
    val readingMinutes: Int,
)

private val NewsDatePattern = Regex(
    """(?:(\d{4})-(\d{1,2})-(\d{1,2})|(\d{1,2})/(\d{1,2})/(\d{4}))(?:\s*[T\s•\-]\s*(\d{1,2}):(\d{2}))?""",
)

internal fun String.newsDateSortKey(): Long {
    val match = NewsDatePattern.find(this) ?: return Long.MIN_VALUE
    val (year, month, day) = if (match.groupValues[1].isNotEmpty()) {
        Triple(match.groupValues[1], match.groupValues[2], match.groupValues[3])
    } else {
        Triple(match.groupValues[6], match.groupValues[5], match.groupValues[4])
    }
    val dateKey = year.toLongOrNull()?.times(10_000L)
        ?.plus((month.toLongOrNull() ?: 0L) * 100L)
        ?.plus(day.toLongOrNull() ?: 0L) ?: return Long.MIN_VALUE
    val hour = match.groupValues[7].toLongOrNull()?.coerceIn(0L, 23L) ?: 0L
    val minute = match.groupValues[8].toLongOrNull()?.coerceIn(0L, 59L) ?: 0L
    return dateKey * 10_000L + hour * 100L + minute
}

internal fun String?.newsSourceUrlOrNull(): String? {
    val raw = this?.trim()?.takeIf(String::isNotBlank) ?: return null
    val candidate = if (raw.startsWith("www.", ignoreCase = true)) "https://$raw" else raw
    if (candidate.any(Char::isWhitespace)) return null
    val parsed = runCatching { Url(candidate) }.getOrNull() ?: return null
    val scheme = parsed.protocol.name.lowercase()
    if ((scheme != "https" && scheme != "http") || parsed.host.isBlank()) return null
    return candidate
}

object NewsContentFormatter {
    private const val DEFAULT_NEWS_ORIGIN = "https://apistrapi.unimol.it"
    private const val WORDS_PER_MINUTE = 190.0

    private val scriptOrStyleRegex =
        Regex("<(script|style)\\b[^>]*>[\\s\\S]*?</\\1>", setOf(RegexOption.IGNORE_CASE))
    private val imageTagRegex =
        Regex("<img\\b[^>]*>", setOf(RegexOption.IGNORE_CASE))
    private val anchorTagRegex =
        Regex("<a\\b([^>]*)>([\\s\\S]*?)</a>", setOf(RegexOption.IGNORE_CASE))
    private val hrefAttrRegex =
        Regex("""href\s*=\s*["']([^"']+)["']""", setOf(RegexOption.IGNORE_CASE))
    private val headingTagRegex =
        Regex("<h([1-6])\\b[^>]*>([\\s\\S]*?)</h\\1>", setOf(RegexOption.IGNORE_CASE))
    private val orderedListRegex =
        Regex("<ol\\b[^>]*>([\\s\\S]*?)</ol>", setOf(RegexOption.IGNORE_CASE))
    private val unorderedListRegex =
        Regex("<ul\\b[^>]*>([\\s\\S]*?)</ul>", setOf(RegexOption.IGNORE_CASE))
    private val listItemRegex =
        Regex("<li\\b[^>]*>([\\s\\S]*?)</li>", setOf(RegexOption.IGNORE_CASE))
    private val lineBreakRegex =
        Regex("<br\\s*/?>", setOf(RegexOption.IGNORE_CASE))
    private val blockCloseRegex =
        Regex("</(p|div|section|article|blockquote|tr|table)\\s*>", setOf(RegexOption.IGNORE_CASE))
    private val blockOpenRegex =
        Regex("<(p|div|section|article|blockquote|tr|table)\\b[^>]*>", setOf(RegexOption.IGNORE_CASE))
    private val strongTagRegex =
        Regex("<(strong|b)\\b[^>]*>([\\s\\S]*?)</\\1>", setOf(RegexOption.IGNORE_CASE))
    private val emphasisTagRegex =
        Regex("<(em|i)\\b[^>]*>([\\s\\S]*?)</\\1>", setOf(RegexOption.IGNORE_CASE))
    private val remainingHtmlTagRegex =
        Regex("</?[a-zA-Z][^>]*>")
    private val markdownImageRegex =
        Regex("""!\[[^\]]*]\([^)]*\)""")
    private val markdownLinkRegex =
        Regex("""\[([^\]]+)]\(([^)]+)\)""")
    private val rawUrlRegex =
        Regex("""https?://[^\s<>()"\]]+""", setOf(RegexOption.IGNORE_CASE))
    private val htmlEntityRegex =
        Regex("""&(#x[0-9a-fA-F]+|#\d+|[a-zA-Z]+);""")
    private val excessBlankLinesRegex =
        Regex("""\n{3,}""")
    private val horizontalSpacesRegex =
        Regex("""[ \t\u00A0]{2,}""")
    private val trailingHashClusterRegex =
        Regex("""(?:_[0-9a-fA-F]{6,16})+$""")
    private val sentenceSplitBoundaryRegex =
        Regex("""(?<=[.!?])\s+(?=[A-ZÀÈÉÌÒÙ"«“])""")

    private val documentExtensions = listOf(
        ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".odt", ".ods", ".zip", ".rar",
    )
    private val imageExtensions = listOf(
        ".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg", ".bmp", ".avif",
    )

    fun formatTitle(rawTitle: String): String =
        toPlainText(rawTitle).lineSequence().joinToString(" ").trim()

    fun toPlainText(raw: String): String {
        if (raw.isBlank()) return ""
        val withoutNoise = raw
            .replace(scriptOrStyleRegex, " ")
            .replace(imageTagRegex, " ")
            .replace(markdownImageRegex, " ")
            .replace(anchorTagRegex) { match -> match.groupValues[2] }
            .replace(lineBreakRegex, "\n")
            .replace(blockCloseRegex, "\n")
            .replace(blockOpenRegex, " ")
            .replace(remainingHtmlTagRegex, " ")

        val decoded = decodeHtmlEntities(withoutNoise)
            .replace(markdownLinkRegex) { match -> match.groupValues[1] }

        return decoded
            .lineSequence()
            .map { line -> line.replace(horizontalSpacesRegex, " ").trim() }
            .filter(String::isNotBlank)
            .joinToString("\n")
    }

    fun formatMarkdownBody(
        description: String,
        fullContent: String,
        summaryHeading: String,
        articleHeading: String,
    ): String {
        val summary = description.trim()
        val article = fullContent.trim()
        val distinctSummary = summary.isNotBlank() && article.isNotBlank() &&
            !article.startsWith(summary, ignoreCase = true)
        return buildString {
            if (distinctSummary) {
                append("## ").append(summaryHeading).append("\n\n")
                append(summary).append("\n\n---\n\n")
            }
            if (article.isNotBlank() || summary.isNotBlank()) {
                append("## ").append(articleHeading).append("\n\n")
                append(if (article.isNotBlank()) article else summary)
            }
        }
    }

    fun formatArticle(
        title: String,
        rawDescription: String,
        rawContent: String,
        sourceUrl: String?,
        attachmentFallbackLabel: String? = null,
    ): FormattedNewsContent {
        val attachments = LinkedHashMap<String, FormattedNewsAttachment>()
        val cleanSummary = cleanBodyToMarkdown(rawDescription, sourceUrl, attachments, attachmentFallbackLabel)
        val cleanArticle = cleanBodyToMarkdown(rawContent, sourceUrl, attachments, attachmentFallbackLabel)

        val summaryPlain = toPlainText(rawDescription)
        val primaryMarkdown = cleanArticle.ifBlank { cleanSummary }
        val normalizedSummary = cleanSummary.trim()
        val normalizedPrimary = primaryMarkdown.trim()

        val distinctSummary = normalizedSummary.takeIf { summary ->
            summary.isNotBlank() &&
                normalizedPrimary.isNotBlank() &&
                !normalizedPrimary.equals(summary, ignoreCase = true) &&
                !normalizedPrimary.startsWith(summary.take(80), ignoreCase = true) &&
                !summary.equals(title.trim(), ignoreCase = true)
        }

        val wordCount = (distinctSummary.orEmpty() + " " + normalizedPrimary)
            .split(Regex("\\s+"))
            .count(String::isNotBlank)
        val readingMinutes = ceil(wordCount / WORDS_PER_MINUTE).toInt().coerceAtLeast(1)

        return FormattedNewsContent(
            summaryText = summaryPlain.ifBlank { toPlainText(rawContent) },
            distinctSummary = distinctSummary,
            articleMarkdown = normalizedPrimary,
            attachments = attachments.values.toList(),
            readingMinutes = readingMinutes,
        )
    }

    fun resolveAssetUrl(rawUrl: String?, baseOriginUrl: String? = null): String? {
        val trimmed = rawUrl?.trim()?.takeIf(String::isNotBlank) ?: return null
        val decoded = decodeHtmlEntities(trimmed).replace(" ", "%20")
        val normalizedCandidate = when {
            decoded.startsWith("https://", ignoreCase = true) -> decoded
            decoded.startsWith("http://", ignoreCase = true) ->
                if (decoded.contains("unimol.it", ignoreCase = true)) {
                    "https://" + decoded.removePrefix("http://").removePrefix("HTTP://")
                } else {
                    decoded
                }
            decoded.startsWith("//") -> "https:$decoded"
            decoded.startsWith("www.", ignoreCase = true) -> "https://$decoded"
            decoded.startsWith("/") -> "${extractOrigin(baseOriginUrl)}$decoded"
            decoded.startsWith("uploads/", ignoreCase = true) -> "${extractOrigin(baseOriginUrl)}/$decoded"
            else -> return null
        }
        return normalizedCandidate.newsSourceUrlOrNull()
    }

    private fun extractOrigin(sourceUrl: String?): String {
        val valid = sourceUrl.newsSourceUrlOrNull() ?: return DEFAULT_NEWS_ORIGIN
        val parsed = runCatching { Url(valid) }.getOrNull() ?: return DEFAULT_NEWS_ORIGIN
        return "${parsed.protocol.name.lowercase()}://${parsed.host}"
    }

    private fun cleanBodyToMarkdown(
        raw: String,
        sourceUrl: String?,
        attachments: LinkedHashMap<String, FormattedNewsAttachment>,
        attachmentFallbackLabel: String?,
    ): String {
        if (raw.isBlank()) return ""

        var text = raw
            .replace("\r\n", "\n")
            .replace('\r', '\n')
            .replace(scriptOrStyleRegex, "")
            .replace(imageTagRegex, "")
            .replace(markdownImageRegex, "")

        text = text.replace(anchorTagRegex) { match ->
            val attrs = match.groupValues[1]
            val innerHtml = match.groupValues[2]
            val rawHref = hrefAttrRegex.find(attrs)?.groupValues?.getOrNull(1)
            val resolvedHref = resolveAssetUrl(rawHref, sourceUrl)
            val label = toPlainText(innerHtml).lineSequence().joinToString(" ").trim()

            if (resolvedHref == null) {
                label
            } else if (isImageUrl(resolvedHref)) {
                ""
            } else {
                val isDoc = isDocumentUrl(resolvedHref)
                val attachmentLabel = buildAttachmentLabel(label, resolvedHref, attachmentFallbackLabel)
                attachments.getOrPut(resolvedHref) {
                    FormattedNewsAttachment(
                        label = attachmentLabel,
                        url = resolvedHref,
                        isDocument = isDoc,
                    )
                }
                if (label.isBlank() || label.equals(rawHref?.trim(), ignoreCase = true) || looksLikeRawFilename(label)) {
                    attachmentLabel
                } else {
                    "[$label]($resolvedHref)"
                }
            }
        }

        text = text.replace(markdownLinkRegex) { match ->
            val label = decodeHtmlEntities(match.groupValues[1]).trim()
            val target = match.groupValues[2].trim()
            val resolved = resolveAssetUrl(target, sourceUrl)
            if (resolved == null) {
                label
            } else if (isImageUrl(resolved)) {
                ""
            } else {
                val isDoc = isDocumentUrl(resolved)
                val attachmentLabel = buildAttachmentLabel(label, resolved, attachmentFallbackLabel)
                attachments.getOrPut(resolved) {
                    FormattedNewsAttachment(
                        label = attachmentLabel,
                        url = resolved,
                        isDocument = isDoc,
                    )
                }
                if (label.isBlank() || looksLikeRawFilename(label)) {
                    attachmentLabel
                } else {
                    "[$label]($resolved)"
                }
            }
        }

        text = text.replace(headingTagRegex) { match ->
            val level = (match.groupValues[1].toIntOrNull() ?: 2).coerceIn(2, 4)
            val headingText = toPlainText(match.groupValues[2]).lineSequence().joinToString(" ").trim()
            if (headingText.isBlank()) "" else "\n\n${"#".repeat(level)} $headingText\n\n"
        }

        text = text.replace(orderedListRegex) { match ->
            val items = listItemRegex.findAll(match.groupValues[1])
                .mapIndexedNotNull { index, item ->
                    val itemText = cleanInlineHtml(item.groupValues[1])
                    itemText.takeIf(String::isNotBlank)?.let { "${index + 1}. $it" }
                }
                .toList()
            if (items.isEmpty()) "" else "\n\n" + items.joinToString("\n") + "\n\n"
        }

        text = text.replace(unorderedListRegex) { match ->
            val items = listItemRegex.findAll(match.groupValues[1])
                .mapNotNull { item ->
                    val itemText = cleanInlineHtml(item.groupValues[1])
                    itemText.takeIf(String::isNotBlank)?.let { "- $it" }
                }
                .toList()
            if (items.isEmpty()) "" else "\n\n" + items.joinToString("\n") + "\n\n"
        }

        text = text.replace(listItemRegex) { match ->
            val itemText = cleanInlineHtml(match.groupValues[1])
            if (itemText.isBlank()) "" else "\n- $itemText\n"
        }

        text = text
            .replace(strongTagRegex) { match ->
                val inner = cleanInlineHtml(match.groupValues[2])
                if (inner.isBlank()) "" else "**$inner**"
            }
            .replace(emphasisTagRegex) { match ->
                val inner = cleanInlineHtml(match.groupValues[2])
                if (inner.isBlank()) "" else "*$inner*"
            }
            .replace(lineBreakRegex, "\n")
            .replace(blockCloseRegex, "\n\n")
            .replace(blockOpenRegex, "\n\n")
            .replace(remainingHtmlTagRegex, "")

        text = decodeHtmlEntities(text)

        rawUrlRegex.findAll(text).forEach { match ->
            val start = match.range.first
            if (start >= 2 && text[start - 1] == '(' && text[start - 2] == ']') return@forEach
            val resolved = resolveAssetUrl(match.value, sourceUrl) ?: return@forEach
            if (!isImageUrl(resolved)) {
                attachments.getOrPut(resolved) {
                    FormattedNewsAttachment(
                        label = buildAttachmentLabel("", resolved, attachmentFallbackLabel),
                        url = resolved,
                        isDocument = isDocumentUrl(resolved),
                    )
                }
            }
        }

        val normalizedLines = text
            .lineSequence()
            .map { line -> line.replace(horizontalSpacesRegex, " ").trim() }
            .toList()

        val blocks = mutableListOf<String>()
        val paragraphBuffer = mutableListOf<String>()

        fun flushParagraph() {
            if (paragraphBuffer.isEmpty()) return
            val joined = paragraphBuffer.joinToString(" ").trim()
            paragraphBuffer.clear()
            if (joined.isNotBlank()) {
                blocks += splitProseIntoReadableParagraphs(joined)
            }
        }

        for (line in normalizedLines) {
            when {
                line.isBlank() -> flushParagraph()
                isStructuralMarkdownLine(line) -> {
                    flushParagraph()
                    blocks += line
                }
                else -> paragraphBuffer += line
            }
        }
        flushParagraph()

        return blocks
            .joinToString("\n\n")
            .replace(excessBlankLinesRegex, "\n\n")
            .trim()
    }

    private fun isStructuralMarkdownLine(line: String): Boolean =
        line.startsWith("#") ||
            line.startsWith("- ") ||
            line.startsWith("* ") ||
            line.startsWith("> ") ||
            line.startsWith("```") ||
            line.startsWith("|") ||
            line == "---" ||
            Regex("^\\d+\\.\\s+.*").matches(line)

    internal fun splitProseIntoReadableParagraphs(
        paragraph: String,
        minLength: Int = 220,
    ): List<String> {
        if (paragraph.length < minLength) return listOf(paragraph)
        val sentences = sentenceSplitBoundaryRegex.split(paragraph)
            .map(String::trim)
            .filter(String::isNotBlank)
        if (sentences.size < 3) return listOf(paragraph)

        val chunks = mutableListOf<String>()
        val current = StringBuilder()
        var sentenceCount = 0

        for (sentence in sentences) {
            if (current.isNotEmpty()) current.append(' ')
            current.append(sentence)
            sentenceCount++
            if (current.length >= 150 && sentenceCount >= 2) {
                chunks += current.toString().trim()
                current.clear()
                sentenceCount = 0
            }
        }
        if (current.isNotEmpty()) {
            chunks += current.toString().trim()
        }
        return chunks.ifEmpty { listOf(paragraph) }
    }

    private fun cleanInlineHtml(raw: String): String =
        decodeHtmlEntities(
            raw.replace(lineBreakRegex, " ")
                .replace(remainingHtmlTagRegex, " "),
        ).replace(horizontalSpacesRegex, " ").trim()

    private fun isImageUrl(url: String): Boolean {
        val cleanPath = url.substringBefore('?').substringBefore('#').lowercase()
        return imageExtensions.any(cleanPath::endsWith)
    }

    private fun isDocumentUrl(url: String): Boolean {
        val cleanPath = url.substringBefore('?').substringBefore('#').lowercase()
        return documentExtensions.any(cleanPath::endsWith) || cleanPath.contains("/uploads/")
    }

    private fun looksLikeRawFilename(label: String): Boolean {
        val trimmed = label.trim()
        val lower = trimmed.lowercase()
        return documentExtensions.any(lower::endsWith) ||
            imageExtensions.any(lower::endsWith) ||
            trimmed.contains('_') && trailingHashClusterRegex.containsMatchIn(trimmed.substringBeforeLast('.'))
    }

    private fun buildAttachmentLabel(
        rawLabel: String,
        url: String,
        attachmentFallbackLabel: String?,
    ): String {
        val cleanRaw = rawLabel.trim()
        if (cleanRaw.isNotBlank() &&
            !cleanRaw.startsWith("http://", ignoreCase = true) &&
            !cleanRaw.startsWith("https://", ignoreCase = true) &&
            !looksLikeRawFilename(cleanRaw)
        ) {
            return cleanRaw
        }

        val segment = url.substringBefore('?').substringBefore('#')
            .substringAfterLast('/')
            .trim()
        if (segment.isBlank()) {
            return runCatching { Url(url).host.removePrefix("www.") }.getOrNull()
                ?.takeIf(String::isNotBlank)
                ?: attachmentFallbackLabel.orEmpty()
        }

        val ext = segment.substringAfterLast('.', missingDelimiterValue = "").uppercase()
        val baseName = segment.substringBeforeLast('.', missingDelimiterValue = segment)
            .replace(trailingHashClusterRegex, "")
            .replace('_', ' ')
            .replace('-', ' ')
            .replace(horizontalSpacesRegex, " ")
            .trim()

        if (baseName.isBlank()) {
            return if (ext.isNotBlank()) {
                val baseLabel = attachmentFallbackLabel?.takeIf(String::isNotBlank)
                if (baseLabel != null) "$baseLabel ($ext)" else ext
            } else {
                attachmentFallbackLabel.orEmpty()
            }
        }
        return if (ext.isNotBlank() && ext.length <= 5) "$baseName ($ext)" else baseName
    }

    internal fun decodeHtmlEntities(input: String): String {
        if (!input.contains('&')) return input
        val namedEntities = mapOf(
            "nbsp" to " ",
            "amp" to "&",
            "lt" to "<",
            "gt" to ">",
            "quot" to "\"",
            "apos" to "'",
            "rsquo" to "’",
            "lsquo" to "‘",
            "rdquo" to "”",
            "ldquo" to "“",
            "ndash" to "–",
            "mdash" to "—",
            "hellip" to "…",
            "bull" to "•",
            "middot" to "·",
            "laquo" to "«",
            "raquo" to "»",
            "euro" to "€",
            "deg" to "°",
            "ordm" to "º",
            "ordf" to "ª",
            "agrave" to "à",
            "egrave" to "è",
            "eacute" to "é",
            "igrave" to "ì",
            "ograve" to "ò",
            "ugrave" to "ù",
            "Agrave" to "À",
            "Egrave" to "È",
            "Eacute" to "É",
            "Igrave" to "Ì",
            "Ograve" to "Ò",
            "Ugrave" to "Ù",
        )
        return htmlEntityRegex.replace(input) { match ->
            val entity = match.groupValues[1]
            when {
                entity.startsWith("#x", ignoreCase = true) ->
                    entity.drop(2).toIntOrNull(16)?.takeIf { it in 1..0x10FFFF }?.let { Char(it).toString() }
                        ?: match.value
                entity.startsWith("#") ->
                    entity.drop(1).toIntOrNull()?.takeIf { it in 1..0x10FFFF }?.let { Char(it).toString() }
                        ?: match.value
                else -> namedEntities[entity] ?: namedEntities[entity.lowercase()] ?: match.value
            }
        }
    }
}
