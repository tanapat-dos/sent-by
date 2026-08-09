package com.reelshelf.app.ui

enum class AppLanguage {
    EN,
    TH,
}

data class HelpStep(val title: String, val body: String)

data class UiStrings(
    val appName: String,
    val tagline: String,
    val blurb: String,
    val inbox: String,
    val senders: String,
    val done: String,
    val howToUse: String,
    val gotIt: String,
    val helpTitle: String,
    val helpOverview: String,
    val helpSteps: List<HelpStep>,
    val helpFoot: String,
    val tipInbox: String,
    val tipSenders: String,
    val tipDone: String,
    val tipPaste: String,
    val tipHelp: String,
    val tipPrivacy: String,
    val searchPlaceholder: String,
    val catchUpUnwatched: String,
    val catchUpNeedsReply: String,
    val unwatched: String,
    val watched: String,
    val needsReply: String,
    val allCategories: String,
    val emptyInbox: String,
    val allCaughtUp: String,
    val noMatch: String,
    val completedDetail: String,
    val noSendersYet: String,
    val language: String,
    val langEn: String,
    val langTh: String,
    val privacyTitle: String,
    val privacyBody1: String,
) {
    fun peopleAwaitingReply(count: Int): String =
        when {
            count == 0 ->
                if (this === En) "No one awaiting reply" else "ไม่มีใครรอการตอบ"
            count == 1 ->
                if (this === En) "1 person awaiting reply" else "มี 1 คนรอการตอบ"
            this === En -> "$count people awaiting reply"
            else -> "มี $count คนรอการตอบ"
        }

    fun sentBy(names: List<String>): String {
        val cleaned = names.map { it.trim() }.filter { it.isNotEmpty() }
        return when {
            cleaned.isEmpty() -> noSendersYet
            cleaned.size == 1 -> if (this === En) "Sent by ${cleaned[0]}" else "ส่งโดย ${cleaned[0]}"
            cleaned.size == 2 ->
                if (this === En) {
                    "Sent by ${cleaned[0]} and ${cleaned[1]}"
                } else {
                    "ส่งโดย ${cleaned[0]} และ ${cleaned[1]}"
                }
            else -> {
                val head = cleaned.dropLast(1).joinToString(", ")
                if (this === En) {
                    "Sent by $head, and ${cleaned.last()}"
                } else {
                    "ส่งโดย $head และ ${cleaned.last()}"
                }
            }
        }
    }

    fun sentByCsv(csv: String?): String {
        if (csv.isNullOrBlank()) return noSendersYet
        return sentBy(csv.split(',').map { it.trim() }.filter { it.isNotEmpty() })
    }

    fun statusLine(completed: Boolean, watched: Boolean, outstandingReplies: Int): String =
        when {
            completed -> done
            watched && outstandingReplies > 0 ->
                "${this.watched} · ${peopleAwaitingReply(outstandingReplies)}"
            watched -> this.watched
            outstandingReplies > 0 ->
                "$unwatched · ${peopleAwaitingReply(outstandingReplies)}"
            else -> unwatched
        }

    fun alreadySavedAddedSender(senderName: String): String =
        if (this === En) {
            "Already saved — added $senderName as another sender"
        } else {
            "บันทึกไว้แล้ว — เพิ่ม $senderName เป็นผู้ส่งอีกคน"
        }

    fun savedNewAndExisting(created: Int, existing: Int): String =
        if (this === En) {
            "Saved $created new, updated $existing existing."
        } else {
            "บันทึกใหม่ $created อัปเดตของเดิม $existing"
        }

    fun savedClips(count: Int): String =
        if (this === En) {
            if (count == 1) "Saved 1 clip." else "Saved $count clips."
        } else {
            if (count == 1) "บันทึก 1 คลิปแล้ว" else "บันทึก $count คลิปแล้ว"
        }

    companion object {
        fun forLanguage(language: AppLanguage): UiStrings =
            when (language) {
                AppLanguage.EN -> En
                AppLanguage.TH -> Th
            }
    }
}

