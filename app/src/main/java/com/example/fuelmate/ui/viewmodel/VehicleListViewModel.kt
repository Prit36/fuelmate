package com.example.fuelmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fuelmate.data.local.entity.Vehicle
import com.example.fuelmate.data.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VehicleListUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val isLoading: Boolean = true,
    val isAdding: Boolean = false,
    val isEditing: Boolean = false,
    val editingVehicle: Vehicle? = null,
    val dialogName: String = "",
    val dialogError: String? = null,
    val pendingDelete: Vehicle? = null
)

class VehicleListViewModel(
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val vehicles = vehicleRepository.observeVehicles()
    private val isLoading = MutableStateFlow(true)
    private val isAdding = MutableStateFlow(false)
    private val isEditing = MutableStateFlow(false)
    private val editingVehicle = MutableStateFlow<Vehicle?>(null)
    private val dialogName = MutableStateFlow("")
    private val dialogError = MutableStateFlow<String?>(null)
    private val pendingDelete = MutableStateFlow<Vehicle?>(null)

    init {
        viewModelScope.launch {
            vehicles.first()
            isLoading.value = false
        }
    }

    val uiState: StateFlow<VehicleListUiState> =
        combine(
            combine(vehicles, isLoading, isAdding, isEditing, editingVehicle) { v, load, add, edit, editV ->
                VehicleListUiState(
                    vehicles = v,
                    isLoading = load,
                    isAdding = add,
                    isEditing = edit,
                    editingVehicle = editV
                )
            },
            combine(dialogName, dialogError, pendingDelete) { name, err, del ->
                Triple(name, err, del)
            }
        ) { base, extra ->
            base.copy(
                dialogName = extra.first,
                dialogError = extra.second,
                pendingDelete = extra.third
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VehicleListUiState()
        )

    fun onAddClick() {
        isAdding.value = true
        dialogName.value = ""
        dialogError.value = null
    }

    fun onEditClick(vehicle: Vehicle) {
        isEditing.value = true
        editingVehicle.value = vehicle
        dialogName.value = vehicle.name
        dialogError.value = null
    }

    fun onDismissDialog() {
        isAdding.value = false
        isEditing.value = false
        editingVehicle.value = null
        dialogName.value = ""
        dialogError.value = null
    }

    fun onDialogNameChange(name: String) {
        dialogName.value = name
        dialogError.value = null
    }

    fun confirmDialog() {
        val name = dialogName.value.trim()
        if (name.isBlank()) {
            dialogError.value = "Please enter a name"
            return
        }
        viewModelScope.launch {
            runCatching {
                if (isEditing.value) {
                    val current = editingVehicle.value
                        ?: throw IllegalStateException("No vehicle being edited")
                    vehicleRepository.updateVehicle(current.copy(name = name))
                } else {
                    vehicleRepository.addVehicle(name)
                }
            }.onSuccess {
                onDismissDialog()
            }.onFailure { dialogError.value = it.message ?: "Could not save vehicle" }
        }
    }

    fun requestDelete(vehicle: Vehicle) {
        pendingDelete.value = vehicle
    }

    fun confirmDelete() {
        val vehicle = pendingDelete.value ?: return
        viewModelScope.launch {
            vehicleRepository.deleteVehicle(vehicle)
            pendingDelete.value = null
        }
    }

    fun cancelDelete() {
        pendingDelete.value = null
    }
}