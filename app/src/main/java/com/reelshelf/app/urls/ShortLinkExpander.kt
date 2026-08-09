package com.reelshelf.app.urls

/**
 * Resolves HTTP redirects with hard bounds. Saving must never depend on success.
 */
interface RedirectClient {
    /**
     * Follow redirects for [url] up to [maxRedirects].
     * Implementations must enforce [timeoutMs] and must not throw for ordinary HTTP failures.
     */
    fun resolve(url: String, maxRedirects: Int, timeoutMs: Int): RedirectResolution
}

sealed class RedirectResolution {
    data class Success(
        val finalUrl: String,
        val redirectCount: Int,
        val hopUrls: List<String>,
    ) : RedirectResolution()

    data class Failed(
        val reason: FailureReason,
        val lastUrl: String?,
        val redirectCount: Int,
        val message: String? = null,
    ) : RedirectResolution()

    enum class FailureReason {
        TIMEOUT,
        TOO_MANY_REDIRECTS,
        HTTP_ERROR,
        NETWORK,
        UNSUPPORTED,
        LOGIN_OR_COOKIE_WALL,
    }
}

data class ExpansionResult(
    val originalUrl: String,
    /** Best URL available for further canonicalization (final or original). */
    val usableUrl: String,
    val expanded: Boolean,
    val resolution: RedirectResolution,
)

class ShortLinkExpander(
    private val client: RedirectClient,
    private val maxRedirects: Int = 5,
    private val timeoutMs: Int = 5_000,
) {
    fun expandIfNeeded(originalUrl: String): ExpansionResult {
        val trimmed = originalUrl.trim()
        if (!shouldAttemptExpansion(trimmed)) {
            return ExpansionResult(
                originalUrl = trimmed,
                usableUrl = trimmed,
                expanded = false,
                resolution =
                    RedirectResolution.Failed(
                        reason = RedirectResolution.FailureReason.UNSUPPORTED,
                        lastUrl = trimmed,
                        redirectCount = 0,
                        message = "Not a known short-link host",
                    ),
            )
        }

        return when (val resolution = client.resolve(trimmed, maxRedirects, timeoutMs)) {
            is RedirectResolution.Success ->
                ExpansionResult(
                    originalUrl = trimmed,
                    usableUrl = resolution.finalUrl,
                    expanded = resolution.finalUrl != trimmed,
                    resolution = resolution,
                )
            is RedirectResolution.Failed ->
                ExpansionResult(
                    originalUrl = trimmed,
                    usableUrl = trimmed,
                    expanded = false,
                    resolution = resolution,
                )
        }
    }

    fun canonicalizeWithOptionalExpansion(originalUrl: String): CanonicalUrlResult {
        val expansion = expandIfNeeded(originalUrl)
        val canonical = UrlCanonicalizer.canonicalize(expansion.usableUrl)
        // Always preserve the caller's original URL, even if expansion/canonicalization used another.
        return canonical.copy(originalUrl = originalUrl.trim())
    }

    companion object {
        private val shortHosts =
            setOf(
                "youtu.be",
                "www.youtu.be",
                "vt.tiktok.com",
                "vm.tiktok.com",
                "fb.watch",
                "www.fb.watch",
                "instagr.am",
                "www.instagr.am",
                "lnkd.in",
                "bit.ly",
                "t.co",
            )

        fun shouldAttemptExpansion(url: String): Boolean {
            return try {
                val host = java.net.URI(url).host?.lowercase() ?: return false
                host in shortHosts ||
                    host.startsWith("vt.") ||
                    host.startsWith("vm.") ||
                    host.endsWith(".tiktok.com") && (host.startsWith("vt.") || host.startsWith("vm."))
            } catch (_: Exception) {
                false
            }
        }
    }
}
