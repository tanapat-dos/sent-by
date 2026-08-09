package com.reelshelf.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "share_records",
    foreignKeys = [
        ForeignKey(
            entity = ClipEntity::class,
            parentColumns = ["id"],
            childColumns = ["clip_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SenderEntity::class,
            parentColumns = ["id"],
            childColumns = ["sender_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("clip_id"),
        Index("sender_id"),
        Index("reply_status"),
        Index("received_at"),
    ],
)
data class ShareRecordEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "clip_id") val clipId: String,
    @ColumnInfo(name = "sender_id") val senderId: String,
    @ColumnInfo(name = "source_app") val sourceApp: SourceApp,
    @ColumnInfo(name = "received_at") val receivedAt: Long,
    @ColumnInfo(name = "original_text") val originalText: String? = null,
    @ColumnInfo(name = "user_note") val userNote: String? = null,
    @ColumnInfo(name = "reply_status") val replyStatus: ReplyStatus = ReplyStatus.NEEDS_REPLY,
    @ColumnInfo(name = "reply_text") val replyText: String? = null,
)
