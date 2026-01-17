package com.example.theseus.presentation.sync

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.theseus.data.sync.SyncState
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SyncStatusBar(
    modifier: Modifier = Modifier,
    viewModel: SyncViewModel = koinViewModel()
) {
    val syncState by viewModel.syncState.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()

    AnimatedVisibility(
        visible = syncState !is SyncState.Idle,
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (syncState) {
                    is SyncState.Success -> MaterialTheme.colorScheme.primaryContainer
                    is SyncState.Error -> MaterialTheme.colorScheme.errorContainer
                    is SyncState.Syncing -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (syncState) {
                        is SyncState.Syncing -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Text("Syncing...", style = MaterialTheme.typography.bodyMedium)
                        }
                        is SyncState.Success -> {
                            Icon(
                                Icons.Default.CloudDone,
                                contentDescription = "Synced",
                                modifier = Modifier.size(20.dp)
                            )
                            lastSyncTime?.let { timestamp ->
                                val time = Instant.fromEpochMilliseconds(timestamp)
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                Text(
                                    "Last synced: ${time.time}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        is SyncState.Error -> {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Error",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                (syncState as SyncState.Error).message,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        else -> {}
                    }
                }

                if (syncState !is SyncState.Syncing) {
                    TextButton(onClick = { viewModel.triggerSync() }) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = "Sync",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync")
                    }
                }
            }
        }
    }
}

@Composable
fun SyncIndicator(
    modifier: Modifier = Modifier,
    viewModel: SyncViewModel = koinViewModel()
) {
    val syncState by viewModel.syncState.collectAsState()

    when (syncState) {
        is SyncState.Syncing -> {
            Icon(
                Icons.Default.Sync,
                contentDescription = "Syncing",
                modifier = modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        is SyncState.Success -> {
            Icon(
                Icons.Default.CloudDone,
                contentDescription = "Synced",
                modifier = modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        else -> {
            IconButton(onClick = { viewModel.triggerSync() }) {
                Icon(
                    Icons.Default.CloudOff,
                    contentDescription = "Sync failed - tap to retry",
                    modifier = modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}