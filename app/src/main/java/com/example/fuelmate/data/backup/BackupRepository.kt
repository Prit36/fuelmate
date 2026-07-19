package com.example.fuelmate.data.backup

import androidx.room.withTransaction
import com.example.fuelmate.data.local.AppDatabase
import com.example.fuelmate.data.local.entity.FuelEntry
import com.example.fuelmate.data.local.entity.Vehicle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Portable, human-readable backup of all vehicles + fuel entries as JSON.
 *
 * JSON (de)serialization is delegated to the pure [BackupJson] object (no Android deps),
 * so the round-trip is unit-testable on the JVM. Import replaces ALL current data inside a
 * single Room transaction — the caller must show a confirmation dialog before calling [importAll].
 *
 * ponytail: manual JSON (not kotlinx.serialization) to avoid a new dependency for a 2-table
 * schema. If the schema grows complex, swap BackupJson to kotlinx.serialization.
 */
class BackupRepository(
    private val db: AppDatabase
) {
    private val vehicleDao get() = db.vehicleDao()
    private val fuelEntryDao get() = db.fuelEntryDao()

    suspend fun exportAll(): String = withContext(Dispatchers.IO) {
        val vehicles = vehicleDao.observeAll().first()
        val pairs = vehicles.map { v ->
            v to fuelEntryDao.observeForVehicle(v.id).first()
        }
        BackupJson.serialize(pairs)
    }

    /**
     * Replaces all data with the contents of [json]. Runs inside a transaction so a
     * malformed/partial import leaves the DB untouched. Returns the number of vehicles imported.
     */
    suspend fun importAll(json: String): Int = withContext(Dispatchers.IO) {
        val parsed = BackupJson.deserialize(json)

        // Snapshot existing rows outside the transaction, then wipe + insert atomically.
        val existing = vehicleDao.observeAll().first()
        db.withTransaction {
            existing.forEach { vehicleDao.delete(it) }
            for ((vehicle, entries) in parsed) {
                val newId = vehicleDao.insert(vehicle.copy(id = 0L))
                entries.forEach { fuelEntryDao.insert(it.copy(id = 0L, vehicleId = newId)) }
            }
        }
        parsed.size
    }
}
