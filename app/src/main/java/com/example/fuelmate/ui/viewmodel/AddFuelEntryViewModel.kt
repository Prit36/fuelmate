package com.example.fuelmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fuelmate.data.local.entity.FuelEntry
import com.example.fuelmate.data.repository.FuelRepository
import com.example.fuelmate.ui.util.formatNum
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class AddFuelEntryUiState(
    val odometer: String = "",
    val amount: String = "",
    val liters: String = "",
    val note: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val minOdometer: Double = 0.0,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val odometerError: String? = null,
    val amountError: String? = null,
    val litersError: String? = null
)

/** One-shot events the screen reacts to (e.g. navigation), consumed via a Channel. */
sealed interface AddFuelEntryEvent {
    data object Saved : AddFuelEntryEvent
}

class AddFuelEntryViewModel(
    private val fuelRepository: FuelRepository,
    private val vehicleId: Long,
    private val entryId: Long? = null
) : ViewModel() {

    private val _state = MutableStateFlow(AddFuelEntryUiState())

    // Expose the underlying StateFlow directly so the screen always observes the
    // latest value (including prefilled edit data) without any stateIn replay gaps.
    val uiState: StateFlow<AddFuelEntryUiState> = _state

    // One-shot events (e.g. navigate back after a successful save).
    private val _events = Channel<AddFuelEntryEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val isEditing: Boolean get() = entryId != null

    // Most recent entry for this vehicle, used to offer a "same as last" quick-fill.
    private var latest: FuelEntry? = null

    init {
        viewModelScope.launch {
            if (entryId != null) {
                // Editing an existing entry: load it and prefill the form.
                _state.value = _state.value.copy(isLoading = true)
                val entry = fuelRepository.getEntry(entryId)
                _state.value = _state.value.copy(isLoading = false)
                if (entry != null) {
                    _state.value = _state.value.copy(
                        odometer = entry.odometerKm.toString(),
                        amount = entry.amountPaid.toString(),
                        liters = entry.liters.toString(),
                        note = entry.note ?: "",
                        dateMillis = entry.date,
                        minOdometer = 0.0
                    )
                }
            } else {
                latest = fuelRepository.getLatest(vehicleId)
                _state.value = _state.value.copy(
                    odometer = latest?.odometerKm?.let { if (it > 0) it.toString() else "" } ?: "",
                    minOdometer = latest?.odometerKm ?: 0.0
                )
            }
        }
    }

    /** True when a previous entry exists to copy values from (add mode only). */
    val hasLatest: Boolean get() = !isEditing && latest != null

    /** One-tap quick-fill: copy liters, amount, and note from the most recent entry. */
    fun fillFromLatest() {
        val l = latest ?: return
        update {
            it.copy(
                liters = l.liters.toString(),
                amount = l.amountPaid.toString(),
                note = l.note ?: "",
                litersError = null,
                amountError = null
            )
        }
    }

    fun onOdometerChange(value: String) = update { it.copy(odometer = value, odometerError = null) }
    fun onAmountChange(value: String) = update { it.copy(amount = value, amountError = null) }
    fun onLitersChange(value: String) = update { it.copy(liters = value, litersError = null) }
    fun onNoteChange(value: String) = update { it.copy(note = value) }
    fun onDateChange(millis: Long) = update { it.copy(dateMillis = millis) }

    fun save() {
        val s = _state.value
        val odo = s.odometer.toDoubleOrNull()
        val amt = s.amount.toDoubleOrNull()
        val lit = s.liters.toDoubleOrNull()

        val odoErr = when {
            odo == null -> "Enter a valid odometer reading"
            odo < s.minOdometer -> "Odometer must be >= ${formatNum(s.minOdometer)} km"
            else -> null
        }
        val amtErr = when {
            amt == null || amt < 0 -> "Enter a valid amount (Rs)"
            else -> null
        }
        val litErr = when {
            lit == null || lit <= 0 -> "Enter litres filled (> 0)"
            else -> null
        }

        if (odoErr != null || amtErr != null || litErr != null) {
            return update {
                it.copy(odometerError = odoErr, amountError = amtErr, litersError = litErr)
            }
        }

        // At this point all three are non-null (errors would have returned above).
        val finalOdo = odo!!
        val finalAmt = amt!!
        val finalLit = lit!!

        _state.value = s.copy(isSaving = true)
        viewModelScope.launch {
            runCatching {
                if (entryId != null) {
                    val existing = fuelRepository.getEntry(entryId)
                    if (existing != null) {
                        fuelRepository.updateEntry(
                            existing.copy(
                                odometerKm = finalOdo,
                                amountPaid = finalAmt,
                                liters = finalLit,
                                date = s.dateMillis,
                                note = s.note.trim().ifEmpty { null }
                            )
                        )
                    }
                } else {
                    fuelRepository.addEntry(
                        vehicleId = vehicleId,
                        odometerKm = finalOdo,
                        amountPaid = finalAmt,
                        liters = finalLit,
                        date = s.dateMillis,
                        note = s.note
                    )
                }
            }.onSuccess {
                _state.value = _state.value.copy(isSaving = false)
                _events.trySend(AddFuelEntryEvent.Saved)
            }.onFailure {
                _state.value = _state.value.copy(isSaving = false)
            }
        }
    }

    private fun update(transform: (AddFuelEntryUiState) -> AddFuelEntryUiState) {
        _state.value = transform(_state.value)
    }
}