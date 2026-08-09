package com.reelshelf.app.urls

import java.net.URI

object FacebookUrlAdapter : PlatformUrlAdapter {
    override val platform: Platform = Platform.FACEBOOK

    private val hosts =
        setOf(
            "facebook.com",
            "www.facebook.com",
            "m.facebook.com",
            "fb.watch",
            "www.fb.watch",
            "fb.com",
            "www.fb.com",
        )

    override fun matches(uri: URI): Boolean {
        val host = QueryUtils.hostOf(uri)
        return host in hosts || host.endsWith(".facebook.com")
    }

    override fun canonicalize(originalUrl: String, uri: URI): CanonicalUrlResult {
        val host = QueryUtils.hostOf(uri)
        val params = QueryUtils.parseQuery(uri.rawQuery)
        val segments = QueryUtils.pathSegments(uri)

        val contentId =
            when {
                params["v"]?.all(Char::isDigit) == true -> params["v"]
                params["story_fbid"]?.isNotBlank() == true -> params["story_fbid"]
                segments.size >= 2 && segments[0].equals("reel", ignoreCase = true) -> segments[1]
                segments.size >= 2 && segments[0].equals("watch", ignoreCase = true) ->
                    params["v"] ?: segments.getOrNull(1)
                host == "fb.watch" || host == "www.fb.watch" -> segments.firstOrNull()
                else -> {
                    val videosIdx = segments.indexOfFirst { it.equals("videos", ignoreCase = true) }
                    if (videosIdx >= 0 && videosIdx + 1 < segments.size) segments[videosIdx + 1] else null
                }
            }?.takeIf { it.isNotBlank() }

        val canonical =
            when {
                contentId != null && (host == "fb.watch" || host == "www.fb.watch") ->
                    QueryUtils.rebuild(host = "fb.watch", path = "/$contentId")
                contentId != null && segments.firstOrNull().equals("reel", true) ->
                    QueryUtils.rebuild(host = "www.facebook.com", path = "/reel/$contentId")
                contentId != null && params.containsKey("v") ->
                    QueryUtils.rebuild(
                        host = "www.facebook.com",
                        path = "/watch",
                        queryParams = mapOf("v" to contentId),
                    )
                contentId != null ->
                    QueryUtils.rebuild(
                        host = "www.facebook.com",
                        path = uri.path,
                        queryParams = QueryUtils.stripTracking(params, keep = setOf("v", "story_fbid")),
                    )
                else ->
                    QueryUtils.rebuild(
                        host = if (host.endsWith("fb.watch")) "fb.watch" else "www.facebook.com",
                        path = uri.path,
                        queryParams = QueryUtils.stripTracking(params, keep = setOf("v", "story_fbid")),
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
