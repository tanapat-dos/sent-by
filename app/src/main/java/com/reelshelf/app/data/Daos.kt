package com.reelshelf.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.reelshelf.app.urls.Platform
import kotlinx.coroutines.flow.Flow

data class ClipInboxRow(
    val id: String,
    val originalUrl: String,
    val canonicalUrl: String,
    val platform: Platform,
    val platformContentId: String?,
    val title: String?,
    val creatorName: String?,
    val thumbnailUrl: String?,
    val watchStatus: WatchStatus,
    val createdAt: Long,
    val lastReceivedAt: Long,
    val senderCount: Int,
    val senderNames: String?,
    val outstandingReplyCount: Int,
    val categoryNames: String? = null,
)

@Dao
interface ClipDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(clip: ClipEntity)

    @Update
    suspend fun update(clip: ClipEntity)

    @Query("SELECT * FROM clips WHERE id = :id")
    suspend fun getById(id: String): ClipEntity?

    @Query("SELECT * FROM clips WHERE id = :id")
    fun observeById(id: String): Flow<ClipEntity?>

    @Query(
        """
        SELECT * FROM clips
        WHERE platform = :platform AND platform_content_id = :contentId
        LIMIT 1
        """,
    )
    suspend fun findByPlatformContentId(platform: Platform, contentId: String): ClipEntity?

    @Query("SELECT * FROM clips WHERE canonical_url = :canonicalUrl LIMIT 1")
    suspend fun findByCanonicalUrl(canonicalUrl: String): ClipEntity?

    @Query(
        """
        SELECT
          c.id AS id,
          c.original_url AS originalUrl,
          c.canonical_url AS canonicalUrl,
          c.platform AS platform,
          c.platform_content_id AS platformContentId,
          c.title AS title,
          c.creator_name AS creatorName,
          c.thumbnail_url AS thumbnailUrl,
          c.watch_status AS watchStatus,
          c.created_at AS createdAt,
          c.last_received_at AS lastReceivedAt,
          COUNT(s.id) AS senderCount,
          GROUP_CONCAT(DISTINCT snd.display_name) AS senderNames,
          SUM(CASE WHEN s.reply_status = 'NEEDS_REPLY' THEN 1 ELSE 0 END) AS outstandingReplyCount,
          (
            SELECT GROUP_CONCAT(DISTINCT cat.name)
            FROM clip_categories cc
            JOIN categories cat ON cat.id = cc.category_id
            WHERE cc.clip_id = c.id
          ) AS categoryNames
        FROM clips c
        LEFT JOIN share_records s ON s.clip_id = c.id
        LEFT JOIN senders snd ON snd.id = s.sender_id
        WHERE
          (
            :filter = 'ALL'
            OR (:filter = 'UNWATCHED' AND c.watch_status = 'UNWATCHED')
            OR (:filter = 'WATCHED' AND c.watch_status = 'WATCHED')
            OR (
              :filter = 'NEEDS_REPLY'
              AND EXISTS (
                SELECT 1 FROM share_records sr
                WHERE sr.clip_id = c.id AND sr.reply_status = 'NEEDS_REPLY'
              )
            )
            OR (
              :filter = 'COMPLETED'
              AND c.watch_status = 'WATCHED'
              AND NOT EXISTS (
                SELECT 1 FROM share_records sr
                WHERE sr.clip_id = c.id AND sr.reply_status = 'NEEDS_REPLY'
              )
              AND EXISTS (SELECT 1 FROM share_records sr2 WHERE sr2.clip_id = c.id)
            )
          )
          AND (
            :categoryId = ''
            OR EXISTS (
              SELECT 1 FROM clip_categories cc2
              WHERE cc2.clip_id = c.id AND cc2.category_id = :categoryId
            )
          )
          AND (
            :query = ''
            OR LOWER(IFNULL(c.title, '')) LIKE '%' || :query || '%'
            OR LOWER(IFNULL(c.creator_name, '')) LIKE '%' || :query || '%'
            OR LOWER(c.original_url) LIKE '%' || :query || '%'
            OR LOWER(c.canonical_url) LIKE '%' || :query || '%'
            OR LOWER(c.platform) LIKE '%' || :query || '%'
            OR EXISTS (
              SELECT 1 FROM share_records sr
              JOIN senders snd2 ON snd2.id = sr.sender_id
              WHERE sr.clip_id = c.id AND LOWER(snd2.display_name) LIKE '%' || :query || '%'
            )
            OR EXISTS (
              SELECT 1 FROM clip_categories cc3
              JOIN categories cat3 ON cat3.id = cc3.category_id
              WHERE cc3.clip_id = c.id AND LOWER(cat3.name) LIKE '%' || :query || '%'
            )
          )
        GROUP BY c.id
        ORDER BY
          CASE WHEN c.watch_status = 'UNWATCHED' THEN 0 ELSE 1 END,
          c.last_received_at DESC
        """,
    )
    fun observeInbox(filter: String, query: String, categoryId: String): Flow<List<ClipInboxRow>>

    @Query(
        """
        SELECT DISTINCT c.* FROM clips c
        JOIN share_records s ON s.clip_id = c.id
        WHERE s.sender_id = :senderId
        ORDER BY c.last_received_at DESC
        """,
    )
    fun observeClipsForSender(senderId: String): Flow<List<ClipEntity>>

    @Query("SELECT * FROM clips WHERE metadata_status IN ('PENDING', 'RETRYABLE')")
    suspend fun clipsNeedingMetadata(): List<ClipEntity>
}

