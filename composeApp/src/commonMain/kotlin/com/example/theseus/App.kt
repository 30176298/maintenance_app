package com.example.theseus

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.theseus.presentation.aircraft.AircraftListScreen
import com.example.theseus.presentation.dashboard.DashboardScreen
import com.example.theseus.presentation.navigation.Screen
import com.example.theseus.presentation.sync.SyncSettingsScreen

@Composable
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
        var aircraftIdParam by remember { mutableStateOf<String?>(null) }

        TheseusNavigation(
            currentScreen = currentScreen,
            aircraftIdParam = aircraftIdParam,
            onNavigate = { screen, param ->
                currentScreen = screen
                aircraftIdParam = param
            }
        )
    }
}

@Composable
fun TheseusNavigation(
    currentScreen: Screen,
    aircraftIdParam: String?,
    onNavigate: (Screen, String?) -> Unit
) {
    when (currentScreen) {
        Screen.Dashboard -> DashboardScreen(
            onNavigateToAircraftList = { onNavigate(Screen.AircraftList, null) },
            onNavigateToAircraft = { id -> onNavigate(Screen.AircraftDetail, id) },
            onNavigateToSyncSettings = { onNavigate(Screen.SyncSettings, null) }
        )
        Screen.AircraftList -> AircraftListScreen(
            onNavigateBack = { onNavigate(Screen.Dashboard, null) },
            onNavigateToDetail = { id -> onNavigate(Screen.AircraftDetail, id) },
            onNavigateToAdd = { onNavigate(Screen.AddAircraft, null) }
        )
        Screen.AircraftDetail -> {
            aircraftIdParam?.let { id ->
                com.example.theseus.presentation.aircraft.AircraftDetailScreen(
                    aircraftId = id,
                    onNavigateBack = { onNavigate(Screen.AircraftList, null) },
                    onNavigateToMaintenance = { onNavigate(Screen.MaintenanceLog, id) }
                )
            }
        }
        Screen.AddAircraft -> {
            com.example.theseus.presentation.aircraft.AddAircraftScreen(
                onNavigateBack = { onNavigate(Screen.AircraftList, null) },
                onAircraftAdded = { id -> onNavigate(Screen.AircraftDetail, id) }
            )
        }
        Screen.MaintenanceLog -> {
            aircraftIdParam?.let { id ->
                com.example.theseus.presentation.maintenance.MaintenanceLogScreen(
                    aircraftId = id,
                    onNavigateBack = { onNavigate(Screen.AircraftDetail, id) }
                )
            }
        }
        Screen.AllMaintenanceLog -> {
            com.example.theseus.presentation.maintenance.MaintenanceLogScreen(
                aircraftId = null,
                onNavigateBack = { onNavigate(Screen.Dashboard, null) }
            )
        }
        Screen.SyncSettings -> {
            SyncSettingsScreen(
                onNavigateBack = { onNavigate(Screen.Dashboard, null) }
            )
        }
    }
}