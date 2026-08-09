package com.reelshelf.app.urls

import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

internal object QueryUtils {
    private val trackingParams =
        setOf(
            "utm_source",
            "utm_medium",
            "utm_campaign",
            "utm_term",
            "utm_content",
            "utm_id",
            "fbclid",
            "gclid",
            "igshid",
            "igsh",
            "si",
            "feature",
            "pp",
            "ref",
            "ref_url",
            "mibextid",
            "rdid",
            "share_app_id",
            "share_id",
            "_r",
            "_t",
        )

    fun parseQuery(rawQuery: String?): Map<String, String> {
        if (rawQuery.isNullOrBlank()) return emptyMap()
        return rawQuery
            .split("&")
            .mapNotNull { part ->
                if (part.isBlank()) return@mapNotNull null
                val idx = part.indexOf('=')
                if (idx < 0) {
                    decode(part) to ""
                } else {
                    decode(part.substring(0, idx)) to decode(part.substring(idx + 1))
                }
            }.toMap()
    }

    fun stripTracking(params: Map<String, String>, keep: Set<String> = emptySet()): Map<String, String> =
        params.filterKeys { key ->
            key in keep || key.lowercase() !in trackingParams
        }

    fun buildQuery(params: Map<String, String>): String? {
        if (params.isEmpty()) return null
        return params.entries.joinToString("&") { (k, v) ->
            if (v.isEmpty()) encode(k) else "${encode(k)}=${encode(v)}"
        }
    }

    fun rebuild(
        scheme: String = "https",
        host: String,
        path: String?,
        queryParams: Map<String, String> = emptyMap(),
        fragment: String? = null,
    ): String {
        val normalizedPath =
            when {
                path.isNullOrBlank() -> ""
                path.startsWith("/") -> path
                else -> "/$path"
            }
        val query = buildQuery(queryParams)
        return buildString {
            append(scheme)
            append("://")
            append(host.lowercase())
            append(normalizedPath.trimEnd('/').ifEmpty { "" })
            // Preserve trailing slash only for root
            if (normalizedPath == "/") append("/")
            if (query != null) {
                append("?")
                append(query)
            }
            if (!fragment.isNullOrBlank()) {
                append("#")
                append(fragment)
            }
        }
    }

    fun hostOf(uri: URI): String = uri.host?.lowercase().orEmpty()

    fun pathSegments(uri: URI): List<String> =
        uri.path
            ?.trim('/')
            ?.takeIf { it.isNotEmpty() }
            ?.split("/")
            ?.filter { it.isNotEmpty() }
            .orEmpty()

    private fun decode(value: String): String =
        URLDecoder.decode(value, StandardCharsets.UTF_8)

    private fun encode(value: String): String =
        java.net.URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20")
}