@Dao
interface ShareDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(share: ShareRecordEntity)

    @Update
    suspend fun update(share: ShareRecordEntity)

    @Query("SELECT * FROM share_records WHERE clip_id = :clipId ORDER BY received_at DESC")
    fun observeForClip(clipId: String): Flow<List<ShareRecordEntity>>

    @Query("SELECT * FROM share_records WHERE clip_id = :clipId ORDER BY received_at DESC")
    suspend fun listForClip(clipId: String): List<ShareRecordEntity>

    @Query("SELECT * FROM share_records WHERE id = :id")
    suspend fun getById(id: String): ShareRecordEntity?

    @Query("UPDATE share_records SET sender_id = :intoSenderId WHERE sender_id = :fromSenderId")
    suspend fun reassignSender(fromSenderId: String, intoSenderId: String)
}

@Dao
interface SenderDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(sender: SenderEntity)

    @Update
    suspend fun update(sender: SenderEntity)

    @Query("SELECT * FROM senders WHERE id = :id")
    suspend fun getById(id: String): SenderEntity?

    @Query("SELECT * FROM senders WHERE id = :id")
    fun observeById(id: String): Flow<SenderEntity?>

    @Query(
        """
        SELECT * FROM senders
        ORDER BY is_favorite DESC, last_used_at DESC
        """,
    )
    fun observeRecent(): Flow<List<SenderEntity>>

    @Query(
        """
        SELECT * FROM senders
        ORDER BY is_favorite DESC, last_used_at DESC
        """,
    )
    suspend fun listRecent(): List<SenderEntity>

    @Query("DELETE FROM senders WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface IngestionEventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: IngestionEventEntity): Long

    @Query("SELECT * FROM ingestion_events WHERE fingerprint = :fingerprint LIMIT 1")
    suspend fun findByFingerprint(fingerprint: String): IngestionEventEntity?

    @Query("DELETE FROM ingestion_events WHERE created_at < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: CategoryEntity)

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?

    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY LOWER(name) ASC")
    suspend fun listAll(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(ref: ClipCategoryCrossRef)

    @Query(
        """
        DELETE FROM clip_categories
        WHERE clip_id = :clipId AND category_id = :categoryId
        """,
    )
    suspend fun deleteCrossRef(clipId: String, categoryId: String)

    @Query(
        """
        SELECT c.* FROM categories c
        JOIN clip_categories cc ON cc.category_id = c.id
        WHERE cc.clip_id = :clipId
        ORDER BY c.name COLLATE NOCASE ASC
        """,
    )
    fun observeForClip(clipId: String): Flow<List<CategoryEntity>>

    @Query(
        """
        SELECT category_id FROM clip_categories WHERE clip_id = :clipId
        """,
    )
    suspend fun categoryIdsForClip(clipId: String): List<String>
}
