package com.anto426.uniapp.didactics.presentation

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import com.anto426.uniapp.model.didactics.ThesisData
import com.anto426.unisdk.backend.model.ProfessorContentItem

fun extractThesisData(item: ProfessorContentItem): ThesisData {
    val fields =
        item.fields.map { it.label to it.value }.ifEmpty { item.detail.toAcademicDetailFields() }
    val map = fields.associate { (k, v) -> k.lowercase().trim() to v.trim() }

    val thesisTitle =
        map["titolo tesi"]
            ?: map["titolo"]
            ?: map["argomento"]
            ?: map["elaborato"]
            ?: map["titolo elaborato"]

    val relatore =
        map["relatore"]
            ?: map["docente relatore"]
            ?: map["professore relatore"]
            ?: map["relatore tesi"]

    val correlatore =
        map["correlatore"]
            ?: map["docente correlatore"]
            ?: map["secondo relatore"]

    val matricola =
        map["matricola"]
            ?: map["matr."]
            ?: map["matr"]
            ?: item.code

    val cds =
        map["corso di laurea"]
            ?: map["cds"]
            ?: map["corso"]
            ?: map["percorso"]
            ?: item.subtitle

    val date =
        map["data discussione"]
            ?: map["discussione"]
            ?: map["data tesi"]
            ?: map["data"]
            ?: item.date

    val sede =
        map["sede"]
            ?: map["aula"]
            ?: map["luogo"]

    val voto =
        map["voto"]
            ?: map["voto finale"]
            ?: map["punteggio"]
            ?: map["esito"]

    val sessione =
        map["sessione"]
            ?: map["sessione di laurea"]
            ?: map["appello"]

    val standardKeys =
        setOf(
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

    val extraFields = fields.filterNot { (k, _) -> standardKeys.contains(k.lowercase().trim()) }

    return ThesisData(
        candidateName = item.title,
        thesisTitle = thesisTitle,
        relatore = relatore,
        correlatore = correlatore,
        matricola = matricola,
        cds = cds,
        date = date,
        sede = sede,
        voto = voto,
        sessione = sessione,
        extraFields = extraFields,
    )
}

suspend fun ProfessorContentItem.orderedAcademicDetailFields(
    section: AcademicSection,
): List<Pair<String, String>> {
    val parsed =
        fields.map { it.label to it.value }.ifEmpty { detail.toAcademicDetailFields() }
    if (section != AcademicSection.Theses) return parsed
    val priorities =
        listOf("data", "discussione", "cognome", "nome", "matricola", "percorso", "cds", "argomento", "titolo tesi", "titolo", "voto")
    return parsed
        .map { (label, value) ->
            val normalizedLabel =
                when (label.lowercase()) {
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

fun String?.toAcademicDetailFields(): List<Pair<String, String>> =
    orEmpty()
        .lineSequence()
        .mapNotNull { line ->
            val separator = line.indexOf(':')
            if (separator <= 0 || separator == line.lastIndex) return@mapNotNull null
            val label = line.substring(0, separator).trim()
            val value = line.substring(separator + 1).trim()
            if (label.isBlank() || value.isBlank()) null else label to value
        }
        .toList()
