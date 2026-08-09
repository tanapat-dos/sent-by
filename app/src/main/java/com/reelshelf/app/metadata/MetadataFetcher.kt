package com.reelshelf.app.metadata

import com.reelshelf.app.urls.Platform

/**
 * Partial metadata is valid. Saving never waits on this result.
 */
data class MetadataResult(
    val platform: Platform,
    val title: String? = null,
    val creatorName: String? = null,
    val thumbnailUrl: String? = null,
    val failure: MetadataFailure? = null,
) {
    val isPartialSuccess: Boolean
        get() = title != null || creatorName != null || thumbnailUrl != null
}

sealed class MetadataFailure {
    data class Retryable(val message: String? = null) : MetadataFailure()

    data class Permanent(val message: String? = null) : MetadataFailure()

    data class LoginRequired(val message: String? = null) : MetadataFailure()

    data class NotSupported(val message: String? = null) : MetadataFailure()
}

interface MetadataFetcher {
    val platform: Platform

    fun fetch(canonicalUrl: String, platformContentId: String?): MetadataResult
}

/**
 * Registry used after Phase 0 decides which mechanisms are allowed per platform.
 */
class MetadataFetcherRegistry(
    private val fetchers: List<MetadataFetcher>,
) {
    fun fetch(platform: Platform, canonicalUrl: String, platformContentId: String?): MetadataResult {
        val fetcher = fetchers.firstOrNull { it.platform == platform }
        return fetcher?.fetch(canonicalUrl, platformContentId)
            ?: MetadataResult(
                platform = platform,
                failure = MetadataFailure.NotSupported("No fetcher registered for $platform"),
            )
    }
}

/**
 * Placeholder fetcher documenting that live Open Graph / oEmbed validation is pending.
 * Does not scrape login walls or violate platform ToS.
 */
class UnsupportedMetadataFetcher(
    override val platform: Platform,
    private val reason: String,
) : MetadataFetcher {
    override fun fetch(canonicalUrl: String, platformContentId: String?): MetadataResult =
        MetadataResult(
            platform = platform,
            failure = MetadataFailure.NotSupported(reason),
        )
}
