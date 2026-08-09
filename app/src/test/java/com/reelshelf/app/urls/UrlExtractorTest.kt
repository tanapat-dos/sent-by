package com.reelshelf.app.urls

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class UrlExtractorTest(
    private val name: String,
    private val input: String?,
    private val expected: List<String>,
) {
    @Test
    fun extractsExpectedUrls() {
        assertThat(UrlExtractor.extract(input))
            .containsExactlyElementsIn(expected)
            .inOrder()
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any?>> =
            listOf(
                arrayOf("null", null, emptyList<String>()),
                arrayOf("blank", "   ", emptyList<String>()),
                arrayOf(
                    "plain https",
                    "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    listOf("https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
                ),
                arrayOf(
                    "plain http",
                    "http://example.com/video",
                    listOf("http://example.com/video"),
                ),
                arrayOf(
                    "embedded in sentence",
                    "ดูคลิปนี้สิ https://vt.tiktok.com/ZSabcdef/ นะ",
                    listOf("https://vt.tiktok.com/ZSabcdef/"),
                ),
                arrayOf(
                    "trailing punctuation",
                    "link: https://youtu.be/abc123).",
                    listOf("https://youtu.be/abc123"),
                ),
                arrayOf(
                    "trailing comma and quote",
                    "\"https://instagram.com/reel/ABC123/\",",
                    listOf("https://instagram.com/reel/ABC123/"),
                ),
                arrayOf(
                    "multiple urls",
                    "a https://tiktok.com/@u/video/1 and https://youtu.be/xyz",
                    listOf(
                        "https://tiktok.com/@u/video/1",
                        "https://youtu.be/xyz",
                    ),
                ),
                arrayOf(
                    "rejects ftp",
                    "ftp://files.example.com/a.mp4 https://ok.example/x",
                    listOf("https://ok.example/x"),
                ),
                arrayOf(
                    "rejects bare domain",
                    "www.youtube.com/watch?v=1",
                    emptyList<String>(),
                ),
                arrayOf(
                    "keeps encoded query",
                    "https://example.com/watch?name=%E0%B8%AA%E0%B8%A7%E0%B8%B1%E0%B8%AA%E0%B8%94%E0%B8%B5",
                    listOf(
                        "https://example.com/watch?name=%E0%B8%AA%E0%B8%A7%E0%B8%B1%E0%B8%AA%E0%B8%94%E0%B8%B5",
                    ),
                ),
                arrayOf(
                    "malformed host rejected",
                    "https:///no-host",
                    emptyList<String>(),
                ),
                arrayOf(
                    "dedupes identical urls",
                    "https://youtu.be/a https://youtu.be/a",
                    listOf("https://youtu.be/a"),
                ),
            )
    }
}
