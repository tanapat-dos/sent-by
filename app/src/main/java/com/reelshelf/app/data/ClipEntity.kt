package com.reelshelf.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.reelshelf.app.urls.Platform

@Entity(
    tableName = "clips",
    indices = [
        Index(value = ["canonical_url"], unique = true),
        Index(value = ["platform", "platform_content_id"], unique = true),
        Index(value = ["watch_status", "last_received_at"]),
    ],
)
data class ClipEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "original_url") val originalUrl: String,
    @ColumnInfo(name = "canonical_url") val canonicalUrl: String,
    val platform: Platform,
    @ColumnInfo(name = "platform_content_id") val platformContentId: String?,
    val title: String? = null,
    @ColumnInfo(name = "creator_name") val creatorName: String? = null,
    @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String? = null,
    @ColumnInfo(name = "watch_status") val watchStatus: WatchStatus = WatchStatus.UNWATCHED,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "last_received_at") val lastReceivedAt: Long,
    @ColumnInfo(name = "metadata_status") val metadataStatus: String = MetadataWorkStatus.PENDING,
)

object MetadataWorkStatus {
    const val PENDING = "PENDING"
    const val SUCCESS = "SUCCESS"
    const val PARTIAL = "PARTIAL"
    const val RETRYABLE = "RETRYABLE"
    const val PERMANENT = "PERMANENT"
    const val LOGIN_REQUIRED = "LOGIN_REQUIRED"
    const val NOT_SUPPORTED = "NOT_SUPPORTED"
}
