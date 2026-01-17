package com.example.theseus.presentation.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object AircraftList : Screen("aircraft_list")
    data object AircraftDetail : Screen("aircraft_detail/{aircraftId}") {
        fun createRoute(aircraftId: String) = "aircraft_detail/$aircraftId"
    }
    data object AddAircraft : Screen("add_aircraft")
    data object MaintenanceLog : Screen("maintenance_log/{aircraftId}") {
        fun createRoute(aircraftId: String) = "maintenance_log/$aircraftId"
    }
    data object AllMaintenanceLog : Screen("all_maintenance_log")
    data object SyncSettings : Screen("sync_settings")
}