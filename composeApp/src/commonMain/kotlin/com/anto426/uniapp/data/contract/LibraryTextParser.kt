package com.anto426.uniapp.data.contract

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
