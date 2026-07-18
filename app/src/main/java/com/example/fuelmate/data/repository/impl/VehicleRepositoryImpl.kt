package com.example.fuelmate.data.repository.impl

import com.example.fuelmate.data.local.dao.VehicleDao
import com.example.fuelmate.data.local.entity.Vehicle
import com.example.fuelmate.data.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow

class VehicleRepositoryImpl(
    private val vehicleDao: VehicleDao
) : VehicleRepository {

    override fun observeVehicles(): Flow<List<Vehicle>> = vehicleDao.observeAll()

    override suspend fun getVehicle(id: Long): Vehicle? = vehicleDao.getById(id)

    override suspend fun addVehicle(name: String): Long {
        val trimmed = name.trim()
        require(trimmed.isNotBlank()) { "Vehicle name must not be blank" }
        return vehicleDao.insert(Vehicle(name = trimmed))
    }

    override suspend fun deleteVehicle(vehicle: Vehicle) = vehicleDao.delete(vehicle)
}