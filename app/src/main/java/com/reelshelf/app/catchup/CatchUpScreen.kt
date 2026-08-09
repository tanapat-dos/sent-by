package com.reelshelf.app.catchup

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reelshelf.app.data.ReplyStatus
import com.reelshelf.app.data.WatchStatus
import com.reelshelf.app.open.ClipOpener
import com.reelshelf.app.open.OpenOutcome
import com.reelshelf.app.reply.RecentReplyStore
import com.reelshelf.app.ui.ClipPreviewHeader

private val replyPresets = listOf("😂", "❤️", "That was good")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CatchUpScreen(
    viewModel: CatchUpViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val replyStore = remember { RecentReplyStore(context) }
    var recentReplies by remember { mutableStateOf(replyStore.list()) }
    val clip = state.detail.clip

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catch up · ${state.progressLabel}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (state.finished || clip == null) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = if (state.queue.isEmpty()) "No clips need catch-up right now." else "You're caught up.",
                    style = MaterialTheme.typography.titleMedium,
                )
                Button(onClick = onBack) { Text("Back to inbox") }
            }
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
            Button(
                onClick = {
                    when (ClipOpener(context).open(clip.originalUrl)) {
                        OpenOutcome.Started -> viewModel.setWatched(true)
                        OpenOutcome.NoHandler ->
                            Toast.makeText(context, "No app can open this URL", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(context, "Could not open URL", Toast.LENGTH_SHORT).show()
                    }
                },
            ) {
                Text("Open clip")
            }
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
            Text("Senders", style = MaterialTheme.typography.titleMedium)
            state.detail.shares.forEach { item ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "${item.sender?.displayName ?: "Unknown"} · ${item.share.sourceApp}",
                        style = MaterialTheme.typography.titleSmall,
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
                }
            }
            Button(
                onClick = viewModel::next,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.index + 1 >= state.queue.size) "Finish" else "Next clip")
            }
        }
    }
}

internal fun copyAndChooseChatApp(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("reply", text))
    Toast.makeText(context, "Copied — pick an app", Toast.LENGTH_SHORT).show()
    val send =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
    val chooser =
        Intent.createChooser(send, "Choose chat app").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    runCatching { context.startActivity(chooser) }
}
