package com.anto426.uniapp.data

import com.anto426.unisdk.model.AttendanceRecord
import com.anto426.unisdk.model.ExamRound
import com.anto426.unisdk.model.ProfessorDashboard
import com.anto426.unisdk.model.ProfessorSection
import com.anto426.unisdk.model.UniversityContact
import com.anto426.unisdk.model.UniversityNews
import com.anto426.unisdk.transport.TransportData

/**
 * Pure, deterministic merge functions used by [PortalUniGateway] so streaming updates and
 * partial portal responses never erase previously cached history.
 */

internal fun mergeUniversityNews(
    existing: List<UniversityNews>,
    incoming: List<UniversityNews>,
): List<UniversityNews> {
    if (incoming.isEmpty()) return existing
    if (existing.isEmpty()) return incoming

    val existingByKey = LinkedHashMap<String, UniversityNews>(existing.size)
    existing.forEach { item ->
        val key = newsCacheIdentity(item)
        val prev = existingByKey[key]
        existingByKey[key] = if (prev == null) item else mergeSingleNews(prev, item)
    }

    val merged = LinkedHashMap<String, UniversityNews>(existing.size + incoming.size)
    // Incoming items are placed first so fresh news appear promptly while preserving richer fields.
    incoming.forEach { item ->
        val key = newsCacheIdentity(item)
        val prior = merged[key] ?: existingByKey[key]
        merged[key] = if (prior == null) item else mergeSingleNews(prior, item)
    }
    // Never drop previously cached news absent from the incoming stream/partial response.
    existingByKey.forEach { (key, item) ->
        if (!merged.containsKey(key)) {
            merged[key] = item
        }
    }
    return merged.values.toList()
}

private fun newsCacheIdentity(item: UniversityNews): String {
    val id = item.id?.trim()?.takeIf { it.isNotBlank() }
    if (id != null) return "id:$id"
    return "meta:${item.title.trim().lowercase()}|${item.publishedAt?.trim().orEmpty()}"
}

private fun mergeSingleNews(
    existing: UniversityNews,
    incoming: UniversityNews,
): UniversityNews = incoming.copy(
    id = incoming.id?.takeIf { it.isNotBlank() } ?: existing.id,
    title = incoming.title.takeIf { it.isNotBlank() } ?: existing.title,
    summary = pickRicherField(existing.summary, incoming.summary),
    content = pickRicherField(existing.content, incoming.content),
    publishedAt = incoming.publishedAt?.takeIf { it.isNotBlank() } ?: existing.publishedAt,
    category = incoming.category?.takeIf { it.isNotBlank() } ?: existing.category,
    sourceUrl = incoming.sourceUrl?.takeIf { it.isNotBlank() } ?: existing.sourceUrl,
    imageUrl = incoming.imageUrl?.takeIf { it.isNotBlank() } ?: existing.imageUrl,
)

internal fun mergeAttendanceHistory(
    existing: List<AttendanceRecord>,
    incoming: List<AttendanceRecord>,
): List<AttendanceRecord> {
    if (incoming.isEmpty()) return existing
    if (existing.isEmpty()) return incoming

    val existingByKey = LinkedHashMap<String, AttendanceRecord>(existing.size)
    existing.forEach { record ->
        existingByKey[attendanceCacheIdentity(record)] = record
    }

    val merged = LinkedHashMap<String, AttendanceRecord>(existing.size + incoming.size)
    incoming.forEach { record ->
        val key = attendanceCacheIdentity(record)
        val prior = merged[key] ?: existingByKey[key]
        merged[key] = if (prior == null) record else mergeSingleAttendance(prior, record)
    }
    existingByKey.forEach { (key, record) ->
        if (!merged.containsKey(key)) {
            merged[key] = record
        }
    }
    return merged.values.toList()
}

private fun attendanceCacheIdentity(record: AttendanceRecord): String {
    val id = record.id?.trim()?.takeIf { it.isNotBlank() }
    if (id != null) return "id:$id"
    return listOf(
        record.courseName.trim().lowercase(),
        record.occurredAt?.trim().orEmpty(),
        record.startTime?.trim().orEmpty(),
        record.endTime?.trim().orEmpty(),
    ).joinToString("|")
}

