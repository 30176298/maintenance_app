package com.example.theseus.presentation.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.theseus.data.repository.AircraftRepository
import com.example.theseus.data.repository.MaintenanceRepository
import com.example.theseus.domain.model.Aircraft
import com.example.theseus.domain.model.MaintenanceEvent
import com.example.theseus.domain.model.MaintenanceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

@OptIn(kotlin.time.ExperimentalTime::class)
class MaintenanceLogViewModel(
    private val aircraftId: String?,
    private val maintenanceRepository: MaintenanceRepository,
    private val aircraftRepository: AircraftRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MaintenanceLogUiState>(MaintenanceLogUiState.Loading)
    val uiState: StateFlow<MaintenanceLogUiState> = _uiState.asStateFlow()

    init {
        loadMaintenanceLogs()
    }

    private fun loadMaintenanceLogs() {
        viewModelScope.launch {
            if (aircraftId != null) {
                maintenanceRepository.getMaintenanceEventsByAircraftId(aircraftId).collect { events ->
                    val aircraft = aircraftRepository.getAircraftById(aircraftId)
                    _uiState.value = MaintenanceLogUiState.Success(events, aircraft)
                }
            } else {
                maintenanceRepository.getAllMaintenanceEvents().collect { events ->
                    _uiState.value = MaintenanceLogUiState.Success(events, null)
                }
            }
        }
    }

    fun addMaintenanceEvent(
        aircraftId: String,
        date: Instant,
        hoursAtMaintenance: Double,
        cyclesAtMaintenance: Int,
        maintenanceType: MaintenanceType,
        description: String,
        technicianName: String,
        technicianCertification: String?
    ) {
        viewModelScope.launch {
            val event = MaintenanceEvent.create(
                aircraftId = aircraftId,
                date = date,
                hoursAtMaintenance = hoursAtMaintenance,
                cyclesAtMaintenance = cyclesAtMaintenance,
                maintenanceType = maintenanceType,
                description = description,
                technicianName = technicianName,
                technicianCertification = technicianCertification
            )
            maintenanceRepository.insertMaintenanceEvent(event)
        }
    }

    fun deleteMaintenanceEvent(eventId: String) {
        viewModelScope.launch {
            maintenanceRepository.deleteMaintenanceEvent(eventId)
        }
    }
}

sealed class MaintenanceLogUiState {
    data object Loading : MaintenanceLogUiState()
    data class Success(
        val maintenanceEvents: List<MaintenanceEvent>,
        val aircraft: Aircraft?
    ) : MaintenanceLogUiState()
}
