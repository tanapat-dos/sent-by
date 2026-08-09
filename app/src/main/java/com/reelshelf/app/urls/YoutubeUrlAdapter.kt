package com.reelshelf.app.urls

import java.net.URI

object YoutubeUrlAdapter : PlatformUrlAdapter {
    override val platform: Platform = Platform.YOUTUBE

    private val hosts =
        setOf(
            "youtube.com",
            "www.youtube.com",
            "m.youtube.com",
            "music.youtube.com",
            "youtu.be",
            "www.youtu.be",
            "youtube-nocookie.com",
            "www.youtube-nocookie.com",
        )

    override fun matches(uri: URI): Boolean {
        val host = QueryUtils.hostOf(uri)
        return host in hosts || host.endsWith(".youtube.com")
    }

    override fun canonicalize(originalUrl: String, uri: URI): CanonicalUrlResult {
        val host = QueryUtils.hostOf(uri)
        val params = QueryUtils.parseQuery(uri.rawQuery)
        val segments = QueryUtils.pathSegments(uri)

        val videoId =
            when {
                host == "youtu.be" || host == "www.youtu.be" -> segments.firstOrNull()
                segments.size >= 2 && segments[0].equals("shorts", ignoreCase = true) -> segments[1]
                segments.size >= 2 && segments[0].equals("embed", ignoreCase = true) -> segments[1]
                segments.size >= 2 && segments[0].equals("live", ignoreCase = true) -> segments[1]
                else -> params["v"]
            }?.takeIf { it.isNotBlank() }

        val canonical =
            if (videoId != null) {
                QueryUtils.rebuild(
                    host = "www.youtube.com",
                    path = "/watch",
                    queryParams = mapOf("v" to videoId),
                )
            } else {
                val kept = QueryUtils.stripTracking(params, keep = setOf("v", "list"))
                QueryUtils.rebuild(
                    host = "www.youtube.com",
                    path = uri.path,
                    queryParams = kept,
                )
            }

        return CanonicalUrlResult(
            originalUrl = originalUrl,
            canonicalUrl = canonical,
            platform = platform,
            platformContentId = videoId,
        )
    }
}
