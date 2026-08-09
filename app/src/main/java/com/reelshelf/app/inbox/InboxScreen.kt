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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reelshelf.app.data.ClipInboxRow
import com.reelshelf.app.data.InboxFilter
import com.reelshelf.app.data.WatchStatus
import com.reelshelf.app.ui.Copy
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
                title = {
                    Column {
                        Text(Copy.APP_NAME)
                        Text(
                            text = Copy.TAGLINE,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
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
            PrimaryNav(
                current =
                    when (state.filter) {
                        InboxFilter.COMPLETED -> NavTab.DONE
                        else -> NavTab.INBOX
                    },
                onInbox = { viewModel.setFilter(InboxFilter.ALL) },
                onSenders = onSenders,
                onDone = { viewModel.setFilter(InboxFilter.COMPLETED) },
            )
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
            if (state.filter != InboxFilter.COMPLETED) {
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
                    listOf(
                        InboxFilter.UNWATCHED,
                        InboxFilter.WATCHED,
                        InboxFilter.NEEDS_REPLY,
                    ).forEach { filter ->
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
                                            else -> filter.name
                                        },
                                    maxLines = 1,
                                    softWrap = false,
                                )
                            },
                        )
                    }
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
                state.clips.isEmpty() && state.filter == InboxFilter.COMPLETED -> {
                    Text(
                        text = Copy.ALL_CAUGHT_UP,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
                state.clips.isEmpty() -> {
                    Text(
                        text = Copy.EMPTY_INBOX,
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

private enum class NavTab { INBOX, SENDERS, DONE }

@Composable
private fun PrimaryNav(
    current: NavTab,
    onInbox: () -> Unit,
    onSenders: () -> Unit,
    onDone: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavTabButton("Inbox", selected = current == NavTab.INBOX, onClick = onInbox)
        Text("|", color = MaterialTheme.colorScheme.onSurfaceVariant)
        NavTabButton("Senders", selected = current == NavTab.SENDERS, onClick = onSenders)
        Text("|", color = MaterialTheme.colorScheme.onSurfaceVariant)
        NavTabButton("Done", selected = current == NavTab.DONE, onClick = onDone)
    }
}

@Composable
private fun NavTabButton(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            text = label,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color =
                if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
        )
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
                            if (completed) append(", done")
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
                        contentDescription = "Done",
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
                        append(Copy.sentByCsv(clip.senderNames))
                        clip.categoryNames?.takeIf { it.isNotBlank() }?.let {
                            append(" · ")
                            append(it)
                        }
                    },
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text =
                    Copy.statusLine(
                        completed = completed,
                        watched = clip.watchStatus == WatchStatus.WATCHED,
                        outstandingReplies = clip.outstandingReplyCount,
                    ),
                style = MaterialTheme.typography.labelLarge,
                color = if (completed) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
