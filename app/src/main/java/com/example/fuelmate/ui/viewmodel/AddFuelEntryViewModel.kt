package com.example.fuelmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fuelmate.data.repository.FuelRepository
import com.example.fuelmate.ui.util.formatNum
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddFuelEntryUiState(
    val odometer: String = "",
    val amount: String = "",
    val liters: String = "",
    val note: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val minOdometer: Double = 0.0,
    val isSaving: Boolean = false,
    val error: String? = null
)

/** One-shot events the screen reacts to (e.g. navigation), consumed via a Channel. */
sealed interface AddFuelEntryEvent {
    data object Saved : AddFuelEntryEvent
}

class AddFuelEntryViewModel(
    private val fuelRepository: FuelRepository,
    private val vehicleId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(AddFuelEntryUiState())

    val uiState: StateFlow<AddFuelEntryUiState> = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AddFuelEntryUiState()
        )

    // One-shot events (e.g. navigate back after a successful save).
    private val _events = Channel<AddFuelEntryEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val latest = fuelRepository.getLatest(vehicleId)
            _state.value = _state.value.copy(
                odometer = latest?.odometerKm?.let { if (it > 0) it.toString() else "" } ?: "",
                minOdometer = latest?.odometerKm ?: 0.0
            )
        }
    }

    fun onOdometerChange(value: String) = update { it.copy(odometer = value, error = null) }
    fun onAmountChange(value: String) = update { it.copy(amount = value, error = null) }
    fun onLitersChange(value: String) = update { it.copy(liters = value, error = null) }
    fun onNoteChange(value: String) = update { it.copy(note = value) }
    fun onDateChange(millis: Long) = update { it.copy(dateMillis = millis) }

    fun save() {
        val s = _state.value
        val odo = s.odometer.toDoubleOrNull()
        val amt = s.amount.toDoubleOrNull()
        val lit = s.liters.toDoubleOrNull()

        when {
            odo == null -> return update { it.copy(error = "Enter a valid odometer reading") }
            odo < s.minOdometer -> return update {
                it.copy(error = "Odometer must be >= ${formatNum(s.minOdometer)} km")
            }
            amt == null || amt < 0 -> return update { it.copy(error = "Enter a valid amount (Rs)") }
            lit == null || lit <= 0 -> return update { it.copy(error = "Enter litres filled (> 0)") }
        }

        _state.value = s.copy(isSaving = true, error = null)
        viewModelScope.launch {
            runCatching {
                fuelRepository.addEntry(
                    vehicleId = vehicleId,
                    odometerKm = odo,
                    amountPaid = amt,
                    liters = lit,
                    date = s.dateMillis,
                    note = s.note
                )
            }.onSuccess {
                _state.value = _state.value.copy(isSaving = false)
                _events.trySend(AddFuelEntryEvent.Saved)
            }.onFailure {
                _state.value = _state.value.copy(isSaving = false, error = it.message ?: "Could not save")
            }
        }
    }

    private fun update(transform: (AddFuelEntryUiState) -> AddFuelEntryUiState) {
        _state.value = transform(_state.value)
    }
}