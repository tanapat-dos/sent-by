package com.reelshelf.app.urls

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class UrlCanonicalizerTest(
    @Suppress("unused") private val name: String,
    private val input: String,
    private val expectedPlatform: Platform,
    private val expectedCanonical: String,
    private val expectedContentId: String?,
) {
    @Test
    fun canonicalizes() {
        val result = UrlCanonicalizer.canonicalize(input)
        assertThat(result.originalUrl).isEqualTo(input.trim())
        assertThat(result.platform).isEqualTo(expectedPlatform)
        assertThat(result.canonicalUrl).isEqualTo(expectedCanonical)
        assertThat(result.platformContentId).isEqualTo(expectedContentId)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any?>> =
            listOf(
                arrayOf(
                    "youtube watch",
                    "https://www.youtube.com/watch?v=dQw4w9WgXcQ&utm_source=share",
                    Platform.YOUTUBE,
                    "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    "dQw4w9WgXcQ",
                ),
                arrayOf(
                    "youtube shorts mobile",
                    "https://m.youtube.com/shorts/abc123XYZ?feature=share",
                    Platform.YOUTUBE,
                    "https://www.youtube.com/watch?v=abc123XYZ",
                    "abc123XYZ",
                ),
                arrayOf(
                    "youtu.be",
                    "https://youtu.be/abc123XYZ?si=tracking",
                    Platform.YOUTUBE,
                    "https://www.youtube.com/watch?v=abc123XYZ",
                    "abc123XYZ",
                ),
                arrayOf(
                    "tiktok video",
                    "https://www.tiktok.com/@creator/video/7234567890123456789?_r=1&utm_medium=share",
                    Platform.TIKTOK,
                    "https://www.tiktok.com/@creator/video/7234567890123456789",
                    "7234567890123456789",
                ),
                arrayOf(
                    "tiktok short link unchanged path",
                    "https://vt.tiktok.com/ZSabcdef/?utm_source=x",
                    Platform.TIKTOK,
                    "https://vt.tiktok.com/ZSabcdef",
                    null,
                ),
                arrayOf(
                    "instagram reel",
                    "https://www.instagram.com/reel/AbCdEfGhIjK/?igsh=xyz",
                    Platform.INSTAGRAM,
                    "https://www.instagram.com/reel/AbCdEfGhIjK",
                    "AbCdEfGhIjK",
                ),
                arrayOf(
                    "instagram reels alias",
                    "https://instagram.com/reels/AbCdEfGhIjK/",
                    Platform.INSTAGRAM,
                    "https://www.instagram.com/reel/AbCdEfGhIjK",
                    "AbCdEfGhIjK",
                ),
                arrayOf(
                    "facebook watch",
                    "https://www.facebook.com/watch/?v=1234567890&ref=share",
                    Platform.FACEBOOK,
                    "https://www.facebook.com/watch?v=1234567890",
                    "1234567890",
                ),
                arrayOf(
                    "facebook reel",
                    "https://m.facebook.com/reel/9876543210/?mibextid=abc",
                    Platform.FACEBOOK,
                    "https://www.facebook.com/reel/9876543210",
                    "9876543210",
                ),
                arrayOf(
                    "fb.watch",
                    "https://fb.watch/abcdEFG/",
                    Platform.FACEBOOK,
                    "https://fb.watch/abcdEFG",
                    "abcdEFG",
                ),
                arrayOf(
                    "unknown strips tracking",
                    "https://Example.COM/path/video?utm_source=x&id=1",
                    Platform.OTHER,
                    "https://example.com/path/video?id=1",
                    null,
                ),
            )
    }
}

class UrlCanonicalizerContractTest {
    @Test
    fun preservesOriginalSeparatelyFromCanonical() {
        val original = "https://youtu.be/abc123?si=track"
        val result = UrlCanonicalizer.canonicalize(original)
        assertThat(result.originalUrl).isEqualTo(original)
        assertThat(result.canonicalUrl).isNotEqualTo(original)
        assertThat(result.platformContentId).isEqualTo("abc123")
    }
}
