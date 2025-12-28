package com.example.theseus.di

import com.example.theseus.data.local.DatabaseDriverFactory
import com.example.theseus.data.repository.AircraftRepository
import com.example.theseus.data.repository.MaintenanceRepository
import com.example.theseus.database.TheseusDatabase
import com.example.theseus.presentation.aircraft.AircraftListViewModel
import com.example.theseus.presentation.aircraft.AircraftDetailViewModel
import com.example.theseus.presentation.maintenance.MaintenanceLogViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Database
    single { get<DatabaseDriverFactory>().createDriver() }
    single { TheseusDatabase(get()) }

    // Repositories
    single { AircraftRepository(get()) }
    single { MaintenanceRepository(get()) }

    // ViewModels
    viewModel { AircraftListViewModel(get()) }
    viewModel { (aircraftId: String) -> AircraftDetailViewModel(aircraftId, get(), get()) }
    viewModel { (aircraftId: String?) -> MaintenanceLogViewModel(aircraftId, get(), get()) }
}