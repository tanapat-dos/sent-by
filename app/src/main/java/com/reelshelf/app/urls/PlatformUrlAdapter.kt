package com.reelshelf.app.urls

enum class Platform {
    YOUTUBE,
    TIKTOK,
    INSTAGRAM,
    FACEBOOK,
    OTHER,
}

data class CanonicalUrlResult(
    val originalUrl: String,
    val canonicalUrl: String,
    val platform: Platform,
    val platformContentId: String?,
)

interface PlatformUrlAdapter {
    val platform: Platform

    fun matches(uri: java.net.URI): Boolean

    fun canonicalize(originalUrl: String, uri: java.net.URI): CanonicalUrlResult
}

object UrlCanonicalizer {
    private val adapters: List<PlatformUrlAdapter> =
        listOf(
            YoutubeUrlAdapter,
            TikTokUrlAdapter,
            InstagramUrlAdapter,
            FacebookUrlAdapter,
            FallbackUrlAdapter,
        )

    fun canonicalize(originalUrl: String): CanonicalUrlResult {
        val trimmed = originalUrl.trim()
        val uri =
            try {
                java.net.URI(trimmed)
            } catch (_: Exception) {
                return FallbackUrlAdapter.canonicalize(trimmed, java.net.URI("https://invalid.local/"))
            }
        val adapter = adapters.first { it.matches(uri) }
        return adapter.canonicalize(trimmed, uri)
    }
}
