package com.example.fuelmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fuelmate.data.local.entity.Vehicle
import com.example.fuelmate.data.model.FuelRecord
import com.example.fuelmate.data.model.VehicleStats
import com.example.fuelmate.data.repository.FuelRepository
import com.example.fuelmate.data.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VehicleDetailUiState(
    val vehicle: Vehicle? = null,
    val records: List<FuelRecord> = emptyList(),
    val stats: VehicleStats = VehicleStats(),
    val isLoading: Boolean = true,
    val pendingDelete: FuelRecord? = null
)

class VehicleDetailViewModel(
    private val vehicleRepository: VehicleRepository,
    private val fuelRepository: FuelRepository,
    private val vehicleId: Long
) : ViewModel() {

    // Single reactive stream: the vehicle is exposed as a flow (no manual init launch),
    // then combined with the record/stats flows into one UiState.
    private val vehicle: Flow<Vehicle?> = flow {
        emit(vehicleRepository.getVehicle(vehicleId))
    }

    private val records = fuelRepository.observeRecords(vehicleId)
    private val stats = fuelRepository.observeStats(vehicleId)
    private val pendingDelete = MutableStateFlow<FuelRecord?>(null)

    val uiState: StateFlow<VehicleDetailUiState> =
        combine(vehicle, records, stats, pendingDelete) { v, recs, st, del ->
            VehicleDetailUiState(
                vehicle = v,
                records = recs,
                stats = st,
                isLoading = false,
                pendingDelete = del
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VehicleDetailUiState(isLoading = true)
        )

    fun requestDeleteEntry(record: FuelRecord) {
        pendingDelete.value = record
    }

    fun confirmDeleteEntry() {
        val record = pendingDelete.value ?: return
        viewModelScope.launch {
            fuelRepository.deleteEntry(record.entry)
            pendingDelete.value = null
        }
    }

    fun cancelDeleteEntry() {
        pendingDelete.value = null
    }
}