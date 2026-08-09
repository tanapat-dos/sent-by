package com.reelshelf.app.ui

object Routes {
    const val INBOX = "inbox"
    const val PASTE = "paste"
    const val PRIVACY = "privacy"
    const val SENDERS = "senders"
    const val CATEGORIES = "categories"
    const val CATCH_UP = "catchup/{mode}"
    const val CLIP = "clip/{clipId}"
    const val SENDER = "sender/{senderId}"
    const val QUICK_SAVE = "quick_save"

    fun clip(clipId: String) = "clip/$clipId"

    fun sender(senderId: String) = "sender/$senderId"

    fun catchUp(mode: String) = "catchup/$mode"
}
