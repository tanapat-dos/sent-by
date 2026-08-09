package com.reelshelf.app.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CompletionRulesTest {
    @Test
    fun unwatchedNeverCompleted() {
        assertThat(
            CompletionRules.isCompleted(
                WatchStatus.UNWATCHED,
                listOf(ReplyStatus.REPLIED),
            ),
        ).isFalse()
    }

    @Test
    fun watchedWithNeedsReplyNotCompleted() {
        assertThat(
            CompletionRules.isCompleted(
                WatchStatus.WATCHED,
                listOf(ReplyStatus.REPLIED, ReplyStatus.NEEDS_REPLY),
            ),
        ).isFalse()
    }

    @Test
    fun watchedAllSettledIsCompleted() {
        assertThat(
            CompletionRules.isCompleted(
                WatchStatus.WATCHED,
                listOf(ReplyStatus.REPLIED, ReplyStatus.NO_REPLY_NEEDED),
            ),
        ).isTrue()
    }

    @Test
    fun watchedWithNoSharesNotCompleted() {
        assertThat(CompletionRules.isCompleted(WatchStatus.WATCHED, emptyList())).isFalse()
    }

    @Test
    fun outstandingReplyCount() {
        assertThat(
            CompletionRules.outstandingReplyCount(
                listOf(ReplyStatus.NEEDS_REPLY, ReplyStatus.REPLIED, ReplyStatus.NEEDS_REPLY),
            ),
        ).isEqualTo(2)
    }
}
