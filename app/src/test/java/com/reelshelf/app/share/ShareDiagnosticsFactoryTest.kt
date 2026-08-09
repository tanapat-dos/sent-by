package com.reelshelf.app.share

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ShareDiagnosticsFactoryTest {
    @Test
    fun from_emptyText_marksMissingPayload() {
        val result =
            ShareDiagnosticsFactory.from(
                action = IntentActions.SEND,
                type = "text/plain",
                text = "   ",
                referringPackage = null,
                referrerUri = null,
                callingPackage = "jp.naver.line.android",
                extrasKeys = emptyList(),
            )

        assertThat(result.textPresent).isFalse()
        assertThat(result.textLength).isEqualTo(0)
        assertThat(result.textPreview).isNull()
        assertThat(result.urlCountHint).isEqualTo(0)
        assertThat(result.toLogLine()).doesNotContain("http")
    }

    @Test
    fun from_sharedUrl_countsHintAndTruncatesPreview() {
        val longPad = "x".repeat(300)
        val text = "check this https://www.youtube.com/watch?v=dQw4w9WgXcQ $longPad"
        val result =
            ShareDiagnosticsFactory.from(
                action = IntentActions.SEND,
                type = "text/plain",
                text = text,
                referringPackage = "android-app://com.facebook.orca",
                referrerUri = "android-app://com.facebook.orca",
                callingPackage = "com.facebook.orca",
                extrasKeys = listOf(IntentActions.EXTRA_TEXT, "android.intent.extra.STREAM"),
                previewMaxChars = 40,
            )

        assertThat(result.textPresent).isTrue()
        assertThat(result.urlCountHint).isEqualTo(1)
        assertThat(result.textPreview).endsWith("…")
        assertThat(result.textPreview!!.length).isEqualTo(41)
        assertThat(result.extrasKeys).containsExactly(
            "android.intent.extra.STREAM",
            IntentActions.EXTRA_TEXT,
        ).inOrder()
        assertThat(result.toLogLine()).contains("urlCountHint=1")
        assertThat(result.toLogLine()).doesNotContain(longPad)
    }

    @Test
    fun from_multipleUrls_countsAll() {
        val text =
            "a https://tiktok.com/t/ABC one https://youtu.be/xyz and https://instagram.com/reel/123/"
        val result =
            ShareDiagnosticsFactory.from(
                action = IntentActions.SEND,
                type = "text/plain",
                text = text,
                referringPackage = null,
                referrerUri = null,
                callingPackage = null,
                extrasKeys = emptyList(),
            )
        assertThat(result.urlCountHint).isEqualTo(3)
    }
}

/** Avoid android.Intent dependency in JVM unit tests. */
private object IntentActions {
    const val SEND = "android.intent.action.SEND"
    const val EXTRA_TEXT = "android.intent.extra.TEXT"
}
