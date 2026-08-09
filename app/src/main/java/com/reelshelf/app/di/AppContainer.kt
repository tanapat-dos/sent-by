package com.reelshelf.app.di

import android.content.Context
import androidx.work.WorkManager
import com.reelshelf.app.data.ClipIngestor
import com.reelshelf.app.data.ClipRepository
import com.reelshelf.app.data.ReelShelfDatabase
import com.reelshelf.app.data.SenderRepository
import com.reelshelf.app.metadata.MetadataEnrichmentScheduler
import com.reelshelf.app.metadata.MetadataFetcherRegistry
import com.reelshelf.app.metadata.UnsupportedMetadataFetcher
import com.reelshelf.app.metadata.YoutubeOEmbedFetcher
import com.reelshelf.app.ui.LocalePreferences
import com.reelshelf.app.urls.Platform

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val localePreferences = LocalePreferences(appContext)

    val database: ReelShelfDatabase = ReelShelfDatabase.build(appContext)

    val metadataRegistry: MetadataFetcherRegistry =
        MetadataFetcherRegistry(
            listOf(
                YoutubeOEmbedFetcher(),
                UnsupportedMetadataFetcher(Platform.TIKTOK, "Live metadata not enabled until allow-listed"),
                UnsupportedMetadataFetcher(Platform.INSTAGRAM, "Live metadata not enabled until allow-listed"),
                UnsupportedMetadataFetcher(Platform.FACEBOOK, "Live metadata not enabled until allow-listed"),
                UnsupportedMetadataFetcher(Platform.OTHER, "No metadata fetcher for unknown platforms"),
            ),
        )

    val workManager: WorkManager by lazy { WorkManager.getInstance(appContext) }

    val metadataScheduler: MetadataEnrichmentScheduler by lazy {
        MetadataEnrichmentScheduler(workManager)
    }

    val clipRepository = ClipRepository(database)
    val senderRepository = SenderRepository(database)

    val clipIngestor: ClipIngestor by lazy {
        ClipIngestor(database) { clipId ->
            metadataScheduler.enqueue(clipId)
        }
    }
}
