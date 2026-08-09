package com.reelshelf.app.data

import androidx.room.withTransaction
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SenderRepository(
    private val db: ReelShelfDatabase,
) {
    private val senders get() = db.senderDao()
    private val shares get() = db.shareDao()

    fun observeRecent(): Flow<List<SenderEntity>> = senders.observeRecent()

    fun observeClips(senderId: String): Flow<List<ClipEntity>> =
        db.clipDao().observeClipsForSender(senderId)

    fun observeHistorySummary(senderId: String): Flow<SenderHistorySummary> =
        observeClips(senderId).map { clips ->
            SenderHistorySummary(
                clipCount = clips.size,
                lastReceivedAt = clips.maxOfOrNull { it.lastReceivedAt },
            )
        }

    fun observeSender(senderId: String): Flow<SenderEntity?> = senders.observeById(senderId)

    suspend fun create(displayName: String, now: Long = System.currentTimeMillis()): SenderEntity {
        val trimmed = displayName.trim()
        require(trimmed.isNotEmpty()) { "Sender name required" }
        val sender =
            SenderEntity(
                id = UUID.randomUUID().toString(),
                displayName = trimmed,
                lastUsedAt = now,
                createdAt = now,
            )
        senders.insert(sender)
        return sender
    }

    suspend fun touch(senderId: String, now: Long = System.currentTimeMillis()) {
        val existing = senders.getById(senderId) ?: return
        senders.update(existing.copy(lastUsedAt = now))
    }

    suspend fun rename(senderId: String, displayName: String) {
        val trimmed = displayName.trim()
        require(trimmed.isNotEmpty()) { "Sender name required" }
        val existing = senders.getById(senderId) ?: return
        senders.update(existing.copy(displayName = trimmed))
    }

    suspend fun setFavorite(senderId: String, favorite: Boolean) {
        val existing = senders.getById(senderId) ?: return
        senders.update(existing.copy(isFavorite = favorite))
    }

    suspend fun merge(fromSenderId: String, intoSenderId: String) {
        require(fromSenderId != intoSenderId)
        db.withTransaction {
            requireNotNull(senders.getById(fromSenderId))
            requireNotNull(senders.getById(intoSenderId))
            shares.reassignSender(fromSenderId, intoSenderId)
            senders.deleteById(fromSenderId)
        }
        touch(intoSenderId)
    }
}

class ClipRepository(
    private val db: ReelShelfDatabase,
) {
    private val clips get() = db.clipDao()
    private val shares get() = db.shareDao()

    fun observeInbox(
        filter: InboxFilter,
        query: String,
        categoryId: String? = null,
    ): Flow<List<ClipInboxRow>> =
        clips.observeInbox(filter.name, query.trim().lowercase(), categoryId.orEmpty())

    fun observeCategories(): Flow<List<CategoryEntity>> = db.categoryDao().observeAll()

    fun observeCategoriesForClip(clipId: String): Flow<List<CategoryEntity>> =
        db.categoryDao().observeForClip(clipId)

    suspend fun createCategory(name: String, now: Long = System.currentTimeMillis()): CategoryEntity {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Category name required" }
        val category =
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                name = trimmed,
                createdAt = now,
                updatedAt = now,
            )
        db.categoryDao().insert(category)
        return category
    }

    suspend fun renameCategory(categoryId: String, name: String) {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Category name required" }
        val existing = db.categoryDao().getById(categoryId) ?: return
        db.categoryDao().update(
            existing.copy(name = trimmed, updatedAt = System.currentTimeMillis()),
        )
    }

    suspend fun deleteCategory(categoryId: String) {
        db.categoryDao().deleteById(categoryId)
    }

    suspend fun setClipCategory(clipId: String, categoryId: String, assigned: Boolean) {
        if (assigned) {
            db.categoryDao().insertCrossRef(ClipCategoryCrossRef(clipId, categoryId))
        } else {
            db.categoryDao().deleteCrossRef(clipId, categoryId)
        }
    }

    suspend fun getClip(clipId: String): ClipEntity? = clips.getById(clipId)

    fun observeClip(clipId: String): Flow<ClipEntity?> = clips.observeById(clipId)

    fun observeShares(clipId: String): Flow<List<ShareRecordEntity>> = shares.observeForClip(clipId)

    suspend fun listShares(clipId: String): List<ShareRecordEntity> = shares.listForClip(clipId)

    suspend fun setWatchStatus(clipId: String, status: WatchStatus) {
        val clip = clips.getById(clipId) ?: return
        clips.update(clip.copy(watchStatus = status))
    }

    suspend fun setReplyStatus(shareId: String, status: ReplyStatus, replyText: String? = null) {
        val share = shares.getById(shareId) ?: return
        shares.update(
            share.copy(
                replyStatus = status,
                replyText = replyText ?: share.replyText,
            ),
        )
    }

    suspend fun updateMetadata(
        clipId: String,
        title: String?,
        creatorName: String?,
        thumbnailUrl: String?,
        metadataStatus: String,
    ) {
        val clip = clips.getById(clipId) ?: return
        clips.update(
            clip.copy(
                title = title ?: clip.title,
                creatorName = creatorName ?: clip.creatorName,
                thumbnailUrl = thumbnailUrl ?: clip.thumbnailUrl,
                metadataStatus = metadataStatus,
            ),
        )
    }
}

enum class InboxFilter {
    ALL,
    UNWATCHED,
    WATCHED,
    NEEDS_REPLY,
    COMPLETED,
}
