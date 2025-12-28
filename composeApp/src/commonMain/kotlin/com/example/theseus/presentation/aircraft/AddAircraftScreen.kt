package com.example.theseus.presentation.aircraft

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.theseus.data.repository.AircraftRepository
import com.example.theseus.domain.model.Aircraft
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAircraftScreen(
    onNavigateBack: () -> Unit,
    onAircraftAdded: (String) -> Unit,
    aircraftRepository: AircraftRepository = koinInject()
) {
    var registration by remember { mutableStateOf("") }
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var yearOfManufacture by remember { mutableStateOf("") }
    var totalHours by remember { mutableStateOf("0.0") }
    var totalCycles by remember { mutableStateOf("0") }

    var registrationError by remember { mutableStateOf(false) }
    var makeError by remember { mutableStateOf(false) }
    var modelError by remember { mutableStateOf(false) }
    var serialNumberError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Aircraft") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Aircraft Information",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = registration,
                onValueChange = {
                    registration = it.uppercase()
                    registrationError = false
                },
                label = { Text("Registration *") },
                isError = registrationError,
                supportingText = if (registrationError) {
                    { Text("Registration is required") }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = make,
                onValueChange = {
                    make = it
                    makeError = false
                },
                label = { Text("Make *") },
                isError = makeError,
                supportingText = if (makeError) {
                    { Text("Make is required") }
                } else null,
                placeholder = { Text("e.g., Cessna, Boeing") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = model,
                onValueChange = {
                    model = it
                    modelError = false
                },
                label = { Text("Model *") },
                isError = modelError,
                supportingText = if (modelError) {
                    { Text("Model is required") }
                } else null,
                placeholder = { Text("e.g., 172, 737-800") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = serialNumber,
                onValueChange = {
                    serialNumber = it
                    serialNumberError = false
                },
                label = { Text("Serial Number *") },
                isError = serialNumberError,
                supportingText = if (serialNumberError) {
                    { Text("Serial number is required") }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = yearOfManufacture,
                onValueChange = { yearOfManufacture = it.filter { c -> c.isDigit() } },
                label = { Text("Year of Manufacture") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("e.g., 2020") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = totalHours,
                onValueChange = { totalHours = it },
                label = { Text("Total Airframe Hours") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = totalCycles,
                onValueChange = { totalCycles = it.filter { c -> c.isDigit() } },
                label = { Text("Total Cycles") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    var hasError = false

                    if (registration.isBlank()) {
                        registrationError = true
                        hasError = true
                    }
                    if (make.isBlank()) {
                        makeError = true
                        hasError = true
                    }
                    if (model.isBlank()) {
                        modelError = true
                        hasError = true
                    }
                    if (serialNumber.isBlank()) {
                        serialNumberError = true
                        hasError = true
                    }

                    if (!hasError) {
                        scope.launch {
                            try {
                                val aircraft = Aircraft.create(
                                    registration = registration.trim(),
                                    make = make.trim(),
                                    model = model.trim(),
                                    serialNumber = serialNumber.trim(),
                                    yearOfManufacture = yearOfManufacture.toIntOrNull(),
                                    totalHours = totalHours.toDoubleOrNull() ?: 0.0,
                                    totalCycles = totalCycles.toIntOrNull() ?: 0
                                )
                                aircraftRepository.insertAircraft(aircraft)
                                onAircraftAdded(aircraft.id)
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    message = "Error adding aircraft: ${e.message}",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Aircraft")
            }

            Text(
                text = "* Required fields",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
