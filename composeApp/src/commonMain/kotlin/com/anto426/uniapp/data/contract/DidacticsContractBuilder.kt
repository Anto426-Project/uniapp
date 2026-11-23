package com.anto426.uniapp.data.contract

import com.anto426.unisdk.model.Career
import com.anto426.unisdk.model.CareerExam
import com.anto426.unisdk.model.CourseSyllabus
import com.anto426.unisdk.model.ExamRound
import com.anto426.unisdk.model.StudyPlan
import com.anto426.unisdk.model.StudyPlanCourse
import kotlin.math.abs
import kotlin.math.roundToLong

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

internal fun String.normalizeAcademicTitle(): String =
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

    val isGenericCourseTitle = adDes.trim().let { raw ->
        raw.isBlank() ||
            raw.equals("Attivita Didattica", ignoreCase = true) ||
            raw.equals("Attività Didattica", ignoreCase = true)
    }
    val resolvedCourseName =
        (if (!isGenericCourseTitle) {
            adDes.trim()
        } else {
            studyPlanCourse?.title?.trim()?.takeIf(String::isNotBlank)
                ?: careerExam?.name?.trim()?.takeIf(String::isNotBlank)
                ?: adDes.trim()
        }).toDisplayCourseTitle()

    val formattedProfessor =
        professors
            .mapNotNull { it.trim().takeIf(String::isNotBlank)?.toDisplayAcademicName() }
            .distinct()
            .takeIf { it.isNotEmpty() }
            ?.joinToString(", ")
            ?: professorsLabel?.trim()?.takeIf(String::isNotBlank)?.toDisplayAcademicName().orEmpty()

    return StudyCourse(
        id = adsceId,
        name = resolvedCourseName,
        cfu = effectiveCfu?.let { "$it CFU" }.orEmpty(),
        status = if (isCompleted) CourseStatus.COMPLETED else CourseStatus.ACTIVE,
        professor = formattedProfessor,
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
        courseUrl = courseUrl,
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

private fun String.toDisplayCourseTitle(): String {
    val trimmed = trim().replace(Regex("""\s+"""), " ")
    if (trimmed.isBlank()) return ""
    val letters = trimmed.filter(Char::isLetter)
    if (letters.isEmpty() || letters.any(Char::isLowerCase)) return trimmed
    val smallWords = setOf("di", "e", "ed", "per", "con", "su", "in", "a", "ad", "da", "del", "della", "delle", "degli", "dei", "dello", "al", "alla", "alle", "agli", "ai", "nel", "nella", "nelle", "negli", "nei", "il", "lo", "la", "i", "gli", "le", "un", "una", "uno", "of", "and", "for", "in", "to", "the")
    val romanNumerals = setOf("I", "II", "III", "IV", "V", "VI")
    return trimmed.split(" ").mapIndexed { idx, token ->
        val clean = token.trim()
        when {
            clean.isBlank() -> clean
            clean in romanNumerals -> clean
            idx > 0 && clean.lowercase() in smallWords -> clean.lowercase()
            else -> clean.lowercase().replaceFirstChar { ch -> ch.titlecase() }
        }
    }.joinToString(" ")
}

private fun String.toDisplayAcademicName(): String {
    val trimmed = trim().replace(Regex("""\s+"""), " ")
    if (trimmed.isBlank()) return ""
    val letters = trimmed.filter(Char::isLetter)
    if (letters.isEmpty() || letters.any(Char::isLowerCase)) return trimmed
    return trimmed.split(" ").joinToString(" ") { word ->
        word.split("'").joinToString("'") { part ->
            val lower = part.lowercase()
            lower.replaceFirstChar { ch -> ch.titlecase() }
        }
    }
}

private fun findMatchingProfessorContact(
    professorName: String,
    contacts: List<ContactData>?,
): ContactData? {
    if (professorName.isBlank() || contacts.isNullOrEmpty()) return null
    val candidates = professorName.split(",", ";", "/").map(String::trim).filter(String::isNotBlank)
    for (candidate in candidates) {
        val cleanProf = candidate.lowercase()
            .replace("prof.ssa", "")
            .replace("prof.", "")
            .replace("dott.ssa", "")
            .replace("dott.", "")
            .replace("ing.", "")
            .trim()
        val profTokens = cleanProf.split(" ", "-").map(String::trim).filter { it.length > 2 }
        if (profTokens.isEmpty()) continue

        val match = contacts.firstOrNull { contact ->
            val cleanContact = contact.name.lowercase()
            val contactTokens = cleanContact.split(" ", "-", ",").map(String::trim).filter { it.length > 2 }
            (profTokens.size >= 2 && profTokens.all { token -> cleanContact.contains(token) }) ||
                (contactTokens.size >= 2 && contactTokens.all { token -> cleanProf.contains(token) }) ||
                cleanContact == cleanProf
        }
        if (match != null) return match
    }
    return null
}
