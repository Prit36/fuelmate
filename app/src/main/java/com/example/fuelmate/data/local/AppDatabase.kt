package com.example.fuelmate.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.fuelmate.data.local.dao.FuelEntryDao
import com.example.fuelmate.data.local.dao.VehicleDao
import com.example.fuelmate.data.local.entity.FuelEntry
import com.example.fuelmate.data.local.entity.Vehicle

/**
 * exportSchema = true persists schema history under `schemas/` so Room can verify
 * AutoMigrations at build time (modern best practice with Room 2.8+).
 */
@Database(
    entities = [Vehicle::class, FuelEntry::class],
    version = 1,
    exportSchema = true,
    autoMigrations = [
        // Add future AutoMigration entries here as the version increases, e.g.
        // AutoMigration(from = 1, to = 2)
    ]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao
    abstract fun fuelEntryDao(): FuelEntryDao

    companion object {
        const val DATABASE_NAME = "fuelmate.db"
    }
}