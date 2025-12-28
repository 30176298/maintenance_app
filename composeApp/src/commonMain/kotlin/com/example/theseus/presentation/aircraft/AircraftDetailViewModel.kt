package com.example.theseus.presentation.aircraft

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.theseus.data.repository.AircraftRepository
import com.example.theseus.data.repository.MaintenanceRepository
import com.example.theseus.domain.model.Aircraft
import com.example.theseus.domain.model.MaintenanceEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AircraftDetailViewModel(
    private val aircraftId: String,
    private val aircraftRepository: AircraftRepository,
    private val maintenanceRepository: MaintenanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AircraftDetailUiState>(AircraftDetailUiState.Loading)
    val uiState: StateFlow<AircraftDetailUiState> = _uiState.asStateFlow()

    init {
        loadAircraftDetails()
    }

    private fun loadAircraftDetails() {
        viewModelScope.launch {
            val aircraft = aircraftRepository.getAircraftById(aircraftId)
            if (aircraft == null) {
                _uiState.value = AircraftDetailUiState.Error("Aircraft not found")
                return@launch
            }

            maintenanceRepository.getMaintenanceEventsByAircraftId(aircraftId).collect { events ->
                val maintenanceCount = events.size.toLong()
                val latestMaintenance = events.firstOrNull()

                _uiState.value = AircraftDetailUiState.Success(
                    aircraft = aircraft,
                    maintenanceEvents = events,
                    maintenanceCount = maintenanceCount,
                    latestMaintenance = latestMaintenance
                )
            }
        }
    }

    fun saveAircraft(
        registration: String,
        make: String,
        model: String,
        serialNumber: String,
        yearOfManufacture: Int?,
        totalHours: Double,
        totalCycles: Int
    ) {
        viewModelScope.launch {
            val currentAircraft = aircraftRepository.getAircraftById(aircraftId)
            if (currentAircraft != null) {
                val updated = currentAircraft.copy(
                    registration = registration,
                    make = make,
                    model = model,
                    serialNumber = serialNumber,
                    yearOfManufacture = yearOfManufacture,
                    totalHours = totalHours,
                    totalCycles = totalCycles
                )
                aircraftRepository.updateAircraft(updated)
            }
        }
    }
}

sealed class AircraftDetailUiState {
    data object Loading : AircraftDetailUiState()
    data class Error(val message: String) : AircraftDetailUiState()
    data class Success(
        val aircraft: Aircraft,
        val maintenanceEvents: List<MaintenanceEvent>,
        val maintenanceCount: Long,
        val latestMaintenance: MaintenanceEvent?
    ) : AircraftDetailUiState()
}