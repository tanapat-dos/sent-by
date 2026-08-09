package com.reelshelf.app.data

import java.text.DateFormat
import java.util.Date

data class SenderHistorySummary(
    val clipCount: Int,
    val lastReceivedAt: Long?,
) {
    fun lastReceivedLabel(): String {
        val ts = lastReceivedAt ?: return "No clips yet"
        return "Last received " +
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(ts))
    }
}
