package com.reelshelf.app.data

import androidx.room.withTransaction
import com.reelshelf.app.urls.UrlCanonicalizer
import com.reelshelf.app.urls.UrlExtractor
import java.security.MessageDigest
import java.util.UUID

data class IngestRequest(
    val text: String,
    val senderId: String,
    val sourceApp: SourceApp,
    val fingerprint: String? = null,
    val now: Long = System.currentTimeMillis(),
)

data class IngestedClipResult(
    val clipId: String,
    val originalUrl: String,
    val canonicalUrl: String,
    val wasExistingClip: Boolean,
)

sealed class IngestOutcome {
    data class Saved(
        val results: List<IngestedClipResult>,
        val duplicateIntent: Boolean = false,
    ) : IngestOutcome()

    data object NoUrls : IngestOutcome()

    data object DuplicateIntent : IngestOutcome()
}

class ClipIngestor(
    private val db: ReelShelfDatabase,
    private val onClipSaved: (clipId: String) -> Unit = {},
) {
    private val clips get() = db.clipDao()
    private val shares get() = db.shareDao()
    private val senders get() = db.senderDao()
    private val events get() = db.ingestionEventDao()

    suspend fun ingest(request: IngestRequest): IngestOutcome {
        val urls = UrlExtractor.extract(request.text)
        if (urls.isEmpty()) return IngestOutcome.NoUrls

        val fingerprint =
            request.fingerprint
                ?: fingerprintFor(request.text, request.senderId, request.sourceApp)

        return db.withTransaction {
            events.deleteOlderThan(request.now - IDEMPOTENCY_TTL_MS)
            val inserted =
                events.insert(
                    IngestionEventEntity(
                        id = UUID.randomUUID().toString(),
                        fingerprint = fingerprint,
                        createdAt = request.now,
                    ),
                )
            if (inserted == -1L) {
                return@withTransaction IngestOutcome.DuplicateIntent
            }

            val sender = senders.getById(request.senderId)
                ?: error("Unknown sender ${request.senderId}")
            senders.update(sender.copy(lastUsedAt = request.now))

            val results = mutableListOf<IngestedClipResult>()
            for (url in urls) {
                val canonical = UrlCanonicalizer.canonicalize(url)
                val existing =
                    canonical.platformContentId?.let {
                        clips.findByPlatformContentId(canonical.platform, it)
                    } ?: clips.findByCanonicalUrl(canonical.canonicalUrl)

                val clipId: String
                val wasExisting: Boolean
                if (existing != null) {
                    clips.update(existing.copy(lastReceivedAt = request.now))
                    clipId = existing.id
                    wasExisting = true
                } else {
                    clipId = UUID.randomUUID().toString()
                    clips.insert(
                        ClipEntity(
                            id = clipId,
                            originalUrl = canonical.originalUrl,
                            canonicalUrl = canonical.canonicalUrl,
                            platform = canonical.platform,
                            platformContentId = canonical.platformContentId,
                            createdAt = request.now,
                            lastReceivedAt = request.now,
                        ),
                    )
                    wasExisting = false
                }

                shares.insert(
                    ShareRecordEntity(
                        id = UUID.randomUUID().toString(),
                        clipId = clipId,
                        senderId = request.senderId,
                        sourceApp = request.sourceApp,
                        receivedAt = request.now,
                        originalText = request.text,
                        replyStatus = ReplyStatus.NEEDS_REPLY,
                    ),
                )

                results +=
                    IngestedClipResult(
                        clipId = clipId,
                        originalUrl = canonical.originalUrl,
                        canonicalUrl = canonical.canonicalUrl,
                        wasExistingClip = wasExisting,
                    )
            }
            IngestOutcome.Saved(results)
        }.also { outcome ->
            if (outcome is IngestOutcome.Saved) {
                outcome.results.forEach { onClipSaved(it.clipId) }
            }
        }
    }

    companion object {
        const val IDEMPOTENCY_TTL_MS = 5 * 60 * 1000L

        fun fingerprintFor(text: String, senderId: String, sourceApp: SourceApp): String {
            val material = "$senderId|${sourceApp.name}|${text.trim()}"
            val digest = MessageDigest.getInstance("SHA-256").digest(material.toByteArray())
            return digest.joinToString("") { "%02x".format(it) }
        }
    }
}
