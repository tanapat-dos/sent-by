package com.reelshelf.app.urls

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FakeRedirectClient(
    private val script: (String) -> RedirectResolution,
) : RedirectClient {
    val seen = mutableListOf<Triple<String, Int, Int>>()

    override fun resolve(url: String, maxRedirects: Int, timeoutMs: Int): RedirectResolution {
        seen += Triple(url, maxRedirects, timeoutMs)
        return script(url)
    }
}

class ShortLinkExpanderTest {
    @Test
    fun expandIfNeeded_success_usesFinalUrlButSaveIndependently() {
        val client =
            FakeRedirectClient {
                RedirectResolution.Success(
                    finalUrl = "https://www.tiktok.com/@u/video/1234567890123456789",
                    redirectCount = 1,
                    hopUrls = listOf(it, "https://www.tiktok.com/@u/video/1234567890123456789"),
                )
            }
        val expander = ShortLinkExpander(client)
        val result = expander.expandIfNeeded("https://vt.tiktok.com/ZSabcdef/")

        assertThat(result.expanded).isTrue()
        assertThat(result.usableUrl)
            .isEqualTo("https://www.tiktok.com/@u/video/1234567890123456789")
        assertThat(result.originalUrl).isEqualTo("https://vt.tiktok.com/ZSabcdef/")
    }

    @Test
    fun expandIfNeeded_failure_returnsOriginalUsableUrl() {
        val client =
            FakeRedirectClient {
                RedirectResolution.Failed(
                    reason = RedirectResolution.FailureReason.TIMEOUT,
                    lastUrl = it,
                    redirectCount = 0,
                )
            }
        val expander = ShortLinkExpander(client)
        val result = expander.expandIfNeeded("https://vm.tiktok.com/ZMdeadbeef/")

        assertThat(result.expanded).isFalse()
        assertThat(result.usableUrl).isEqualTo("https://vm.tiktok.com/ZMdeadbeef/")
        assertThat((result.resolution as RedirectResolution.Failed).reason)
            .isEqualTo(RedirectResolution.FailureReason.TIMEOUT)
    }

    @Test
    fun expandIfNeeded_nonShortLink_skipsNetwork() {
        val client =
            FakeRedirectClient {
                error("should not be called")
            }
        val expander = ShortLinkExpander(client)
        val result = expander.expandIfNeeded("https://www.youtube.com/watch?v=abc")

        assertThat(result.expanded).isFalse()
        assertThat(client.seen).isEmpty()
        assertThat(result.usableUrl).isEqualTo("https://www.youtube.com/watch?v=abc")
    }

    @Test
    fun canonicalizeWithOptionalExpansion_preservesOriginalOnFailure() {
        val client =
            FakeRedirectClient {
                RedirectResolution.Failed(
                    reason = RedirectResolution.FailureReason.LOGIN_OR_COOKIE_WALL,
                    lastUrl = it,
                    redirectCount = 0,
                    message = "HTTP 403",
                )
            }
        val expander = ShortLinkExpander(client)
        val original = "https://vt.tiktok.com/ZSabcdef/"
        val result = expander.canonicalizeWithOptionalExpansion(original)

        assertThat(result.originalUrl).isEqualTo(original)
        assertThat(result.platform).isEqualTo(Platform.TIKTOK)
        assertThat(result.canonicalUrl).startsWith("https://vt.tiktok.com/")
    }

    @Test
    fun canonicalizeWithOptionalExpansion_usesExpandedCanonical() {
        val client =
            FakeRedirectClient {
                RedirectResolution.Success(
                    finalUrl = "https://www.tiktok.com/@creator/video/7234567890123456789?_r=1",
                    redirectCount = 2,
                    hopUrls = listOf(it),
                )
            }
        val expander = ShortLinkExpander(client)
        val result = expander.canonicalizeWithOptionalExpansion("https://vt.tiktok.com/ZSabcdef/")

        assertThat(result.originalUrl).isEqualTo("https://vt.tiktok.com/ZSabcdef/")
        assertThat(result.platformContentId).isEqualTo("7234567890123456789")
        assertThat(result.canonicalUrl)
            .isEqualTo("https://www.tiktok.com/@creator/video/7234567890123456789")
    }

    @Test
    fun tooManyRedirects_doesNotBlockUsableOriginal() {
        val client =
            FakeRedirectClient {
                RedirectResolution.Failed(
                    reason = RedirectResolution.FailureReason.TOO_MANY_REDIRECTS,
                    lastUrl = it,
                    redirectCount = 6,
                )
            }
        val result = ShortLinkExpander(client, maxRedirects = 5)
            .expandIfNeeded("https://fb.watch/abcdEFG/")
        assertThat(result.usableUrl).isEqualTo("https://fb.watch/abcdEFG/")
        assertThat(result.expanded).isFalse()
    }
}