private val En =
    UiStrings(
        appName = "Sent By",
        tagline = "Watch once. Reply to everyone.",
        blurb = "All the clips your friends send you, organized into one catch-up inbox.",
        inbox = "Inbox",
        senders = "Senders",
        done = "Done",
        howToUse = "How to use",
        gotIt = "Got it",
        helpTitle = "How Sent By works",
        helpOverview =
            "Sent By is a catch-up inbox for short video links friends send you. Save each clip once, watch it, then reply to everyone who shared it — without digging through chat history.",
        helpSteps =
            listOf(
                HelpStep(
                    "Save a clip",
                    "Share from LINE/Messenger into Sent By, or paste a link, and pick who sent it. If the same URL arrives again from someone else, we add them as another sender.",
                ),
                HelpStep(
                    "Watch once",
                    "Open the clip from your inbox. Opening marks it watched so you do not rewatch the same video for every person.",
                ),
                HelpStep(
                    "Reply to everyone",
                    "On the clip, reply per sender. Copy a quick reply, then paste it into the chat app yourself. Mark replied when you are done.",
                ),
                HelpStep(
                    "Stay caught up",
                    "Use Inbox for open items, Senders to manage people (and favorites), and Done for clips you have watched and finished replying to.",
                ),
            ),
        helpFoot = "Open this anytime from How to use. Your data stays on this device.",
        tipInbox = "Open clips waiting to be watched or replied to",
        tipSenders = "People who send you clips — rename, favorite, or merge duplicates",
        tipDone = "Clips you already watched and finished replying to",
        tipPaste = "Add a video link and choose who sent it",
        tipHelp = "How Sent By works",
        tipPrivacy = "What we store on this device",
        searchPlaceholder = "Search sender, platform, title, URL",
        catchUpUnwatched = "Catch up unwatched",
        catchUpNeedsReply = "Catch up needs reply",
        unwatched = "Unwatched",
        watched = "Watched",
        needsReply = "Needs reply",
        allCategories = "All categories",
        emptyInbox =
            "Your catch-up inbox is empty. Share a clip from LINE or Messenger, or paste a link.",
        allCaughtUp = "You're all caught up",
        noMatch = "No clips match your search.",
        completedDetail = "Done — watched and all replies handled",
        noSendersYet = "No senders yet",
        language = "Language",
        langEn = "EN",
        langTh = "ไทย",
        privacyTitle = "Privacy & data",
        privacyBody1 = "Sent By stores only what you explicitly share into the app.",
    )

