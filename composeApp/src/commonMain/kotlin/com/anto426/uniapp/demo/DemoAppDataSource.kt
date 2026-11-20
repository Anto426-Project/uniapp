package com.anto426.uniapp.demo

import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.UniAppPortraitSharer
import com.anto426.uniapp.library.model.*
import com.anto426.unisdk.model.*
import com.anto426.unisdk.transport.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

/** Account-local simulation: no backend/client dependency and no real university operations. */
internal class DemoAppDataSource(
    private val identity: () -> Pair<String, String?> = { DemoAccount.developerName(null) to DemoAccount.avatarFallback },
    private val imageLoader: suspend (String) -> ByteArray = { byteArrayOf() },
    private val portraitSharer: (suspend (String, ByteArray) -> ByteArray)? = null,
) : UniAppDataSource, UniAppPortraitSharer {
    private val lock = Mutex()
    private var rounds: List<ExamRound>? = null
    private var transport: TransportData? = null
    private var surveys: List<SurveyCourse>? = null
    private var demoMovements: List<LibraryMovementItem> = listOf(
        LibraryMovementItem(
            id = "demo-mov-1",
            title = "Informatica : arte e mestiere",
            detail = "Biblioteca Campobasso · In prestito fino al 28/11/2026",
            branchName = "Campobasso",
            canCancel = false,
            canRenew = true,
            actionToken = "demo-mov-token-1",
        ),
    )
    private var demoSpaceBookings: List<LibrarySpaceBooking> = listOf(
        LibrarySpaceBooking(
            id = "demo-space-1",
            title = "Sala Studio Centrale · Campobasso",
            description = "Sala Studio Centrale · Campobasso · 20/11/2026 09:00 - 13:00",
            dateLabel = "20/11/2026",
            timeLabel = "09:00 – 13:00",
            canCancel = true,
            bookingToken = "demo-space-token-1",
        ),
    )
    private var pendingDraftBookTitle: String = ""
    private suspend fun feedback() = getString(Res.string.ui_demo_action)
    override suspend fun loadCareer(forceRefresh: Boolean) = DemoCatalog.load().career
    override suspend fun loadProfessorDashboard(forceRefresh: Boolean) = ProfessorDashboard()
    override suspend fun loadStudyPlan(forceRefresh: Boolean) = DemoCatalog.load().plan
    override suspend fun loadExamRounds(forceRefresh: Boolean) = lock.withLock { rounds ?: DemoCatalog.load().exams }
    override suspend fun loadCourseSyllabus(adsceId: String, forceRefresh: Boolean) = DemoCatalog.load().syllabuses.first { it.adsceId == adsceId }
    override suspend fun bookExamRound(round: ExamRound): String {
        lock.withLock { rounds = (rounds ?: DemoCatalog.load().exams).map { if (it.appId == round.appId) it.copy(booked = true) else it } }
        return feedback()
    }
    override suspend fun cancelExamRound(round: ExamRound): String {
        lock.withLock { rounds = (rounds ?: DemoCatalog.load().exams).map { if (it.appId == round.appId) it.copy(booked = false) else it } }
        return feedback()
    }
    override suspend fun loadTaxes(forceRefresh: Boolean) = DemoCatalog.load().taxes
    override suspend fun loadStudentDetails(forceRefresh: Boolean): StudentDetails {
        val (name, avatar) = identity()
        return DemoCatalog.load().student.copy(fullName = name, photoUrl = avatar)
    }
    override suspend fun loadProfileImage(source: String, forceRefresh: Boolean): ByteArray = imageLoader(source)
    override suspend fun sharePortrait(source: String, bytes: ByteArray): ByteArray = portraitSharer?.invoke(source, bytes) ?: bytes
    override suspend fun loadConnectedDevices(forceRefresh: Boolean) = DemoCatalog.load().devices
    override suspend fun disconnectDevice(targetToken: String) = feedback()
    override suspend fun disconnectAllOtherDevices() = feedback()
    override suspend fun loadAttendanceHistory(forceRefresh: Boolean) = DemoCatalog.load().attendance
    override suspend fun registerAttendance(qrCode: String, deviceLatitude: Double?, deviceLongitude: Double?, deviceAccuracyMeters: Double?) = feedback()
    override suspend fun loadUniversityNews(forceRefresh: Boolean) = DemoCatalog.load().news
    override suspend fun loadUniversityContacts(forceRefresh: Boolean) = DemoCatalog.load().contacts
    override suspend fun loadSurveyCourses(forceRefresh: Boolean) = lock.withLock { surveys ?: DemoCatalog.load().surveys }
    override suspend fun loadSurveyFirstPage(courseId: String, tagList: String): SurveyForm {
        val fixture = DemoCatalog.load()
        return SurveyForm("demo-user", "demo-questionnaire", listOf(SurveyPage("demo-page", listOf(
            SurveyQuestion("demo-question", fixture.surveyQuestion,
                fixture.surveyAnswers.mapIndexed { i, text -> SurveyAnswerOption("demo-answer-$i", text) }, pageId = "demo-page")
        ))))
    }
    override suspend fun saveSurvey(courseId: String, request: SurveySaveRequest): String {
        lock.withLock { surveys = (surveys ?: DemoCatalog.load().surveys).map { if (it.courseId == courseId) it.copy(completed = true) else it } }
        return feedback()
    }
    override suspend fun loadSurveyCompilationStatus(adCod: String, forceRefresh: Boolean) = loadSurveyCourses(false).firstOrNull { it.adCod == adCod }?.completed == true
    override suspend fun loadTransportData(forceRefresh: Boolean) = lock.withLock { transport ?: DemoCatalog.load().transport }
    override suspend fun bookTransport(request: TransportBookingRequest): TransportActionResult = lock.withLock {
        val current = transport ?: DemoCatalog.load().transport
        val booking = TransportBooking("demo-booking-${current.bookings.size + 1}", "DEMO-${current.bookings.size + 1}",
            request.routeCode, request.date.toString(), request.direction.name, request.direction == TransportDirection.RETURN,
            "UNIAPP-DEMO-NOT-A-VALID-TICKET")
        transport = current.copy(bookings = current.bookings + booking, totalCount = current.totalCount + 1)
        TransportActionResult.Completed
    }
    override suspend fun deleteTransportBooking(bookingId: String): TransportActionResult = lock.withLock {
        val current = transport ?: DemoCatalog.load().transport
        val bookings = current.bookings.filterNot { it.id == bookingId }
        transport = current.copy(bookings = bookings, totalCount = bookings.size)
        TransportActionResult.Completed
    }

    private val demoBooks = listOf(
        LibraryCatalogBook(
            id = "MOL00065117",
            title = "Informatica : arte e mestiere",
            subtitle = "Ceri, Stefano",
            metadata = "1999",
            branches = listOf("Campobasso", "Pesche", "Termoli"),
        ),
        LibraryCatalogBook(
            id = "MOL00371948",
            title = "Informatica giuridica",
            subtitle = "Ziccardi, Giovanni",
            metadata = "2020",
            branches = listOf("Campobasso", "Pesche"),
        ),
        LibraryCatalogBook(
            id = "MOL00453666",
            title = "Principi di sistemi operativi e reti",
            subtitle = "Tanenbaum, Andrew S.",
            metadata = "2022",
            branches = listOf("Pesche"),
        ),
        LibraryCatalogBook(
            id = "MOL00511204",
            title = "Economia aziendale e bilancio",
            subtitle = "Airoldi, Giuseppe",
            metadata = "2021",
            branches = listOf("Campobasso", "Termoli"),
        ),
    )

    override suspend fun searchLibraryBooks(query: String, page: Int): LibraryCatalogPage {
        val q = query.trim().lowercase()
        val matched = demoBooks.filter {
            q.isBlank() || it.title.lowercase().contains(q) || it.subtitle.lowercase().contains(q)
        }
        return LibraryCatalogPage(query = query, books = matched, page = page, total = matched.size)
    }

    override suspend fun loadLibraryBook(id: String): LibraryBookDetail {
        val book = demoBooks.firstOrNull { it.id == id } ?: demoBooks.first()
        return LibraryBookDetail(
            id = book.id,
            title = book.title,
            author = book.subtitle,
            year = book.metadata,
            hasAvailableDocument = true,
            branches = book.branches,
            inventory = "D1 1225",
            shelfmark = "PE SL004 CERIS",
            notes = "1 v.",
            description = "${book.title} · ${book.subtitle} (${book.metadata}). Copia disponibile presso le sedi d'Ateneo.",
            actions = listOf(
                LibraryBookCopyAction(
                    actionId = "demo-act-${book.id}-MOLBU",
                    label = "Richiesta di prestito",
                    libraryCode = "MOLBU",
                    branchName = "Biblioteca di Ateneo · Campobasso",
                    movementType = "RPE",
                ),
                LibraryBookCopyAction(
                    actionId = "demo-act-${book.id}-MOLBA",
                    label = "Richiesta di prestito",
                    libraryCode = "MOLBA",
                    branchName = "Biblioteca di Ateneo · Pesche",
                    movementType = "RPE",
                ),
            ),
        )
    }

    override suspend fun prepareLibraryBookRequest(bookId: String, actionId: String): LibraryBookLoanDraft {
        val book = demoBooks.firstOrNull { it.id == bookId } ?: demoBooks.first()
        val branch = if (actionId.endsWith("MOLBA")) "Pesche" else "Campobasso"
        pendingDraftBookTitle = book.title
        return LibraryBookLoanDraft(
            draftId = "demo-draft-${demoMovements.size + 1}",
            description = "${book.title} — Richiesta di prestito ($branch)",
        )
    }

    override suspend fun confirmLibraryBookRequest(draftId: String): LibraryLoanSubmissionResult {
        lock.withLock {
            val idx = demoMovements.size + 1
            demoMovements = demoMovements + LibraryMovementItem(
                id = "demo-mov-$idx",
                title = pendingDraftBookTitle.ifBlank { "Documento biblioteca" },
                detail = "Biblioteca Campobasso · Richiesta registrata (modalità demo)",
                branchName = "Campobasso",
                canCancel = true,
                canRenew = false,
                actionToken = "demo-mov-token-$idx",
            )
        }
        return LibraryLoanSubmissionResult(description = feedback(), requiresVerification = true)
    }

    override suspend fun loadLibraryMovements(forceRefresh: Boolean): List<LibraryMovementItem> =
        lock.withLock { demoMovements }

    override suspend fun cancelLibraryMovement(movementToken: String) {
        lock.withLock {
            demoMovements = demoMovements.filterNot { it.actionToken == movementToken }
        }
    }

    override suspend fun renewLibraryMovement(movementToken: String): String = feedback()

    override suspend fun loadLibraryBookingOverview(): LibraryBookingOverview = lock.withLock {
        LibraryBookingOverview(
            contextId = "demo-ctx",
            types = listOf(
                LibraryBookingOption("study", "Posto sala studio"),
                LibraryBookingOption("pickup", "Appuntamento ritiro prestito"),
            ),
            bookings = demoSpaceBookings,
        )
    }

    override suspend fun loadLibraryResourceSelection(contextId: String, typeId: String): LibrarySpaceSelection =
        if (typeId == "study") {
            LibrarySpaceSelection(
                owners = listOf(
                    LibraryBookingOption("MOLBU", "Campobasso · Sede Centrale"),
                    LibraryBookingOption("MOLBA", "Pesche · Scienze"),
                ),
                resources = listOf(
                    LibraryBookingOption("cb-sala-1", "Sala Lettura Piano 1"),
                    LibraryBookingOption("cb-sala-pc", "Postazione multimediale"),
                ),
            )
        } else {
            LibrarySpaceSelection(
                owners = emptyList(),
                resources = listOf(LibraryBookingOption("desk-cb", "Sportello prestiti Campobasso")),
            )
        }

    override suspend fun loadLibraryResourcesForOwner(ownerId: String): List<LibraryBookingOption> =
        if (ownerId == "MOLBA") {
            listOf(LibraryBookingOption("pe-sala-1", "Sala Studio Pesche"))
        } else {
            listOf(
                LibraryBookingOption("cb-sala-1", "Sala Lettura Piano 1"),
                LibraryBookingOption("cb-sala-pc", "Postazione multimediale"),
            )
        }

    override suspend fun loadLibraryCalendar(
        contextId: String,
        resourceId: String,
        fromDate: kotlinx.datetime.LocalDate?,
        toDate: kotlinx.datetime.LocalDate?,
    ): LibrarySpaceCalendar = LibrarySpaceCalendar(
        calendarId = "demo-cal-$resourceId",
        resourceId = resourceId,
        page = 1,
        description = "Disponibilità simulata",
        slots = listOf(
            LibrarySpaceSlot(
                slotId = "slot-1",
                label = "1",
                timeLabel = "09:00 – 13:00",
                description = "21/11/2026 09:00 - 13:00 · 14 posti",
                dateLabel = "21/11/2026",
                seatsLabel = "14 / 20 posti",
                requiresDeviceAddress = false,
            ),
            LibrarySpaceSlot(
                slotId = "slot-2",
                label = "1",
                timeLabel = "14:00 – 18:00",
                description = "21/11/2026 14:00 - 18:00 · 9 posti",
                dateLabel = "21/11/2026",
                seatsLabel = "9 / 20 posti",
                requiresDeviceAddress = false,
            ),
        ),
    )

    override suspend fun loadLibraryCalendarPage(calendarId: String, page: Int): LibrarySpaceCalendar =
        loadLibraryCalendar("demo-ctx", calendarId.removePrefix("demo-cal-"), null, null).copy(page = page)

    override suspend fun bookLibrarySpace(
        calendarId: String,
        slotIds: List<String>,
        note: String,
        deviceAddresses: Map<String, String>,
    ): LibrarySpaceBookingOutcome {
        lock.withLock {
            val idx = demoSpaceBookings.size + 1
            demoSpaceBookings = demoSpaceBookings + LibrarySpaceBooking(
                id = "demo-space-$idx",
                title = "Prenotazione dimostrativa",
                description = "Prenotazione dimostrativa (${slotIds.size} fasce)",
                dateLabel = "21/11/2026",
                timeLabel = "09:00 – 13:00",
                canCancel = true,
                bookingToken = "demo-space-token-$idx",
            )
        }
        return LibrarySpaceBookingOutcome(confirmed = true, serverCode = 1)
    }

    override suspend fun cancelLibrarySpaceBooking(bookingToken: String) {
        lock.withLock {
            demoSpaceBookings = demoSpaceBookings.filterNot { it.bookingToken == bookingToken }
        }
    }

}
