package com.example.theseus.presentation.aircraft

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AircraftDetailScreen(
    aircraftId: String,
    onNavigateBack: () -> Unit,
    onNavigateToMaintenance: () -> Unit,
    viewModel: AircraftDetailViewModel = koinViewModel { parametersOf(aircraftId) }
) {
    val uiState by viewModel.uiState. collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aircraft Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToMaintenance) {
                Icon(Icons.Default.Build, "Maintenance Log")
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is AircraftDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is AircraftDetailUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message)
                }
            }
            is AircraftDetailUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        AircraftInfoCard(state.aircraft)
                    }

                    item {
                        MaintenanceSummaryCard(
                            maintenanceCount = state.maintenanceCount,
                            latestMaintenance = state.latestMaintenance
                        )
                    }

                    item {
                        Text(
                            text = "Recent Maintenance",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    if (state.maintenanceEvents.isEmpty()) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No maintenance records")
                                }
                            }
                        }
                    } else {
                        items(state.maintenanceEvents.take(5)) { event ->
                            MaintenanceEventCard(event)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AircraftInfoCard(aircraft: com.example.theseus.domain.model.Aircraft) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = aircraft.registration,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "${aircraft.make} ${aircraft.model}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow("Serial Number", aircraft.serialNumber)
            aircraft.yearOfManufacture?.let {
                InfoRow("Year", it.toString())
            }
            InfoRow("Total Hours", "${aircraft.totalHours.format(1)} hrs")
            InfoRow("Total Cycles", "${aircraft.totalCycles}")
        }
    }
}

@Composable
@OptIn(kotlin.time.ExperimentalTime::class)
fun MaintenanceSummaryCard(
    maintenanceCount: Long,
    latestMaintenance: com.example.theseus.domain.model.MaintenanceEvent?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Maintenance Summary",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("Total Events", maintenanceCount.toString())
            latestMaintenance?.let { event ->
                val date = event.date.toLocalDateTime(TimeZone.currentSystemDefault())
                InfoRow("Last Maintenance", "${date.date}")
                InfoRow("Type", event.maintenanceType.displayName())
            } ?: run {
                Text("No maintenance records", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
@OptIn(kotlin.time.ExperimentalTime::class)
fun MaintenanceEventCard(event: com.example.theseus.domain.model.MaintenanceEvent) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            val date = event.date.toLocalDateTime(TimeZone.currentSystemDefault())
            Text(
                text = event.maintenanceType.displayName(),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${date.date}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hours: ${event.hoursAtMaintenance.format(1)} | Technician: ${event.technicianName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