private val Th =
    UiStrings(
        appName = "Sent By",
        tagline = "ดูครั้งเดียว ตอบทุกคน",
        blurb = "คลิปทั้งหมดที่เพื่อนส่งมา จัดไว้ในกล่องตามทันที่เดียว",
        inbox = "กล่องรับ",
        senders = "ผู้ส่ง",
        done = "เสร็จแล้ว",
        howToUse = "วิธีใช้",
        gotIt = "เข้าใจแล้ว",
        helpTitle = "Sent By ทำงานยังไง",
        helpOverview =
            "Sent By คือกล่องตามทันสำหรับลิงก์วิดีโอสั้นที่เพื่อนส่งมา บันทึกคลิปครั้งเดียว ดูแล้ว ค่อยตอบทุกคนที่ส่งมา — ไม่ต้องคุ้ยแชทเก่า",
        helpSteps =
            listOf(
                HelpStep(
                    "บันทึกคลิป",
                    "แชร์จาก LINE/Messenger เข้า Sent By หรือวางลิงก์ แล้วเลือกว่าใครส่งมา ถ้า URL เดิมมาจากคนอื่นอีก จะเพิ่มเป็นผู้ส่งอีกคน",
                ),
                HelpStep(
                    "ดูครั้งเดียว",
                    "เปิดคลิปจากกล่องรับ การเปิดจะทำเครื่องหมายว่าดูแล้ว เพื่อไม่ต้องดูซ้ำสำหรับทุกคน",
                ),
                HelpStep(
                    "ตอบทุกคน",
                    "ในหน้าคลิป ตอบแยกตามผู้ส่ง คัดลอกข้อความตอบด่วน แล้ววางในแอปแชทเอง เมื่อตอบแล้วให้ทำเครื่องหมายว่าตอบแล้ว",
                ),
                HelpStep(
                    "ตามทันอยู่เสมอ",
                    "ใช้กล่องรับสำหรับรายการที่ยังไม่จบ ผู้ส่งสำหรับจัดการคน (และรายการโปรด) และเสร็จแล้วสำหรับคลิปที่ดูและตอบครบแล้ว",
                ),
            ),
        helpFoot = "เปิดคู่มือนี้ได้ทุกเมื่อจากปุ่มวิธีใช้ ข้อมูลอยู่บนอุปกรณ์นี้",
        tipInbox = "คลิปที่รอชมหรือรอตอบ",
        tipSenders = "คนที่ส่งคลิปมา — เปลี่ยนชื่อ ติดดาว หรือรวมชื่อซ้ำ",
        tipDone = "คลิปที่ดูและตอบครบแล้ว",
        tipPaste = "เพิ่มลิงก์วิดีโอและเลือกผู้ส่ง",
        tipHelp = "Sent By ทำงานยังไง",
        tipPrivacy = "เราเก็บอะไรไว้บนอุปกรณ์นี้",
        searchPlaceholder = "ค้นหาผู้ส่ง แพลตฟอร์ม ชื่อ หรือ URL",
        catchUpUnwatched = "ตามทันที่ยังไม่ดู",
        catchUpNeedsReply = "ตามทันที่รอตอบ",
        unwatched = "ยังไม่ดู",
        watched = "ดูแล้ว",
        needsReply = "รอตอบ",
        allCategories = "ทุกหมวดหมู่",
        emptyInbox = "กล่องตามทันว่างอยู่ แชร์คลิปจาก LINE หรือ Messenger หรือวางลิงก์",
        allCaughtUp = "ตามทันหมดแล้ว",
        noMatch = "ไม่พบคลิปที่ตรงกับการค้นหา",
        completedDetail = "เสร็จแล้ว — ดูแล้วและตอบครบทุกคน",
        noSendersYet = "ยังไม่มีผู้ส่ง",
        language = "ภาษา",
        langEn = "EN",
        langTh = "ไทย",
        privacyTitle = "ความเป็นส่วนตัวและข้อมูล",
        privacyBody1 = "Sent By เก็บเฉพาะสิ่งที่คุณแชร์เข้าแอปโดยตรง",
    )

/** Compatibility facade used by ViewModels that do not have CompositionLocal. */
object Copy {
    @Volatile
    var language: AppLanguage = AppLanguage.EN

    private val s: UiStrings
        get() = UiStrings.forLanguage(language)

    val APP_NAME get() = s.appName
    val TAGLINE get() = s.tagline
    val BLURB get() = s.blurb
    val ALL_CAUGHT_UP get() = s.allCaughtUp
    val EMPTY_INBOX get() = s.emptyInbox
    val COMPLETED_DETAIL get() = s.completedDetail
    val HELP_OVERVIEW get() = s.helpOverview
    val HELP_STEPS get() = s.helpSteps.map { it.title to it.body }

    fun sentBy(names: List<String>) = s.sentBy(names)

    fun sentByCsv(csv: String?) = s.sentByCsv(csv)

    fun peopleAwaitingReply(count: Int) = s.peopleAwaitingReply(count)

    fun statusLine(completed: Boolean, watched: Boolean, outstandingReplies: Int) =
        s.statusLine(completed, watched, outstandingReplies)

    fun alreadySavedAddedSender(senderName: String) = s.alreadySavedAddedSender(senderName)

    fun savedNewAndExisting(created: Int, existing: Int) = s.savedNewAndExisting(created, existing)

    fun savedClips(count: Int) = s.savedClips(count)

    object NavTips {
        val INBOX get() = Copy.s.tipInbox
        val SENDERS get() = Copy.s.tipSenders
        val DONE get() = Copy.s.tipDone
        val PASTE get() = Copy.s.tipPaste
        val HELP get() = Copy.s.tipHelp
        val PRIVACY get() = Copy.s.tipPrivacy
    }
}
