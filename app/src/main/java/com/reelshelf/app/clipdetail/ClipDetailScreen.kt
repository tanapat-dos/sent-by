package com.reelshelf.app.clipdetail

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reelshelf.app.catchup.copyAndChooseChatApp
import com.reelshelf.app.data.CompletionRules
import com.reelshelf.app.data.ReplyStatus
import com.reelshelf.app.data.WatchStatus
import com.reelshelf.app.open.ClipOpener
import com.reelshelf.app.open.OpenOutcome
import com.reelshelf.app.reply.RecentReplyStore
import com.reelshelf.app.ui.ClipPreviewHeader
import com.reelshelf.app.ui.displayTitle

private val replyPresets = listOf("😂", "❤️", "That was good")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClipDetailScreen(
    viewModel: ClipDetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val replyStore = remember { RecentReplyStore(context) }
    var recentReplies by remember { mutableStateOf(replyStore.list()) }
    val clip = state.clip
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        clip?.let { displayTitle(it.title, it.originalUrl, it.canonicalUrl) } ?: "Clip",
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (clip == null) {
            Text("Clip not found", modifier = Modifier.padding(padding).padding(16.dp))
            return@Scaffold
        }
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ClipPreviewHeader(clip = clip)
            Text(clip.originalUrl, style = MaterialTheme.typography.bodySmall)
            val completed =
                CompletionRules.isCompleted(
                    clip.watchStatus,
                    state.shares.map { it.share.replyStatus },
                )
            if (completed) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        tint = Color(0xFF2E7D32),
                    )
                    Text(
                        text = "Completed — watched and all replies handled",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF2E7D32),
                    )
                }
            } else {
                Text(
                    text = clip.watchStatus.name.lowercase(),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Button(
                onClick = {
                    when (ClipOpener(context).open(clip.originalUrl)) {
                        OpenOutcome.Started -> viewModel.setWatched(true)
                        OpenOutcome.NoHandler ->
                            Toast.makeText(context, "No app can open this URL", Toast.LENGTH_SHORT).show()
                        OpenOutcome.InvalidUrl, OpenOutcome.Failed ->
                            Toast.makeText(context, "Could not open URL", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.semantics { contentDescription = "Open original clip" },
            ) {
                Text("Open clip")
            }
            Text(
                text = "Opening a clip marks it watched. You can undo below.",
                style = MaterialTheme.typography.bodySmall,
            )
            FilterChip(
                selected = clip.watchStatus == WatchStatus.WATCHED,
                onClick = { viewModel.setWatched(clip.watchStatus != WatchStatus.WATCHED) },
                label = {
                    Text(
                        if (clip.watchStatus == WatchStatus.WATCHED) {
                            "Watched (tap to undo)"
                        } else {
                            "Mark watched"
                        },
                    )
                },
            )
            Text("Categories", style = MaterialTheme.typography.titleMedium)
            if (state.allCategories.isEmpty()) {
                Text(
                    text = "No categories yet. Create some from the Categories screen.",
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.allCategories.forEach { category ->
                        val assigned = category.id in state.assignedCategoryIds
                        FilterChip(
                            selected = assigned,
                            onClick = { viewModel.toggleCategory(category.id, !assigned) },
                            label = { Text(category.name) },
                        )
                    }
                }
            }
            Text("Senders", style = MaterialTheme.typography.titleMedium)
            state.shares.forEach { item ->
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "${item.sender?.displayName ?: "Unknown"} · ${item.share.sourceApp}",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = "Reply: ${item.share.replyStatus.name.lowercase().replace('_', ' ')}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReplyStatus.entries.forEach { status ->
                            FilterChip(
                                selected = item.share.replyStatus == status,
                                onClick = { viewModel.setReplyStatus(item.share.id, status) },
                                label = {
                                    Text(
                                        when (status) {
                                            ReplyStatus.NEEDS_REPLY -> "Needs reply"
                                            ReplyStatus.REPLIED -> "Replied"
                                            ReplyStatus.NO_REPLY_NEEDED -> "No reply needed"
                                        },
                                    )
                                },
                            )
                        }
                    }
                    var custom by remember(item.share.id) { mutableStateOf(item.share.replyText.orEmpty()) }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (replyPresets + recentReplies).distinct().forEach { preset ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    copyAndChooseChatApp(context, preset)
                                    if (preset !in replyPresets) {
                                        replyStore.remember(preset)
                                        recentReplies = replyStore.list()
                                    }
                                    viewModel.setReplyStatus(item.share.id, ReplyStatus.REPLIED, preset)
                                },
                                label = { Text(preset) },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = custom,
                        onValueChange = { custom = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Custom reply") },
                        singleLine = true,
                    )
                    Button(
                        onClick = {
                            if (custom.isBlank()) return@Button
                            copyAndChooseChatApp(context, custom)
                            replyStore.remember(custom)
                            recentReplies = replyStore.list()
                            viewModel.setReplyStatus(item.share.id, ReplyStatus.REPLIED, custom)
                        },
                    ) {
                        Text("Copy reply & choose chat app")
                    }
                    Text(
                        text = "Copied — pick an app. Paste and send yourself.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
