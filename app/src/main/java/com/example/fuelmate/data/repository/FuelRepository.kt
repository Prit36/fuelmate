package com.example.fuelmate.data.repository

import com.example.fuelmate.data.local.entity.FuelEntry
import com.example.fuelmate.data.model.FuelRecord
import com.example.fuelmate.data.model.VehicleStats
import kotlinx.coroutines.flow.Flow

interface FuelRepository {
    /** Emits the list of fuel entries for a vehicle, enriched with per-fill mileage. */
    fun observeRecords(vehicleId: Long): Flow<List<FuelRecord>>

    /** Emits aggregate stats for a vehicle, recomputed whenever its entries change. */
    fun observeStats(vehicleId: Long): Flow<VehicleStats>

    /** Returns the most recent entry for a vehicle (used to prefill/validate the next fill-up). */
    suspend fun getLatest(vehicleId: Long): FuelEntry?

    /** Returns a single entry by id (used when editing). */
    suspend fun getEntry(id: Long): FuelEntry?

    /** Adds a new fuel-up. */
    suspend fun addEntry(
        vehicleId: Long,
        odometerKm: Double,
        amountPaid: Double,
        liters: Double,
        date: Long,
        note: String?
    )

    /** Updates an existing fuel-up. */
    suspend fun updateEntry(entry: FuelEntry)

    /** Deletes a fuel-up. */
    suspend fun deleteEntry(entry: FuelEntry)
}