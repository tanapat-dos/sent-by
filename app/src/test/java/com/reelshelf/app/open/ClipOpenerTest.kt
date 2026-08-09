package com.reelshelf.app.open

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ClipOpenerTest {
    @Test
    fun emptyUrl_isInvalid() {
        assertThat(ClipOpener.isOpenableHttpUrl("")).isFalse()
        assertThat(ClipOpener.isOpenableHttpUrl(null)).isFalse()
    }

    @Test
    fun nonHttp_isInvalid() {
        assertThat(ClipOpener.isOpenableHttpUrl("ftp://example.com/a")).isFalse()
    }

    @Test
    fun httpUrl_isOpenable() {
        assertThat(ClipOpener.isOpenableHttpUrl("https://youtu.be/abc")).isTrue()
        assertThat(ClipOpener.isOpenableHttpUrl("http://example.com/x")).isTrue()
    }

    @Test
    fun missingHost_isInvalid() {
        assertThat(ClipOpener.isOpenableHttpUrl("https:///nohost")).isFalse()
    }
}
