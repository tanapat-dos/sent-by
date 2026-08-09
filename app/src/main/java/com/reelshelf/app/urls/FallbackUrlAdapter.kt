package com.reelshelf.app.urls

import java.net.URI

object FallbackUrlAdapter : PlatformUrlAdapter {
    override val platform: Platform = Platform.OTHER

    override fun matches(uri: URI): Boolean = true

    override fun canonicalize(originalUrl: String, uri: URI): CanonicalUrlResult {
        val host = QueryUtils.hostOf(uri).ifBlank { "invalid.local" }
        val scheme = uri.scheme?.lowercase()?.takeIf { it == "http" || it == "https" } ?: "https"
        val params = QueryUtils.stripTracking(QueryUtils.parseQuery(uri.rawQuery))
        val canonical =
            QueryUtils.rebuild(
                scheme = scheme,
                host = host,
                path = uri.path,
                queryParams = params,
            )
        return CanonicalUrlResult(
            originalUrl = originalUrl,
            canonicalUrl = canonical,
            platform = platform,
            platformContentId = null,
        )
    }
}
