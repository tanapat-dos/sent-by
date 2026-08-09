package com.reelshelf.app.metadata

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.reelshelf.app.ReelShelfApp
import com.reelshelf.app.data.MetadataWorkStatus
import java.util.concurrent.TimeUnit

class MetadataEnrichmentScheduler(
    private val workManager: WorkManager,
) {
    fun enqueue(clipId: String) {
        val request =
            OneTimeWorkRequestBuilder<MetadataEnrichmentWorker>()
                .setInputData(workDataOf(MetadataEnrichmentWorker.KEY_CLIP_ID to clipId))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
        workManager.enqueueUniqueWork(
            "metadata-$clipId",
            ExistingWorkPolicy.KEEP,
            request,
        )
    }
}

class MetadataEnrichmentWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val clipId = inputData.getString(KEY_CLIP_ID) ?: return Result.failure()
        val container = (applicationContext as ReelShelfApp).container
        val clip = container.clipRepository.getClip(clipId) ?: return Result.success()

        val result =
            container.metadataRegistry.fetch(
                platform = clip.platform,
                canonicalUrl = clip.canonicalUrl,
                platformContentId = clip.platformContentId,
            )

        val status =
            when (result.failure) {
                null ->
                    if (result.isPartialSuccess && (result.title == null || result.creatorName == null)) {
                        MetadataWorkStatus.PARTIAL
                    } else if (result.isPartialSuccess || result.title != null) {
                        MetadataWorkStatus.SUCCESS
                    } else {
                        MetadataWorkStatus.NOT_SUPPORTED
                    }
                is MetadataFailure.Retryable -> {
                    container.clipRepository.updateMetadata(
                        clipId = clipId,
                        title = result.title,
                        creatorName = result.creatorName,
                        thumbnailUrl = result.thumbnailUrl,
                        metadataStatus = MetadataWorkStatus.RETRYABLE,
                    )
                    return if (runAttemptCount >= MAX_ATTEMPTS) Result.failure() else Result.retry()
                }
                is MetadataFailure.Permanent -> MetadataWorkStatus.PERMANENT
                is MetadataFailure.LoginRequired -> MetadataWorkStatus.LOGIN_REQUIRED
                is MetadataFailure.NotSupported -> MetadataWorkStatus.NOT_SUPPORTED
            }

        container.clipRepository.updateMetadata(
            clipId = clipId,
            title = result.title,
            creatorName = result.creatorName,
            thumbnailUrl = result.thumbnailUrl,
            metadataStatus = status,
        )
        return Result.success()
    }

    companion object {
        const val KEY_CLIP_ID = "clip_id"
        private const val MAX_ATTEMPTS = 5
    }
}
