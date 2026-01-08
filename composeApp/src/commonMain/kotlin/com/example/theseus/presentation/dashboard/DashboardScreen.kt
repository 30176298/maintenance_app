package com.example.theseus.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.example.theseus.data.local.getNativeHttpClient
import com.example.theseus.domain.model.Aircraft
import com.example.theseus.presentation.aircraft.AircraftListUiState
import com.example.theseus.presentation.aircraft.AircraftListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAircraftList: () -> Unit,
    onNavigateToAircraft: (String) -> Unit,
    viewModel: AircraftListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var responseText by remember { mutableStateOf("Ready") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theseus - Aircraft Maintenance") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Fleet Overview",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    when (uiState) {
                        is AircraftListUiState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                        is AircraftListUiState.Empty -> {
                            Text(
                                text = "No aircraft registered yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        is AircraftListUiState.Success -> {
                            val aircraft = (uiState as AircraftListUiState.Success).aircraft
                            Text(
                                text = "Total Aircraft: ${aircraft.size}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Total Fleet Hours: ${aircraft.sumOf { it.totalHours }.format(1)}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onNavigateToAircraftList,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("View All Aircraft")
            }

            Button(
                onClick = {
                    Post_test(scope) { result ->
                        responseText = result
                        isLoading = false
                    }
                    isLoading = true
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(responseText)
            }


            if (uiState is AircraftListUiState.Success) {
                Text(
                    text = "Recent Aircraft",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                val aircraft = (uiState as AircraftListUiState.Success).aircraft
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(aircraft.take(5)) { aircraft ->
                        AircraftSummaryCard(
                            aircraft = aircraft,
                            onClick = { onNavigateToAircraft(aircraft.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AircraftSummaryCard(
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
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${aircraft.make} ${aircraft.model}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Total Hours: ${aircraft.totalHours.format(1)} | Cycles: ${aircraft.totalCycles}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun Double.format(decimals: Int): String {
    return "%.${decimals}f".format(this)
}


fun Post_test(scope: CoroutineScope, onResult: (String) -> Unit) : Unit {
    scope.launch {
        val client = getNativeHttpClient();
        try {
            val _headers = mapOf(
                "X-API-Key" to "api_warehouse_student_key_1234567890abcdef"
            )

            val result = client.post(
                url = "/api/v1/users",
                body = """{"username":"user2","password":"secret","role":"manager"}""",
                headers = _headers
            )
            onResult("Response: $result")
        } catch (e: Exception) {
            onResult("Error: ${e.message}")
        }
    }
}
