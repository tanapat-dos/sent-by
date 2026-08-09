package com.reelshelf.app.urls

import java.net.URI

object InstagramUrlAdapter : PlatformUrlAdapter {
    override val platform: Platform = Platform.INSTAGRAM

    private val hosts =
        setOf(
            "instagram.com",
            "www.instagram.com",
            "m.instagram.com",
            "instagr.am",
            "www.instagr.am",
        )

    override fun matches(uri: URI): Boolean = QueryUtils.hostOf(uri) in hosts

    override fun canonicalize(originalUrl: String, uri: URI): CanonicalUrlResult {
        val segments = QueryUtils.pathSegments(uri)
        val kindIndex =
            segments.indexOfFirst {
                it.equals("reel", ignoreCase = true) ||
                    it.equals("reels", ignoreCase = true) ||
                    it.equals("p", ignoreCase = true) ||
                    it.equals("tv", ignoreCase = true)
            }
        val contentId =
            if (kindIndex >= 0 && kindIndex + 1 < segments.size) {
                segments[kindIndex + 1].substringBefore("?")
            } else {
                null
            }
        val kind =
            if (kindIndex >= 0) {
                when (segments[kindIndex].lowercase()) {
                    "reels" -> "reel"
                    else -> segments[kindIndex].lowercase()
                }
            } else {
                null
            }

        val canonical =
            if (kind != null && contentId != null) {
                QueryUtils.rebuild(host = "www.instagram.com", path = "/$kind/$contentId")
            } else {
                QueryUtils.rebuild(
                    host = "www.instagram.com",
                    path = uri.path,
                    queryParams = QueryUtils.stripTracking(QueryUtils.parseQuery(uri.rawQuery)),
                )
            }

        return CanonicalUrlResult(
            originalUrl = originalUrl,
            canonicalUrl = canonical,
            platform = platform,
            platformContentId = contentId,
        )
    }
}
