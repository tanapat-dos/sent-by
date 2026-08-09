package com.reelshelf.app.data

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseSchemaTest {
    @Test
    fun createsV1Tables() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = ReelShelfDatabase.buildInMemory(context)
        db.openHelper.readableDatabase.query("SELECT name FROM sqlite_master WHERE type='table'").use { cursor ->
            val tables = mutableListOf<String>()
            while (cursor.moveToNext()) {
                tables += cursor.getString(0)
            }
            assertThat(tables).containsAtLeast(
                "clips",
                "share_records",
                "senders",
                "ingestion_events",
                "categories",
                "clip_categories",
            )
        }
        db.close()
    }
}
