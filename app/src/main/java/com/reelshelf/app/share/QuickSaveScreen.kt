package com.reelshelf.app.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reelshelf.app.data.SourceApp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickSaveScreen(
    viewModel: QuickSaveViewModel,
    title: String = "Quick save",
    allowEditText: Boolean = false,
    onFinished: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.done) {
        if (state.done) {
            kotlinx.coroutines.delay(650)
            onFinished()
        }
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (allowEditText) {
                OutlinedTextField(
                    value = state.text,
                    onValueChange = viewModel::setText,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Paste link or text with URLs") },
                    minLines = 3,
                )
            } else {
                Text(
                    text = state.text.ifBlank { "(empty share text)" },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                text = "${state.urlCount} URL(s) detected",
                style = MaterialTheme.typography.labelLarge,
            )
            Text("Sender", style = MaterialTheme.typography.titleMedium)
            val favorites = state.recentSenders.filter { it.isFavorite }
            val others = state.recentSenders.filterNot { it.isFavorite }
            if (favorites.isNotEmpty()) {
                Text("Favorites", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    favorites.forEach { sender ->
                        FilterChip(
                            selected = state.selectedSenderId == sender.id,
                            onClick = { viewModel.selectSender(sender.id) },
                            label = { Text("★ ${sender.displayName}") },
                            modifier = Modifier.semantics { contentDescription = "Favorite sender ${sender.displayName}" },
                        )
                    }
                }
            }
            if (others.isNotEmpty()) {
                Text("Recent", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    others.forEach { sender ->
                        FilterChip(
                            selected = state.selectedSenderId == sender.id,
                            onClick = { viewModel.selectSender(sender.id) },
                            label = { Text(sender.displayName) },
                            modifier = Modifier.semantics { contentDescription = "Sender ${sender.displayName}" },
                        )
                    }
                }
            }
            OutlinedTextField(
                value = state.newSenderName,
                onValueChange = viewModel::setNewSenderName,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("New sender name") },
            )
            TextButton(
                onClick = viewModel::createSenderAndSelect,
                enabled = state.newSenderName.isNotBlank(),
            ) {
                Text("Create sender")
            }
            Text("Source app", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SourceApp.entries.forEach { app ->
                    FilterChip(
                        selected = state.sourceApp == app,
                        onClick = { viewModel.setSourceApp(app) },
                        label = { Text(app.name) },
                    )
                }
            }
            state.message?.let {
                Text(text = it, style = MaterialTheme.typography.bodyLarge)
            }
            Button(
                onClick = viewModel::save,
                enabled = !state.saving && !state.done && state.urlCount > 0,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Save clips" },
            ) {
                Text(if (state.saving) "Saving…" else "Save")
            }
        }
    }
}
