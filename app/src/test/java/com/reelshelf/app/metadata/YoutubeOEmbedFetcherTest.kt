package com.reelshelf.app.metadata

import com.google.common.truth.Truth.assertThat
import com.reelshelf.app.urls.Platform
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class YoutubeOEmbedFetcherTest {
    @Test
    fun parsesSuccessfulOEmbed() {
        val http =
            SimpleHttpGet {
                SimpleHttpResponse(
                    code = 200,
                    body =
                        """{"title":"Demo","author_name":"Creator","thumbnail_url":"https://img.example/t.jpg"}""",
                )
            }
        val result =
            YoutubeOEmbedFetcher(http)
                .fetch("https://www.youtube.com/watch?v=abc", "abc")
        assertThat(result.platform).isEqualTo(Platform.YOUTUBE)
        assertThat(result.title).isEqualTo("Demo")
        assertThat(result.creatorName).isEqualTo("Creator")
        assertThat(result.thumbnailUrl).isEqualTo("https://img.example/t.jpg")
        assertThat(result.failure).isNull()
    }

    @Test
    fun mapsNotFoundToPermanent() {
        val result =
            YoutubeOEmbedFetcher(SimpleHttpGet { SimpleHttpResponse(404, null) })
                .fetch("https://www.youtube.com/watch?v=missing", "missing")
        assertThat(result.failure).isInstanceOf(MetadataFailure.Permanent::class.java)
    }
}
