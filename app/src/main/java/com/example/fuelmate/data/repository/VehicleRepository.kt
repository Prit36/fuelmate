package com.example.fuelmate.data.repository

import com.example.fuelmate.data.local.entity.Vehicle
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun observeVehicles(): Flow<List<Vehicle>>
    suspend fun getVehicle(id: Long): Vehicle?
    suspend fun addVehicle(name: String): Long
    suspend fun updateVehicle(vehicle: Vehicle)
    suspend fun deleteVehicle(vehicle: Vehicle)
}