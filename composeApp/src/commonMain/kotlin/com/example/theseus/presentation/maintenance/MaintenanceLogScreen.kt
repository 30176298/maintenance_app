package com.example.theseus.presentation.maintenance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
//import androidx.compose.runtime.getValue
import com.example.theseus.domain.model.MaintenanceType
import kotlinx.datetime.*
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class,
    kotlin.time.ExperimentalTime::class)
@Composable
fun MaintenanceLogScreen(
    aircraftId: String?,
    onNavigateBack: () -> Unit,
    viewModel: MaintenanceLogViewModel = koinViewModel { parametersOf(aircraftId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (val state = uiState) {
                            is MaintenanceLogUiState.Success -> {
                                state.aircraft?.let { "Maintenance - ${it.registration}" }
                                    ?: "All Maintenance Logs"
                            }
                            else -> "Maintenance Log"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (aircraftId != null) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, "Add Maintenance")
                }
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is MaintenanceLogUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is MaintenanceLogUiState.Success -> {
                if (state.maintenanceEvents.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No maintenance records")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.maintenanceEvents) { event ->
                            MaintenanceEventCard(
                                event = event,
                                showAircraftInfo = aircraftId == null
                            )
                        }
                    }
                }
            }
        }

        if (showAddDialog && aircraftId != null) {
            val aircraft = (uiState as? MaintenanceLogUiState.Success)?.aircraft
            aircraft?.let {
                AddMaintenanceDialog(
                    aircraft = it,
                    onDismiss = { showAddDialog = false },
                    onAdd = { date, hours, cycles, type, desc, tech, cert ->
                        viewModel.addMaintenanceEvent(
                            aircraftId = aircraftId,
                            date = date,
                            hoursAtMaintenance = hours,
                            cyclesAtMaintenance = cycles,
                            maintenanceType = type,
                            description = desc,
                            technicianName = tech,
                            technicianCertification = cert
                        )
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
@OptIn(kotlin.time.ExperimentalTime::class)
fun MaintenanceEventCard(
    event: com.example.theseus.domain.model.MaintenanceEvent,
    showAircraftInfo: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            val date = event.date.toLocalDateTime(TimeZone.currentSystemDefault())

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = event.maintenanceType.displayName(),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${date.date}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Hours: ${event.hoursAtMaintenance.format(1)} | Cycles: ${event.cyclesAtMaintenance}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Technician: ${event.technicianName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            event.technicianCertification?.let {
                Text(
                    text = "Cert: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class,
    kotlin.time.ExperimentalTime::class)
@Composable
fun AddMaintenanceDialog(
    aircraft: com.example.theseus.domain.model.Aircraft,
    onDismiss: () -> Unit,
    onAdd: (Instant, Double, Int, MaintenanceType, String, String, String?) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var technicianName by remember { mutableStateOf("") }
    var technicianCert by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(MaintenanceType.INSPECTION) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Maintenance Event") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Aircraft: ${aircraft.registration}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Current Hours: ${aircraft.totalHours.format(1)}",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedType.displayName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Maintenance Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        MaintenanceType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName()) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = technicianName,
                    onValueChange = { technicianName = it },
                    label = { Text("Technician Name *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = technicianCert,
                    onValueChange = { technicianCert = it },
                    label = { Text("Certification Number") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (technicianName.isNotBlank() && description.isNotBlank()) {
                        onAdd(
                            kotlinx.datetime.Clock.System.now(),
                            aircraft.totalHours,
                            aircraft.totalCycles,
                            selectedType,
                            description.trim(),
                            technicianName.trim(),
                            technicianCert.takeIf { it.isNotBlank() }
                        )
                    }
                },
                enabled = technicianName.isNotBlank() && description.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun Double.format(decimals: Int): String {
    return "%.${decimals}f".format(this)
}
