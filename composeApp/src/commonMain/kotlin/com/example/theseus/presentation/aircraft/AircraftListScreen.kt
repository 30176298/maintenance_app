package com.example.theseus.presentation.aircraft

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.theseus.domain.model.Aircraft
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AircraftListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAdd: () -> Unit,
    viewModel: AircraftListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aircraft Fleet") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, "Add Aircraft")
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is AircraftListUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is AircraftListUiState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No aircraft registered",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tap + to add your first aircraft")
                    }
                }
            }
            is AircraftListUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.aircraft) { aircraft ->
                        AircraftCard(
                            aircraft = aircraft,
                            onClick = { onNavigateToDetail(aircraft.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AircraftCard(
    aircraft: Aircraft,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = aircraft.registration,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "${aircraft.make} ${aircraft.model}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "S/N: ${aircraft.serialNumber}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    aircraft.yearOfManufacture?.let {
                        Text(
                            text = "Year: $it",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${aircraft.totalHours.format(1)} hrs",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${aircraft.totalCycles} cycles",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

fun Double.format(decimals: Int): String {
    return "%.${decimals}f".format(this)
}