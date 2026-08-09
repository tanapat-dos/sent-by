package com.reelshelf.app.data

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SenderFavoriteOrderingTest {
    private lateinit var db: ReelShelfDatabase
    private lateinit var senders: SenderRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = ReelShelfDatabase.buildInMemory(context)
        senders = SenderRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun favoritesSortBeforeRecent() = runBlocking {
        val older = senders.create("Alice", now = 1_000)
        val newer = senders.create("Bob", now = 2_000)
        senders.setFavorite(older.id, true)

        val ordered = senders.observeRecent().first()
        assertThat(ordered.map { it.displayName }).containsExactly("Alice", "Bob").inOrder()
        assertThat(ordered.first().isFavorite).isTrue()
        assertThat(newer.isFavorite).isFalse()
    }
}
