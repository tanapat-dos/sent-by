package com.reelshelf.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.reelshelf.app.data.ClipEntity
import com.reelshelf.app.data.ClipInboxRow
import java.net.URI

@Composable
fun ClipPreviewHeader(clip: ClipEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        ThumbnailOrPlaceholder(thumbnailUrl = clip.thumbnailUrl, platformLabel = clip.platform.name)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayTitle(clip.title, clip.originalUrl, clip.canonicalUrl),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text =
                    buildString {
                        append(clip.platform.name.lowercase().replaceFirstChar { it.titlecase() })
                        clip.creatorName?.let {
                            append(" · ")
                            append(it)
                        } ?: run {
                            hostOf(clip.canonicalUrl)?.let {
                                append(" · ")
                                append(it)
                            }
                        }
                    },
                style = MaterialTheme.typography.bodySmall,
            )
            if (clip.title.isNullOrBlank()) {
                Text(
                    text = "No preview title yet — URL saved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun InboxThumbnail(clip: ClipInboxRow) {
    ThumbnailOrPlaceholder(thumbnailUrl = clip.thumbnailUrl, platformLabel = clip.platform.name)
}

@Composable
private fun ThumbnailOrPlaceholder(thumbnailUrl: String?, platformLabel: String) {
    if (thumbnailUrl != null) {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = null,
            modifier = Modifier.width(72.dp).height(96.dp),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier =
                Modifier
                    .width(72.dp)
                    .height(96.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = platformLabel.take(2),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

fun displayTitle(title: String?, originalUrl: String, canonicalUrl: String = originalUrl): String =
    title?.takeIf { it.isNotBlank() }
        ?: hostOf(canonicalUrl)?.let { "$it video" }
        ?: originalUrl

fun hostOf(url: String): String? =
    try {
        URI(url).host?.removePrefix("www.")
    } catch (_: Exception) {
        null
    }
