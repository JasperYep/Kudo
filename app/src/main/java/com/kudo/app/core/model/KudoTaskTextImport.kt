package com.kudo.app.core.model

data class KudoTaskImportDraft(
    val title: String,
    val value: Int
)

object KudoTaskTextImport {

    private val listPrefixPattern = Regex("""^(?:[-*+]\s+|\d+[.)]\s+)""")
    private val checkboxPrefixPattern = Regex("""^\[[ xX]\]\s+""")
    private val trailingValuePattern = Regex("""^(.*?)\s+(\d+)$""")

    fun parse(raw: String): List<KudoTaskImportDraft> {
        return raw
            .lineSequence()
            .mapNotNull(::parseLine)
            .toList()
    }

    private fun parseLine(rawLine: String): KudoTaskImportDraft? {
        val cleaned = rawLine.trim()
            .replaceFirst(listPrefixPattern, "")
            .trim()
            .replaceFirst(checkboxPrefixPattern, "")
            .trim()
        if (cleaned.isBlank()) return null
        if (cleaned.all(Char::isDigit)) return null

        val match = trailingValuePattern.matchEntire(cleaned)
        val title = match?.groupValues?.get(1)?.trim() ?: cleaned
        if (title.isBlank()) return null

        val value = match?.groupValues?.get(2)?.toIntOrNull() ?: 1
        return KudoTaskImportDraft(
            title = title,
            value = value
        )
    }
}
