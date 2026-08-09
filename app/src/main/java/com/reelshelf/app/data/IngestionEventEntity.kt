package com.reelshelf.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ingestion_events",
    indices = [
        Index(value = ["fingerprint"], unique = true),
        Index(value = ["created_at"]),
    ],
)
data class IngestionEventEntity(
    @PrimaryKey val id: String,
    val fingerprint: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
