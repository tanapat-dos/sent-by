package com.reelshelf.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy & data") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Sent By stores only what you explicitly share into the app.",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                "Locally on this device we may store: shared URLs, optional shared text you chose to send, " +
                    "sender labels you create, notes, reply drafts, and public preview metadata fetched for those URLs.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "We do not read your LINE or Messenger inbox, contacts, notifications, or accessibility services.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Private message text is not uploaded. Cloud sync is not enabled in this MVP.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Reply shortcuts only copy text to your clipboard and open LINE or Messenger generally. " +
                    "You send the message yourself.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
