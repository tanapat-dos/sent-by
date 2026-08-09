package com.reelshelf.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "senders",
    indices = [
        Index(value = ["display_name"]),
        Index(value = ["last_used_at"]),
        Index(value = ["is_favorite"]),
    ],
)
data class SenderEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "last_used_at") val lastUsedAt: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
)
