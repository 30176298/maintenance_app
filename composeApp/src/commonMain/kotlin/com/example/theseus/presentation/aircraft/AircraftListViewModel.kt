package com.example.theseus.presentation.aircraft

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.theseus.data.repository.AircraftRepository
import com.example.theseus.domain.model.Aircraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AircraftListViewModel(
    private val aircraftRepository: AircraftRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AircraftListUiState>(AircraftListUiState.Loading)
    val uiState: StateFlow<AircraftListUiState> = _uiState.asStateFlow()

    init {
        loadAircraft()
    }

    private fun loadAircraft() {
        viewModelScope.launch {
            aircraftRepository.getAllAircraft().collect { aircraftList ->
                _uiState.value = if (aircraftList.isEmpty()) {
                    AircraftListUiState.Empty
                } else {
                    AircraftListUiState.Success(aircraftList)
                }
            }
        }
    }

    fun deleteAircraft(aircraftId: String) {
        viewModelScope.launch {
            aircraftRepository.deleteAircraft(aircraftId)
        }
    }
}

sealed class AircraftListUiState {
    data object Loading : AircraftListUiState()
    data object Empty : AircraftListUiState()
    data class Success(val aircraft: List<Aircraft>) : AircraftListUiState()
}