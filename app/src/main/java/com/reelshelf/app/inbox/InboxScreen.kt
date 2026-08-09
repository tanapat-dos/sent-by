package com.reelshelf.app.inbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reelshelf.app.data.ClipInboxRow
import com.reelshelf.app.data.InboxFilter
import com.reelshelf.app.data.WatchStatus
import com.reelshelf.app.ui.InboxThumbnail
import com.reelshelf.app.ui.displayTitle
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    viewModel: InboxViewModel,
    onOpenClip: (String) -> Unit,
    onPaste: () -> Unit,
    onSenders: () -> Unit,
    onCategories: () -> Unit,
    onCatchUp: (InboxFilter) -> Unit,
    onPrivacy: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ReelShelf") },
                actions = {
                    IconButton(
                        onClick = { onCatchUp(InboxFilter.UNWATCHED) },
                        modifier = Modifier.semantics { contentDescription = "Catch up unwatched" },
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                    }
                    IconButton(onClick = onPaste, modifier = Modifier.semantics { contentDescription = "Paste link" }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                    IconButton(onClick = onCategories, modifier = Modifier.semantics { contentDescription = "Categories" }) {
                        Icon(Icons.Default.Category, contentDescription = null)
                    }
                    IconButton(onClick = onSenders, modifier = Modifier.semantics { contentDescription = "Senders" }) {
                        Icon(Icons.Default.People, contentDescription = null)
                    }
                    IconButton(onClick = onPrivacy, modifier = Modifier.semantics { contentDescription = "Privacy" }) {
                        Icon(Icons.Default.Info, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Search clips" },
                singleLine = true,
                label = { Text("Search sender, platform, title, URL") },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(onClick = { onCatchUp(InboxFilter.UNWATCHED) }) {
                    Text("Catch up unwatched")
                }
                TextButton(onClick = { onCatchUp(InboxFilter.NEEDS_REPLY) }) {
                    Text("Catch up needs reply")
                }
            }
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                InboxFilter.entries.forEach { filter ->
                    if (filter == InboxFilter.ALL) return@forEach
                    FilterChip(
                        selected = state.filter == filter,
                        onClick = {
                            viewModel.setFilter(if (state.filter == filter) InboxFilter.ALL else filter)
                        },
                        label = {
                            Text(
                                text =
                                    when (filter) {
                                        InboxFilter.UNWATCHED -> "Unwatched"
                                        InboxFilter.WATCHED -> "Watched"
                                        InboxFilter.NEEDS_REPLY -> "Needs reply"
                                        InboxFilter.COMPLETED -> "Completed"
                                        InboxFilter.ALL -> "All"
                                    },
                                maxLines = 1,
                                softWrap = false,
                            )
                        },
                    )
                }
            }
            if (state.categories.isNotEmpty()) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = state.categoryId == null,
                        onClick = { viewModel.setCategoryId(null) },
                        label = { Text("All categories", maxLines = 1, softWrap = false) },
                    )
                    state.categories.forEach { category ->
                        FilterChip(
                            selected = state.categoryId == category.id,
                            onClick = {
                                viewModel.setCategoryId(
                                    if (state.categoryId == category.id) null else category.id,
                                )
                            },
                            label = { Text(category.name, maxLines = 1, softWrap = false) },
                        )
                    }
                }
            }
            when {
                state.clips.isEmpty() && state.query.isNotBlank() -> {
                    Text(
                        text = "No clips match your search.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
                state.clips.isEmpty() -> {
                    Text(
                        text = "Your catch-up inbox is empty. Share a clip from LINE or Messenger, or paste a link.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.clips, key = { it.id }) { clip ->
                            ClipCard(clip = clip, onClick = { onOpenClip(clip.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClipCard(clip: ClipInboxRow, onClick: () -> Unit) {
    val completed =
        clip.watchStatus == WatchStatus.WATCHED &&
            clip.outstandingReplyCount == 0 &&
            clip.senderCount > 0
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .semantics {
                    contentDescription =
                        buildString {
                            append("Clip ${displayTitle(clip.title, clip.originalUrl, clip.canonicalUrl)}")
                            if (completed) append(", completed")
                        }
                }
                .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        InboxThumbnail(clip)
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = displayTitle(clip.title, clip.originalUrl, clip.canonicalUrl),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (completed) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        tint = Color(0xFF2E7D32),
                    )
                }
            }
            Text(
                text =
                    buildString {
                        append(clip.platform.name.lowercase().replaceFirstChar { it.titlecase() })
                        clip.creatorName?.let {
                            append(" · ")
                            append(it)
                        }
                    },
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text =
                    buildString {
                        append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(clip.lastReceivedAt)))
                        append(" · ")
                        append(clip.senderNames ?: "${clip.senderCount} senders")
                        clip.categoryNames?.takeIf { it.isNotBlank() }?.let {
                            append(" · ")
                            append(it)
                        }
                    },
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text =
                    when {
                        completed -> "Completed"
                        clip.watchStatus == WatchStatus.WATCHED && clip.outstandingReplyCount > 0 ->
                            "Watched · ${clip.outstandingReplyCount} need reply"
                        clip.watchStatus == WatchStatus.WATCHED -> "Watched"
                        clip.outstandingReplyCount > 0 ->
                            "Unwatched · ${clip.outstandingReplyCount} need reply"
                        else -> "Unwatched"
                    },
                style = MaterialTheme.typography.labelLarge,
                color = if (completed) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
