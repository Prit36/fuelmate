package com.example.fuelmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fuelmate.data.local.entity.Vehicle
import com.example.fuelmate.data.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VehicleListUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val isAdding: Boolean = false,
    val newVehicleName: String = "",
    val dialogError: String? = null
)

class VehicleListViewModel(
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val vehicles = vehicleRepository.observeVehicles()
    private val isAdding = MutableStateFlow(false)
    private val newVehicleName = MutableStateFlow("")
    private val dialogError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<VehicleListUiState> =
        combine(vehicles, isAdding, newVehicleName, dialogError) { v, add, name, err ->
            VehicleListUiState(
                vehicles = v,
                isAdding = add,
                newVehicleName = name,
                dialogError = err
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VehicleListUiState()
        )

    fun onAddClick() {
        isAdding.value = true
        dialogError.value = null
    }

    fun onDismissAdd() {
        isAdding.value = false
        newVehicleName.value = ""
        dialogError.value = null
    }

    fun onNewNameChange(name: String) {
        newVehicleName.value = name
        dialogError.value = null
    }

    fun confirmAdd() {
        val name = newVehicleName.value.trim()
        if (name.isBlank()) {
            dialogError.value = "Please enter a name"
            return
        }
        viewModelScope.launch {
            runCatching { vehicleRepository.addVehicle(name) }
                .onSuccess {
                    isAdding.value = false
                    newVehicleName.value = ""
                    dialogError.value = null
                }
                .onFailure { dialogError.value = it.message ?: "Could not add vehicle" }
        }
    }

    fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch { vehicleRepository.deleteVehicle(vehicle) }
    }
}