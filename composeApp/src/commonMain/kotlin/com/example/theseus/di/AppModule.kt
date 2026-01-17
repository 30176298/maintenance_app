package com.example.theseus.di

import com.example.theseus.data.local.DatabaseDriverFactory
import com.example.theseus.data.local.getNativeHttpClient
import com.example.theseus.data.repository.AircraftRepository
import com.example.theseus.data.repository.MaintenanceRepository
import com.example.theseus.database.TheseusDatabase
import com.example.theseus.presentation.aircraft.AircraftListViewModel
import com.example.theseus.presentation.aircraft.AircraftDetailViewModel
import com.example.theseus.presentation.maintenance.MaintenanceLogViewModel
import com.example.theseus.presentation.sync.SyncViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.example.theseus.data.remote.TheseusApiClient
import com.example.theseus.data.sync.SyncManager

val appModule = module {
    // Database
    single { get<DatabaseDriverFactory>().createDriver() }
    single { TheseusDatabase(get()) }

    // Repositories
    single { AircraftRepository(get()) }
    single { MaintenanceRepository(get()) }

    // HTTP Client (platform-specific)
    single { getNativeHttpClient() }

    // ViewModels
    viewModel { AircraftListViewModel(get()) }
    viewModel { (aircraftId: String) -> AircraftDetailViewModel(aircraftId, get(), get()) }
    viewModel { (aircraftId: String?) -> MaintenanceLogViewModel(aircraftId, get(), get()) }
    viewModel { SyncViewModel(get()) }

    // API Client
    single {
        TheseusApiClient(
            httpClient = getNativeHttpClient(),
            apiKey = "api_warehouse_student_key_1234567890abcdef"
        )
    }

    // Sync Manager
    single { SyncManager(get(), get()) }
}