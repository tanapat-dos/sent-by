package com.reelshelf.app.share

/**
 * Sanitized view of an incoming share intent for Phase 0 diagnostics.
 * Never treat [textPreview] as safe to log in release — use [toLogLine] instead.
 */
data class ShareDiagnostics(
    val action: String?,
    val type: String?,
    val textPresent: Boolean,
    val textLength: Int,
    val textPreview: String?,
    val urlCountHint: Int,
    val referringPackage: String?,
    val referrerUri: String?,
    val callingPackage: String?,
    val extrasKeys: List<String>,
) {
    fun toLogLine(): String =
        buildString {
            append("ShareDiagnostics(")
            append("action=").append(action)
            append(", type=").append(type)
            append(", textPresent=").append(textPresent)
            append(", textLength=").append(textLength)
            append(", urlCountHint=").append(urlCountHint)
            append(", referringPackage=").append(referringPackage)
            append(", referrerUri=").append(referrerUri)
            append(", callingPackage=").append(callingPackage)
            append(", extrasKeys=").append(extrasKeys)
            append(")")
        }
}

object ShareDiagnosticsFactory {
    private val urlHintRegex =
        Regex("""https?://[^\s<>"']+""", RegexOption.IGNORE_CASE)

    fun from(
        action: String?,
        type: String?,
        text: String?,
        referringPackage: String?,
        referrerUri: String?,
        callingPackage: String?,
        extrasKeys: List<String>,
        previewMaxChars: Int = 280,
    ): ShareDiagnostics {
        val normalized = text?.trim().orEmpty()
        val preview =
            if (normalized.isEmpty()) {
                null
            } else if (normalized.length <= previewMaxChars) {
                normalized
            } else {
                normalized.take(previewMaxChars) + "…"
            }
        return ShareDiagnostics(
            action = action,
            type = type,
            textPresent = normalized.isNotEmpty(),
            textLength = normalized.length,
            textPreview = preview,
            urlCountHint = urlHintRegex.findAll(normalized).count(),
            referringPackage = referringPackage,
            referrerUri = referrerUri,
            callingPackage = callingPackage,
            extrasKeys = extrasKeys.sorted(),
        )
    }
}
