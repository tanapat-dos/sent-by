package com.reelshelf.app.reply

import android.content.Context

/**
 * Local-only recent custom reply texts for quick chips.
 */
class RecentReplyStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun list(): List<String> =
        prefs.getString(KEY, null)
            ?.split(SEP)
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            .orEmpty()

    fun remember(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val next =
            (listOf(trimmed) + list().filterNot { it.equals(trimmed, ignoreCase = true) })
                .take(MAX)
        prefs.edit().putString(KEY, next.joinToString(SEP)).apply()
    }

    companion object {
        private const val PREFS = "reelshelf_replies"
        private const val KEY = "recent_custom"
        private const val SEP = "\u001f"
        private const val MAX = 6
    }
}
