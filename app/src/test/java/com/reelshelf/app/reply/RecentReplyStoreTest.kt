package com.reelshelf.app.reply

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RecentReplyStoreTest {
    @Test
    fun remembersMostRecentFirstAndCaps() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("reelshelf_replies", 0).edit().clear().commit()
        val store = RecentReplyStore(context)
        store.remember("one")
        store.remember("two")
        store.remember("one")
        assertThat(store.list().first()).isEqualTo("one")
        assertThat(store.list()).containsExactly("one", "two").inOrder()
    }
}