private fun mergeSingleAttendance(
    existing: AttendanceRecord,
    incoming: AttendanceRecord,
): AttendanceRecord = incoming.copy(
    id = incoming.id?.takeIf { it.isNotBlank() } ?: existing.id,
    teachingActivityId = incoming.teachingActivityId?.takeIf { it.isNotBlank() } ?: existing.teachingActivityId,
    courseName = incoming.courseName.takeIf { it.isNotBlank() } ?: existing.courseName,
    status = incoming.status?.takeIf { it.isNotBlank() } ?: existing.status,
    occurredAt = incoming.occurredAt?.takeIf { it.isNotBlank() } ?: existing.occurredAt,
    startTime = incoming.startTime?.takeIf { it.isNotBlank() } ?: existing.startTime,
    endTime = incoming.endTime?.takeIf { it.isNotBlank() } ?: existing.endTime,
    teacherName = incoming.teacherName?.takeIf { it.isNotBlank() } ?: existing.teacherName,
    room = incoming.room?.takeIf { it.isNotBlank() } ?: existing.room,
    note = incoming.note?.takeIf { it.isNotBlank() } ?: existing.note,
    durationHours = incoming.durationHours ?: existing.durationHours,
    courseTotalHours = incoming.courseTotalHours ?: existing.courseTotalHours,
    locationVerified = incoming.locationVerified ?: existing.locationVerified,
    locationCode = incoming.locationCode?.takeIf { it.isNotBlank() } ?: existing.locationCode,
)

internal fun mergeUniversityContacts(
    existing: List<UniversityContact>,
    incoming: List<UniversityContact>,
): List<UniversityContact> {
    if (incoming.isEmpty()) return existing
    if (existing.isEmpty()) return incoming

    val existingByKey = LinkedHashMap<String, UniversityContact>(existing.size)
    existing.forEach { contact ->
        existingByKey[contactCacheIdentity(contact)] = contact
    }

    val merged = LinkedHashMap<String, UniversityContact>(existing.size + incoming.size)
    incoming.forEach { contact ->
        val key = contactCacheIdentity(contact)
        val prior = merged[key] ?: existingByKey[key]
        merged[key] = if (prior == null) contact else mergeSingleContact(prior, contact)
    }
    existingByKey.forEach { (key, contact) ->
        if (!merged.containsKey(key)) {
            merged[key] = contact
        }
    }
    return merged.values.toList()
}

private fun contactCacheIdentity(contact: UniversityContact): String {
    val id = contact.id?.trim()?.takeIf { it.isNotBlank() }
    if (id != null) return "id:$id"
    val email = contact.email?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
    if (email != null) return "email:$email"
    return "name:${contact.displayName.trim().lowercase()}|${contact.organization?.trim()?.lowercase().orEmpty()}"
}

private fun mergeSingleContact(
    existing: UniversityContact,
    incoming: UniversityContact,
): UniversityContact = incoming.copy(
    id = incoming.id?.takeIf { it.isNotBlank() } ?: existing.id,
    firstName = incoming.firstName?.takeIf { it.isNotBlank() } ?: existing.firstName,
    lastName = incoming.lastName?.takeIf { it.isNotBlank() } ?: existing.lastName,
    displayName = incoming.displayName.takeIf { it.isNotBlank() } ?: existing.displayName,
    email = incoming.email?.takeIf { it.isNotBlank() } ?: existing.email,
    phones = (incoming.phones + existing.phones).map(String::trim).filter(String::isNotBlank).distinct(),
    organization = incoming.organization?.takeIf { it.isNotBlank() } ?: existing.organization,
    address = incoming.address?.takeIf { it.isNotBlank() } ?: existing.address,
    building = incoming.building?.takeIf { it.isNotBlank() } ?: existing.building,
    city = incoming.city?.takeIf { it.isNotBlank() } ?: existing.city,
    role = incoming.role?.takeIf { it.isNotBlank() } ?: existing.role,
    officeHours = incoming.officeHours?.takeIf { it.isNotBlank() } ?: existing.officeHours,
)

