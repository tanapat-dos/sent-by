package com.reelshelf.app.urls

/**
 * Pure-Kotlin extractor for http(s) URLs in shared or pasted text.
 * No Android framework dependency.
 */
object UrlExtractor {
    private val candidateRegex =
        Regex(
            """(?<url>https?://[^\s<>"'\\]+)""",
            setOf(RegexOption.IGNORE_CASE),
        )

    private val trailingJunk = Regex("""[)\]}>,.;:!?'"”’]+$""")

    fun extract(input: String?): List<String> {
        if (input.isNullOrBlank()) return emptyList()

        val found = LinkedHashSet<String>()
        for (match in candidateRegex.findAll(input)) {
            val raw = match.groups["url"]?.value ?: continue
            normalizeCandidate(raw)?.let { found.add(it) }
        }
        return found.toList()
    }

    private fun normalizeCandidate(raw: String): String? {
        var candidate = raw.trim()
        // Strip paired wrappers already excluded by regex; still trim trailing punctuation.
        while (candidate.isNotEmpty() && trailingJunk.containsMatchIn(candidate)) {
            val next = candidate.replace(trailingJunk, "")
            if (next == candidate) break
            // Keep trailing characters that are part of a balanced path (rare); only strip
            // punctuation that is commonly glued by messengers.
            if (next.endsWith("%") || next.contains("%")) {
                // Avoid eating percent-encoding; trailingJunk does not include %.
            }
            candidate = next
        }

        if (!candidate.startsWith("http://", ignoreCase = true) &&
            !candidate.startsWith("https://", ignoreCase = true)
        ) {
            return null
        }

        return try {
            val uri = java.net.URI(candidate)
            val scheme = uri.scheme?.lowercase() ?: return null
            if (scheme != "http" && scheme != "https") return null
            if (uri.host.isNullOrBlank()) return null
            candidate
        } catch (_: Exception) {
            null
        }
    }
}
