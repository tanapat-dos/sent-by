package com.reelshelf.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    val t = LocalUiStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(t.helpTitle) },
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(t.helpOverview, style = MaterialTheme.typography.bodyMedium)
                t.helpSteps.forEach { step ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(step.title, style = MaterialTheme.typography.titleSmall)
                        Text(step.body, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Text(
                    t.helpFoot,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(t.gotIt) }
        },
    )
}
