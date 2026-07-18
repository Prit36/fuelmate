package com.example.fuelmate.data.repository.impl

import com.example.fuelmate.data.MileageCalculator
import com.example.fuelmate.data.local.dao.FuelEntryDao
import com.example.fuelmate.data.local.entity.FuelEntry
import com.example.fuelmate.data.model.FuelRecord
import com.example.fuelmate.data.model.VehicleStats
import com.example.fuelmate.data.repository.FuelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FuelRepositoryImpl(
    private val fuelEntryDao: FuelEntryDao
) : FuelRepository {

    override fun observeRecords(vehicleId: Long): Flow<List<FuelRecord>> =
        fuelEntryDao.observeForVehicle(vehicleId).map { entries ->
            MileageCalculator.toRecords(entries)
        }

    override fun observeStats(vehicleId: Long): Flow<VehicleStats> =
        fuelEntryDao.observeForVehicle(vehicleId).map { entries ->
            MileageCalculator.computeStats(entries)
        }

    override suspend fun getLatest(vehicleId: Long): FuelEntry? =
        fuelEntryDao.getLatestForVehicle(vehicleId)

    override suspend fun getEntry(id: Long): FuelEntry? =
        fuelEntryDao.getById(id)

    override suspend fun addEntry(
        vehicleId: Long,
        odometerKm: Double,
        amountPaid: Double,
        liters: Double,
        date: Long,
        note: String?
    ) {
        require(odometerKm >= 0) { "Odometer cannot be negative" }
        require(amountPaid >= 0) { "Amount cannot be negative" }
        require(liters > 0) { "Liters must be greater than zero" }
        fuelEntryDao.insert(
            FuelEntry(
                vehicleId = vehicleId,
                odometerKm = odometerKm,
                amountPaid = amountPaid,
                liters = liters,
                date = date,
                note = note?.trim()?.ifEmpty { null }
            )
        )
    }

    override suspend fun updateEntry(entry: FuelEntry) = fuelEntryDao.update(entry)

    override suspend fun deleteEntry(entry: FuelEntry) = fuelEntryDao.delete(entry)
}