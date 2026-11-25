package com.anto426.uniapp.data.contract

import com.anto426.unisdk.model.AttendanceRecord
import com.anto426.unisdk.model.ConnectedDevice
import com.anto426.unisdk.model.SurveyAnswerSubmission
import com.anto426.unisdk.model.SurveyCourse
import com.anto426.unisdk.model.SurveyForm
import com.anto426.unisdk.model.SurveyPageSubmission
import com.anto426.unisdk.model.SurveySaveRequest
import com.anto426.unisdk.model.Taxes
import com.anto426.unisdk.model.UniversityContact

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
        val cleanCity = normalizeContactCity(contact.city)
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

private val leadingPostalCodeRegex = Regex("^\\d{5}\\s+")
private val trailingProvinceParenRegex = Regex("\\s*\\(\\s*[A-Za-z]{2}\\s*\\)\\s*$")
private val trailingProvinceSepRegex = Regex("\\s*[-,]\\s*[A-Za-z]{2}\\s*$")
private val contactCityWhitespaceRegex = Regex("[\\s\\u00A0]+")
private val lowercaseItalianCityParticles = setOf(
    "a", "ad", "al", "da", "dal", "di", "del", "della", "delle", "degli", "dei", "in", "su", "sul", "sulla", "con", "per",
)

internal fun normalizeContactCity(raw: String?): String {
    val collapsed = raw
        ?.replace(contactCityWhitespaceRegex, " ")
        ?.trim()
        .orEmpty()
    if (collapsed.isEmpty()) return ""

    val stripped = collapsed
        .replace(leadingPostalCodeRegex, "")
        .replace(trailingProvinceParenRegex, "")
        .replace(trailingProvinceSepRegex, "")
        .trim()
    if (stripped.isEmpty()) return ""

    val hasLower = stripped.any { it.isLowerCase() }
    val hasUpper = stripped.any { it.isUpperCase() }
    if (hasLower && hasUpper) return stripped

    return stripped.split(' ').mapIndexed { index, word ->
        val lower = word.lowercase()
        if (index > 0 && lower in lowercaseItalianCityParticles) {
            lower
        } else {
            lower.split('\'').joinToString("'") { part ->
                part.replaceFirstChar { ch -> ch.titlecase() }
            }
        }
    }.joinToString(" ")
}
