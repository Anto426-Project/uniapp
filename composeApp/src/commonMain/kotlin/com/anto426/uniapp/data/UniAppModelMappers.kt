package com.anto426.uniapp.data

import com.anto426.liquidmonet.components.cards.LiquidStatusType
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.didactics.AttendanceData
import com.anto426.uniapp.model.didactics.CourseStatus
import com.anto426.uniapp.model.didactics.ExamRecord
import com.anto426.uniapp.model.didactics.ExamSession
import com.anto426.uniapp.model.didactics.GradeExam
import com.anto426.uniapp.model.didactics.GradeSimulationPreset
import com.anto426.uniapp.model.didactics.StudyCourse
import com.anto426.uniapp.model.didactics.StudyYear
import com.anto426.uniapp.model.news.NewsItem
import com.anto426.uniapp.model.services.ContactCategory
import com.anto426.uniapp.model.services.ContactData
import com.anto426.uniapp.model.services.TaxPaymentData
import com.anto426.uniapp.model.settings.DeviceInfo
import com.anto426.uniapp.model.settings.DeviceType
import com.anto426.uniapp.model.transport.TransportReservation
import com.anto426.uniapp.model.transport.TransportRoute
import com.anto426.uniapp.model.transport.TransportTicket
import com.anto426.uniapp.model.transport.TripDirection
import com.anto426.unisdk.backend.model.AttendanceRecord
import com.anto426.unisdk.backend.model.CareerData
import com.anto426.unisdk.backend.model.ConnectedDeviceData
import com.anto426.unisdk.backend.model.CourseSyllabusData
import com.anto426.unisdk.backend.model.ExamRoundData
import com.anto426.unisdk.backend.model.StudyPlanData
import com.anto426.unisdk.backend.model.TaxesData
import com.anto426.unisdk.backend.model.UniversityContact
import com.anto426.unisdk.backend.model.UniversityNews
import com.anto426.unisdk.transport.TransportData

internal fun CareerData.toExamRecords(): List<ExamRecord> {
    val years = exams.mapNotNull { it.date.calendarYearOrNull() }.distinct().sorted()
    return exams.map { exam ->
        ExamRecord(
            name = exam.name,
            grade = exam.grade,
            cfu = exam.cfu?.let { "$it CFU" }.orEmpty(),
            date = exam.date,
            year = years.indexOf(exam.date.calendarYearOrNull()).takeIf { it >= 0 }?.plus(1) ?: 1,
            code = exam.adsceId.orEmpty(),
            lode = exam.grade.contains("L", ignoreCase = true),
        )
    }
}

internal fun CareerData.toGradeExams(): List<GradeExam> =
    exams.mapNotNull { exam ->
        val grade = exam.grade.numericGradeOrNull() ?: return@mapNotNull null
        GradeExam(exam.name, grade, exam.cfu ?: 0)
    }

internal fun List<ExamRoundData>.toExamSessions(): List<ExamSession> =
    map { round ->
        val date = round.dateTime.substringBefore(' ').substringBefore('T')
        val time =
            when {
                'T' in round.dateTime -> round.dateTime.substringAfter('T').take(5)
                ' ' in round.dateTime -> round.dateTime.substringAfter(' ').take(5)
                else -> ""
            }
        ExamSession(
            name = round.courseName,
            date = date,
            time = time,
            room = round.room,
            bookingOpenDate = round.registrationStartingDate.orEmpty(),
            bookingCloseDate = round.registrationEndingDate.orEmpty(),
            type = round.registrationTypeDescription,
            professor = round.presidentFullName.orEmpty(),
            bookedUsersCount = round.totalRegistrations ?: 0,
            availableSlots = round.availableSlots,
            notes = round.notes.orEmpty(),
            canBook = round.isBookable,
            isBooked = round.booked,
            id = round.stableUiId(),
        )
    }

internal fun List<ExamRoundData>.toSimulationPresets(): List<GradeSimulationPreset> =
    filterNot(ExamRoundData::booked)
        .distinctBy(ExamRoundData::courseName)
        .map { GradeSimulationPreset(it.courseName, cfu = 6, initialGrade = 24) }

internal fun StudyPlanData.toStudyYears(): List<StudyYear> =
    courses
        .map { course ->
            StudyCourse(
                id = course.adsceId.orEmpty(),
                name = course.title,
                cfu = course.cfu?.let { "$it CFU" }.orEmpty(),
                status = if (course.completed) CourseStatus.COMPLETED else CourseStatus.PLANNED,
                description = course.category.orEmpty(),
                semester = course.completionDate.orEmpty(),
            ) to (course.year ?: 1).coerceAtLeast(1)
        }
        .groupBy(Pair<StudyCourse, Int>::second)
        .toList()
        .sortedBy { (yearNumber, _) -> yearNumber }
        .map { (yearNumber, courses) ->
            StudyYear(yearNumber, "${yearNumber}° Anno", courses.map(Pair<StudyCourse, Int>::first))
        }

internal fun CourseSyllabusData.toStudyCourse(): StudyCourse =
    StudyCourse(
        id = adsceId,
        name = adDes,
        cfu = "",
        status = CourseStatus.ACTIVE,
        professor = professorsLabel ?: professors.joinToString(),
        description = listOfNotNull(obiettivi, contenuti).joinToString("\n\n"),
        semester = aaOffId.orEmpty(),
    )

