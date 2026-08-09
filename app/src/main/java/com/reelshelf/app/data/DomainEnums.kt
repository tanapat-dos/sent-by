package com.reelshelf.app.data

enum class WatchStatus {
    UNWATCHED,
    WATCHED,
}

enum class ReplyStatus {
    NEEDS_REPLY,
    REPLIED,
    NO_REPLY_NEEDED,
}

enum class SourceApp {
    LINE,
    MESSENGER,
    OTHER,
}

object CompletionRules {
    fun isCompleted(watchStatus: WatchStatus, replyStatuses: Collection<ReplyStatus>): Boolean {
        if (watchStatus != WatchStatus.WATCHED) return false
        if (replyStatuses.isEmpty()) return false
        return replyStatuses.none { it == ReplyStatus.NEEDS_REPLY }
    }

    fun outstandingReplyCount(replyStatuses: Collection<ReplyStatus>): Int =
        replyStatuses.count { it == ReplyStatus.NEEDS_REPLY }
}
