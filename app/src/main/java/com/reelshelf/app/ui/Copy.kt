package com.reelshelf.app.ui

object Copy {
    const val APP_NAME = "Sent By"
    const val TAGLINE = "Watch once. Reply to everyone."
    const val BLURB = "All the clips your friends send you, organized into one catch-up inbox."

    fun sentBy(names: List<String>): String {
        val cleaned = names.map { it.trim() }.filter { it.isNotEmpty() }
        return when (cleaned.size) {
            0 -> "No senders yet"
            1 -> "Sent by ${cleaned[0]}"
            2 -> "Sent by ${cleaned[0]} and ${cleaned[1]}"
            else -> {
                val head = cleaned.dropLast(1).joinToString(", ")
                "Sent by $head, and ${cleaned.last()}"
            }
        }
    }

    fun sentByCsv(csv: String?): String {
        if (csv.isNullOrBlank()) return "No senders yet"
        return sentBy(csv.split(',').map { it.trim() }.filter { it.isNotEmpty() })
    }

    fun peopleAwaitingReply(count: Int): String =
        when (count) {
            0 -> "No one awaiting reply"
            1 -> "1 person awaiting reply"
            else -> "$count people awaiting reply"
        }

    fun statusLine(
        completed: Boolean,
        watched: Boolean,
        outstandingReplies: Int,
    ): String =
        when {
            completed -> "Done"
            watched && outstandingReplies > 0 -> "Watched · ${peopleAwaitingReply(outstandingReplies)}"
            watched -> "Watched"
            outstandingReplies > 0 -> "Unwatched · ${peopleAwaitingReply(outstandingReplies)}"
            else -> "Unwatched"
        }

    fun alreadySavedAddedSender(senderName: String): String =
        "Already saved — added $senderName as another sender"

    fun savedNewAndExisting(created: Int, existing: Int): String =
        "Saved $created new, updated $existing existing."

    fun savedClips(count: Int): String =
        if (count == 1) "Saved 1 clip." else "Saved $count clips."

    const val ALL_CAUGHT_UP = "You're all caught up"
    const val EMPTY_INBOX =
        "Your catch-up inbox is empty. Share a clip from LINE or Messenger, or paste a link."
    const val COMPLETED_DETAIL = "Done — watched and all replies handled"
}