internal fun TaxesData.toTaxPayments(): List<TaxPaymentData> =
    installments.map { installment ->
        TaxPaymentData(
            title = installment.title,
            date = installment.deadline,
            amount = installment.amount,
            isPaid = installment.paid,
            iuv = "",
        )
    }

internal fun List<UniversityContact>.toContacts(): List<ContactData> =
    map { contact ->
        val organization = contact.organization.orEmpty()
        ContactData(
            name = contact.displayName,
            role = organization.ifBlank { "Contatto di Ateneo" },
            initials = contact.displayName.initials(),
            email = contact.email.orEmpty(),
            phone = contact.phones.firstOrNull().orEmpty(),
            category = organization.toContactCategory(),
            department = organization,
            office = listOfNotNull(contact.building, contact.address).joinToString(" • "),
            officeHours = contact.city.orEmpty(),
        )
    }

internal fun List<AttendanceRecord>.toAttendanceData(): List<AttendanceData> =
    groupBy(AttendanceRecord::courseName).map { (course, records) ->
        val attendedHours = records.sumOf { it.durationHours ?: 0.0 }
        val totalHours = records.mapNotNull(AttendanceRecord::courseTotalHours).maxOrNull()
        val percentage =
            totalHours?.takeIf { it > 0.0 }?.let { ((attendedHours / it) * 100.0).coerceIn(0.0, 100.0).toInt() }
        AttendanceData(
            course = course,
            percentage = percentage?.let { "$it%" } ?: "—",
            count = "${records.size} presenze",
        )
    }

internal fun List<UniversityNews>.toNewsItems(): List<NewsItem> =
    map { news ->
        NewsItem(
            title = news.title,
            description = news.summary ?: news.content.orEmpty().take(160),
            fullContent = news.content ?: news.summary.orEmpty(),
            type = news.category.toNewsType(),
        )
    }

internal fun List<ConnectedDeviceData>.toDeviceInfo(): List<DeviceInfo> =
    map { device ->
        DeviceInfo(
            name = listOfNotNull(device.manufacturer, device.model).joinToString(" ").ifBlank { "Dispositivo" },
            location = listOfNotNull(device.platform, device.osVersion).joinToString(" • "),
            lastSeen = device.lastLogin,
            appVersion = device.appVersion,
            type = device.platform.toDeviceType(device.model),
            isCurrent = device.isCurrentDevice,
            id = device.token ?: device.serialCode.orEmpty(),
            revocationToken = device.token,
        )
    }

internal fun TransportData.toReservations(): List<TransportReservation> =
    bookings.map { booking ->
        TransportReservation(
            id = booking.id,
            route = availableRoutes.firstOrNull { it.code == booking.routeCode }?.label ?: routeLabel,
            date = booking.date,
            time = "",
            direction = if (booking.isReturn) TripDirection.RITORNO else TripDirection.ANDATA,
            qrCodeData = booking.ticketUrl,
            departureStop = booking.direction,
            busNumber = booking.number,
        )
    }

internal fun TransportData.toRoutes(): List<TransportRoute> =
    availableRoutes.map { route -> TransportRoute(route.label, "", "") }

internal fun TransportData.toTickets(): List<TransportTicket> =
    availableRoutes.map { route ->
        TransportTicket(
            id = route.code,
            title = route.label,
            price = "",
            validity = "Prenotazione corsa",
            type = "Navetta",
            icon = LiquidIcons.Time,
        )
    }

internal fun ExamRoundData.stableUiId(): String =
    listOfNotNull(appId, adId, adsceId).firstOrNull() ?: "$courseName|$dateTime"

internal fun String.numericGradeOrNull(): Int? {
    val clean = substringBefore('/').trim()
    return clean.filter(Char::isDigit).toIntOrNull()?.takeIf { it in 18..30 }
}

private fun String.calendarYearOrNull(): Int? =
    split('/', '-', '.').lastOrNull()?.takeIf { it.length == 4 }?.toIntOrNull()

private fun String.initials(): String =
    trim().split(' ').filter(String::isNotBlank).take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")

private fun String.toContactCategory(): ContactCategory =
    when {
        contains("segreter", ignoreCase = true) -> ContactCategory.SECRETARIAT
        contains("serviz", ignoreCase = true) || contains("ufficio", ignoreCase = true) -> ContactCategory.SERVICES
        else -> ContactCategory.TEACHERS
    }

private fun String?.toNewsType(): LiquidStatusType =
    when {
        this?.contains("scad", ignoreCase = true) == true -> LiquidStatusType.Warning
        this?.contains("success", ignoreCase = true) == true -> LiquidStatusType.Success
        else -> LiquidStatusType.Info
    }

private fun String?.toDeviceType(model: String?): DeviceType =
    when {
        this?.contains("desktop", ignoreCase = true) == true -> DeviceType.PC
        model?.contains("tablet", ignoreCase = true) == true || model?.contains("pad", ignoreCase = true) == true -> DeviceType.TABLET
        else -> DeviceType.PHONE
    }
