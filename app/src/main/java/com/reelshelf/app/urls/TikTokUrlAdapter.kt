package com.reelshelf.app.urls

import java.net.URI

object TikTokUrlAdapter : PlatformUrlAdapter {
    override val platform: Platform = Platform.TIKTOK

    private val hosts =
        setOf(
            "tiktok.com",
            "www.tiktok.com",
            "m.tiktok.com",
            "vm.tiktok.com",
            "vt.tiktok.com",
        )

    override fun matches(uri: URI): Boolean {
        val host = QueryUtils.hostOf(uri)
        return host in hosts || host.endsWith(".tiktok.com")
    }

    override fun canonicalize(originalUrl: String, uri: URI): CanonicalUrlResult {
        val host = QueryUtils.hostOf(uri)
        val segments = QueryUtils.pathSegments(uri)
        val params = QueryUtils.parseQuery(uri.rawQuery)

        // /@user/video/1234567890
        val videoId =
            segments
                .indexOfFirst { it.equals("video", ignoreCase = true) }
                .takeIf { it >= 0 && it + 1 < segments.size }
                ?.let { segments[it + 1] }
                ?.substringBefore("?")
                ?.takeIf { it.all(Char::isDigit) && it.isNotEmpty() }

        val canonical =
            when {
                videoId != null -> {
                    val user =
                        segments.firstOrNull { it.startsWith("@") }
                            ?: segments.getOrNull(0)?.takeIf { it.startsWith("@") }
                    val path =
                        if (user != null && user.startsWith("@")) {
                            "/$user/video/$videoId"
                        } else {
                            "/video/$videoId"
                        }
                    QueryUtils.rebuild(host = "www.tiktok.com", path = path)
                }
                host == "vm.tiktok.com" || host == "vt.tiktok.com" -> {
                    // Short links: keep host+path without tracking params; expansion is T0.6.
                    QueryUtils.rebuild(
                        host = host,
                        path = uri.path,
                        queryParams = emptyMap(),
                    )
                }
                else -> {
                    QueryUtils.rebuild(
                        host = "www.tiktok.com",
                        path = uri.path,
                        queryParams = QueryUtils.stripTracking(params),
                    )
                }
            }

        return CanonicalUrlResult(
            originalUrl = originalUrl,
            canonicalUrl = canonical,
            platform = platform,
            platformContentId = videoId,
        )
    }
}
