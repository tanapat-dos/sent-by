package com.reelshelf.app.open

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Opens a saved URL via ACTION_VIEW. Returns a structured outcome for diagnostics.
 */
class ClipOpener(
    private val startActivity: (Intent) -> Unit,
) {
    constructor(context: Context) : this({ intent -> context.startActivity(intent) })

    fun open(url: String): OpenOutcome {
        if (!isOpenableHttpUrl(url)) return OpenOutcome.InvalidUrl
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse(url.trim())).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        return try {
            startActivity(intent)
            OpenOutcome.Started
        } catch (_: ActivityNotFoundException) {
            OpenOutcome.NoHandler
        } catch (_: Exception) {
            OpenOutcome.Failed
        }
    }

    companion object {
        fun isOpenableHttpUrl(url: String?): Boolean {
            val trimmed = url?.trim().orEmpty()
            if (trimmed.isEmpty()) return false
            return try {
                val uri = java.net.URI(trimmed)
                val scheme = uri.scheme?.lowercase()
                (scheme == "http" || scheme == "https") && !uri.host.isNullOrBlank()
            } catch (_: Exception) {
                false
            }
        }
    }
}

enum class OpenOutcome {
    Started,
    NoHandler,
    InvalidUrl,
    Failed,
}