internal fun mergeProfessorDashboard(
    existing: ProfessorDashboard?,
    incoming: ProfessorDashboard,
    isFinal: Boolean,
): ProfessorDashboard {
    if (existing == null) return incoming

    fun <T> resolveSection(
        section: ProfessorSection,
        cachedItems: List<T>,
        incomingItems: List<T>,
    ): List<T> = when {
        section in incoming.unavailableSections -> cachedItems
        !isFinal && incomingItems.isEmpty() && cachedItems.isNotEmpty() -> cachedItems
        else -> incomingItems
    }

    return ProfessorDashboard(
        teachings = resolveSection(ProfessorSection.TEACHINGS, existing.teachings, incoming.teachings),
        examRounds = resolveSection(ProfessorSection.EXAM_ROUNDS, existing.examRounds, incoming.examRounds),
        theses = resolveSection(ProfessorSection.THESES, existing.theses, incoming.theses),
        reports = resolveSection(ProfessorSection.REPORTS, existing.reports, incoming.reports),
        coursesOfStudy = resolveSection(ProfessorSection.COURSES_OF_STUDY, existing.coursesOfStudy, incoming.coursesOfStudy),
        unavailableSections = incoming.unavailableSections,
    )
}

internal fun mergeTransportProgress(
    existing: TransportData?,
    incoming: TransportData,
): TransportData {
    if (existing == null) return incoming
    val mergedBookings = LinkedHashMap<String, com.anto426.unisdk.transport.TransportBooking>()
    incoming.bookings.forEach { booking -> mergedBookings[booking.id] = booking }
    existing.bookings.forEach { booking ->
        if (!mergedBookings.containsKey(booking.id)) {
            mergedBookings[booking.id] = booking
        }
    }
    val mergedRoutes = (incoming.availableRoutes + existing.availableRoutes)
        .distinctBy { it.code.lowercase() }
    val bookingsList = mergedBookings.values.toList()
    return incoming.copy(
        routeLabel = incoming.routeLabel.ifBlank { existing.routeLabel },
        routeCode = incoming.routeCode.ifBlank { existing.routeCode },
        availableRoutes = mergedRoutes,
        bookings = bookingsList,
        totalCount = bookingsList.size,
    )
}

internal fun mergeExamRoundsProgress(
    existing: List<ExamRound>?,
    incoming: List<ExamRound>,
): List<ExamRound> {
    if (existing.isNullOrEmpty()) return incoming
    if (incoming.isEmpty()) return existing

    val merged = LinkedHashMap<String, ExamRound>(incoming.size + existing.size)
    incoming.forEach { round -> merged[examRoundCacheIdentity(round)] = round }
    // Keep previously cached booked rounds visible while reservation details are still streaming in.
    existing.forEach { round ->
        val key = examRoundCacheIdentity(round)
        if (round.booked && !merged.containsKey(key)) {
            merged[key] = round
        }
    }
    return merged.values.toList()
}

private fun examRoundCacheIdentity(round: ExamRound): String {
    val structured = listOf(round.cdsId, round.adId, round.appId)
        .map { it?.trim().orEmpty() }
    if (structured.all(String::isNotBlank)) {
        return "ids:${structured.joinToString("|")}"
    }
    return "meta:${round.courseName.trim().lowercase()}|${round.dateTime.trim()}"
}

private fun pickRicherField(existing: String?, incoming: String?): String? {
    val cleanExisting = existing?.takeIf { it.isNotBlank() }
    val cleanIncoming = incoming?.takeIf { it.isNotBlank() }
    return when {
        cleanExisting == null -> cleanIncoming
        cleanIncoming == null -> cleanExisting
        cleanIncoming.length >= cleanExisting.length -> cleanIncoming
        else -> cleanExisting
    }
}
