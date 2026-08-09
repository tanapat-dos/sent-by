package com.reelshelf.app.sender

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reelshelf.app.data.SenderEntity
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendersScreen(
    viewModel: SendersViewModel,
    onBack: () -> Unit,
    onOpenSender: (String) -> Unit,
) {
    val senders by viewModel.senders.collectAsStateWithLifecycle()
    var mergeFromId by remember { mutableStateOf<String?>(null) }
    var mergeIntoId by remember { mutableStateOf<String?>(null) }
    val mergeFrom = senders.firstOrNull { it.id == mergeFromId }
    val mergeInto = senders.firstOrNull { it.id == mergeIntoId }

    if (mergeFrom != null && mergeInto != null) {
        AlertDialog(
            onDismissRequest = {
                mergeFromId = null
                mergeIntoId = null
            },
            title = { Text("Merge senders") },
            text = {
                Text("Merge \"${mergeFrom.displayName}\" into \"${mergeInto.displayName}\"? All shares move to ${mergeInto.displayName}.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.merge(mergeFrom.id, mergeInto.id)
                        mergeFromId = null
                        mergeIntoId = null
                    },
                ) { Text("Merge") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mergeFromId = null
                        mergeIntoId = null
                    },
                ) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Senders") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Favorites appear first in quick save. To merge: choose source, then target.",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (mergeFromId != null && mergeIntoId == null) {
                    Text(
                        "Selected merge source: ${mergeFrom?.displayName}. Tap another sender as the target.",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    TextButton(onClick = { mergeFromId = null }) { Text("Cancel merge") }
                }
            }
            items(senders, key = { it.id }) { sender ->
                SenderRow(
                    sender = sender,
                    onOpen = { onOpenSender(sender.id) },
                    onRename = { viewModel.rename(sender.id, it) },
                    onToggleFavorite = {
                        viewModel.setFavorite(sender.id, !sender.isFavorite)
                    },
                    onMergeAction = {
                        when {
                            mergeFromId == null -> mergeFromId = sender.id
                            mergeFromId == sender.id -> mergeFromId = null
                            else -> mergeIntoId = sender.id
                        }
                    },
                    mergeLabel =
                        when (mergeFromId) {
                            null -> "Use as merge source"
                            sender.id -> "Source selected"
                            else -> "Merge into this sender"
                        },
                )
            }
        }
    }
}

@Composable
private fun SenderRow(
    sender: SenderEntity,
    onOpen: () -> Unit,
    onRename: (String) -> Unit,
    onToggleFavorite: () -> Unit,
    onMergeAction: () -> Unit,
    mergeLabel: String,
) {
    var name by remember(sender.id, sender.displayName) { mutableStateOf(sender.displayName) }
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.semantics {
                    contentDescription =
                        if (sender.isFavorite) "Unfavorite ${sender.displayName}" else "Favorite ${sender.displayName}"
                },
            ) {
                Icon(
                    imageVector = if (sender.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = null,
                )
            }
            Text(
                text = sender.displayName,
                style = MaterialTheme.typography.titleMedium,
                modifier =
                    Modifier
                        .weight(1f)
                        .clickable(onClick = onOpen),
            )
        }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Edit name") },
            singleLine = true,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onRename(name) }) { Text("Save name") }
            TextButton(onClick = onMergeAction) { Text(mergeLabel) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SenderDetailScreen(
    viewModel: SenderDetailViewModel,
    onBack: () -> Unit,
    onOpenClip: (String) -> Unit,
) {
    val sender by viewModel.sender.collectAsStateWithLifecycle()
    val clips by viewModel.clips.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sender?.displayName ?: "Sender") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    text = "${summary.clipCount} clip(s) · ${summary.lastReceivedLabel()}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            items(clips, key = { it.id }) { clip ->
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpenClip(clip.id) }
                            .padding(vertical = 8.dp),
                ) {
                    Text(
                        text = clip.title ?: clip.originalUrl,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text =
                            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                                .format(Date(clip.lastReceivedAt)),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
