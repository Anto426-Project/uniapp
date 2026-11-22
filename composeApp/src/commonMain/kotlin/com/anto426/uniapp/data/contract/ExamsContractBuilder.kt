package com.anto426.uniapp.data.contract

import com.anto426.uniapp.didactics.presentation.AcademicSection
import com.anto426.unisdk.model.Career
import com.anto426.unisdk.model.CareerExam
import com.anto426.unisdk.model.ExamRound
import com.anto426.unisdk.model.ProfessorContentItem
import com.anto426.unisdk.model.ProfessorDashboard
import com.anto426.unisdk.model.ProfessorDetailField
import com.anto426.unisdk.model.ExamRoundBookingState
import com.anto426.unisdk.model.parseExamRoundDateOrNull
import com.anto426.unisdk.model.parseExamRoundDateTimeOrNull
import com.anto426.unisdk.model.resolveExamRoundBookingState
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

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
