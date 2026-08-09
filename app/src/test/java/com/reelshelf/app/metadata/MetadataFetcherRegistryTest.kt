package com.reelshelf.app.metadata

import com.google.common.truth.Truth.assertThat
import com.reelshelf.app.urls.Platform
import org.junit.Test

class MetadataFetcherRegistryTest {
    @Test
    fun returnsNotSupportedWhenNoFetcher() {
        val registry = MetadataFetcherRegistry(emptyList())
        val result = registry.fetch(Platform.YOUTUBE, "https://www.youtube.com/watch?v=a", "a")
        assertThat(result.failure).isInstanceOf(MetadataFailure.NotSupported::class.java)
        assertThat(result.isPartialSuccess).isFalse()
    }

    @Test
    fun acceptsPartialSuccessWithoutTitle() {
        val fetcher =
            object : MetadataFetcher {
                override val platform = Platform.TIKTOK

                override fun fetch(canonicalUrl: String, platformContentId: String?) =
                    MetadataResult(
                        platform = Platform.TIKTOK,
                        thumbnailUrl = "https://example.com/t.jpg",
                    )
            }
        val result =
            MetadataFetcherRegistry(listOf(fetcher))
                .fetch(Platform.TIKTOK, "https://www.tiktok.com/@u/video/1", "1")
        assertThat(result.isPartialSuccess).isTrue()
        assertThat(result.failure).isNull()
    }
}
