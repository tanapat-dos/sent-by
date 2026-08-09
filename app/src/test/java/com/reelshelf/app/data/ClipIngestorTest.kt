package com.reelshelf.app.data

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ClipIngestorTest {
    private lateinit var db: ReelShelfDatabase
    private lateinit var senders: SenderRepository
    private lateinit var ingestor: ClipIngestor
    private val enqueued = mutableListOf<String>()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = ReelShelfDatabase.buildInMemory(context)
        senders = SenderRepository(db)
        ingestor = ClipIngestor(db) { enqueued += it }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun createsClipAndShare() = runBlocking {
        val sender = senders.create("May")
        val outcome =
            ingestor.ingest(
                IngestRequest(
                    text = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    senderId = sender.id,
                    sourceApp = SourceApp.LINE,
                    fingerprint = "fp-1",
                ),
            )
        assertThat(outcome).isInstanceOf(IngestOutcome.Saved::class.java)
        val saved = outcome as IngestOutcome.Saved
        assertThat(saved.results).hasSize(1)
        assertThat(saved.results[0].wasExistingClip).isFalse()
        assertThat(enqueued).containsExactly(saved.results[0].clipId)
        val shares = db.shareDao().listForClip(saved.results[0].clipId)
        assertThat(shares).hasSize(1)
        assertThat(shares[0].replyStatus).isEqualTo(ReplyStatus.NEEDS_REPLY)
    }

    @Test
    fun duplicateCanonicalAddsShareNotClip() = runBlocking {
        val may = senders.create("May")
        val bob = senders.create("Bob")
        ingestor.ingest(
            IngestRequest(
                text = "https://youtu.be/dQw4w9WgXcQ?si=track",
                senderId = may.id,
                sourceApp = SourceApp.LINE,
                fingerprint = "fp-a",
            ),
        )
        enqueued.clear()
        val outcome =
            ingestor.ingest(
                IngestRequest(
                    text = "https://www.youtube.com/watch?v=dQw4w9WgXcQ&utm_source=x",
                    senderId = bob.id,
                    sourceApp = SourceApp.MESSENGER,
                    fingerprint = "fp-b",
                ),
            ) as IngestOutcome.Saved
        assertThat(outcome.results).hasSize(1)
        assertThat(outcome.results[0].wasExistingClip).isTrue()
        val clipId = outcome.results[0].clipId
        assertThat(db.shareDao().listForClip(clipId)).hasSize(2)
        assertThat(db.clipDao().getById(clipId)).isNotNull()
    }

    @Test
    fun repeatedIntentFingerprintSuppressed() = runBlocking {
        val sender = senders.create("May")
        val first =
            ingestor.ingest(
                IngestRequest(
                    text = "https://www.tiktok.com/@u/video/7234567890123456789",
                    senderId = sender.id,
                    sourceApp = SourceApp.LINE,
                    fingerprint = "same-fp",
                ),
            )
        assertThat(first).isInstanceOf(IngestOutcome.Saved::class.java)
        val second =
            ingestor.ingest(
                IngestRequest(
                    text = "https://www.tiktok.com/@u/video/7234567890123456789",
                    senderId = sender.id,
                    sourceApp = SourceApp.LINE,
                    fingerprint = "same-fp",
                ),
            )
        assertThat(second).isEqualTo(IngestOutcome.DuplicateIntent)
    }

    @Test
    fun sameSenderNewEventStillCreatesShare() = runBlocking {
        val sender = senders.create("May")
        val url = "https://www.instagram.com/reel/AbCdEfGhIjK/"
        ingestor.ingest(
            IngestRequest(url, sender.id, SourceApp.LINE, fingerprint = "e1"),
        )
        val second =
            ingestor.ingest(
                IngestRequest(url, sender.id, SourceApp.LINE, fingerprint = "e2"),
            ) as IngestOutcome.Saved
        assertThat(second.results[0].wasExistingClip).isTrue()
        assertThat(db.shareDao().listForClip(second.results[0].clipId)).hasSize(2)
    }

    @Test
    fun noUrls() = runBlocking {
        val sender = senders.create("May")
        val outcome =
            ingestor.ingest(
                IngestRequest("hello", sender.id, SourceApp.OTHER, fingerprint = "x"),
            )
        assertThat(outcome).isEqualTo(IngestOutcome.NoUrls)
    }
}
