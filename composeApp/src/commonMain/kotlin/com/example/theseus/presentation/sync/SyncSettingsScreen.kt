package com.example.theseus.presentation.sync

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.theseus.data.sync.SyncResult
import com.example.theseus.data.sync.SyncState
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SyncViewModel = koinViewModel()
) {
    val syncState by viewModel.syncState.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sync Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sync Status",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    when (val state = syncState) {
                        is SyncState.Idle -> {
                            Text("Ready to sync")
                        }
                        is SyncState.Syncing -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Syncing...")
                            }
                        }
                        is SyncState.Success -> {
                            Text(
                                "Last successful sync:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            lastSyncTime?.let { timestamp ->
                                val dateTime = Instant.fromEpochMilliseconds(timestamp)
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                Text(
                                    "${dateTime.date} at ${dateTime.time}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        is SyncState.Error -> {
                            Text(
                                "Sync failed: ${state.message}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Server Configuration",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Server URL: http://localhost:5000",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Status: Connected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Button(
                onClick = { viewModel.triggerSync() },
                modifier = Modifier.fillMaxWidth(),
                enabled = syncState !is SyncState.Syncing
            ) {
                Icon(Icons.Default.Sync, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    when (syncState) {
                        is SyncState.Syncing -> "Syncing..."
                        else -> "Sync Now"
                    }
                )
            }

            Text(
                text = "Local-First Design",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "All data is stored locally first. Sync happens in the background to backup your data to the server. You can work offline and sync when you're back online.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}