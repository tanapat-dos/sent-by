package com.reelshelf.app.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DisplayTitleTest {
    @Test
    fun prefersTitle() {
        assertThat(displayTitle("Hello", "https://youtu.be/a")).isEqualTo("Hello")
    }

    @Test
    fun fallsBackToHostLabel() {
        assertThat(displayTitle(null, "https://www.youtube.com/watch?v=a"))
            .isEqualTo("youtube.com video")
    }
}
