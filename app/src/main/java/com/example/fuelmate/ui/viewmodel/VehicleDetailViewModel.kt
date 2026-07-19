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

/** Field to sort the fuel history by. */
enum class SortField { DATE, ODOMETER, COST }

data class VehicleDetailUiState(
    val vehicle: Vehicle? = null,
    val records: List<FuelRecord> = emptyList(),
    val stats: VehicleStats = VehicleStats(),
    val isLoading: Boolean = true,
    val pendingDelete: FuelRecord? = null,
    // Search / filter / sort controls for the fuel history list.
    val searchText: String = "",
    val dateFrom: Long? = null,
    val dateTo: Long? = null,
    val sortField: SortField = SortField.DATE,
    val sortAsc: Boolean = false
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

    // Filter/sort controls, held in state so they survive recomposition.
    private val searchText = MutableStateFlow("")
    private val dateFrom = MutableStateFlow<Long?>(null)
    private val dateTo = MutableStateFlow<Long?>(null)
    private val sortField = MutableStateFlow(SortField.DATE)
    private val sortAsc = MutableStateFlow(false)

    // Combine the five filter/sort controls into one state object so the main
    // combine below stays within the 5-source overload limit.
    private val filterState: Flow<VehicleDetailUiState> =
        combine(searchText, dateFrom, dateTo, sortField, sortAsc) { q, from, to, field, asc ->
            VehicleDetailUiState(searchText = q, dateFrom = from, dateTo = to, sortField = field, sortAsc = asc)
        }

    val uiState: StateFlow<VehicleDetailUiState> =
        combine(vehicle, records, stats, pendingDelete, filterState) { v, recs, st, del, f ->
            f.copy(
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

    /** Records after applying the active search text, date range, and sort. */
    val visibleRecords: StateFlow<List<FuelRecord>> =
        combine(records, filterState) { recs, f ->
            recs.filter { matches(it, f.searchText, f.dateFrom, f.dateTo) }
                .let { if (f.sortAsc) it.sortedWith(comp(f.sortField)) else it.sortedWith(comp(f.sortField).reversed()) }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun setSearch(text: String) { searchText.value = text }
    fun setDateRange(from: Long?, to: Long?) { dateFrom.value = from; dateTo.value = to }
    fun setSort(field: SortField) {
        if (sortField.value == field) sortAsc.value = !sortAsc.value
        else { sortField.value = field; sortAsc.value = false }
    }

    private fun matches(r: FuelRecord, q: String, from: Long?, to: Long?): Boolean {
        if (q.isNotBlank() && !(r.entry.note ?: "").contains(q.trim(), ignoreCase = true)) return false
        val d = r.entry.date
        if (from != null && d < from) return false
        if (to != null && d > to) return false
        return true
    }

    private fun comp(field: SortField): Comparator<FuelRecord> = when (field) {
        SortField.DATE -> compareBy { it.entry.date }
        SortField.ODOMETER -> compareBy { it.entry.odometerKm }
        SortField.COST -> compareBy { it.entry.amountPaid }
    }

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