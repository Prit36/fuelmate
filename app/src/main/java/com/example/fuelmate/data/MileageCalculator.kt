package com.example.fuelmate.data

import com.example.fuelmate.data.local.entity.FuelEntry
import com.example.fuelmate.data.model.FuelRecord
import com.example.fuelmate.data.model.VehicleStats

/**
 * Pure functions that derive mileage and aggregate stats from raw fuel entries.
 * Entries are expected to be ordered oldest -> newest.
 */
object MileageCalculator {

    /**
     * Enriches each entry with its per-fill mileage.
     * The first entry has no previous odometer to diff against, so its mileage is null.
     */
    fun toRecords(entries: List<FuelEntry>): List<FuelRecord> {
        return entries.mapIndexed { index, entry ->
            val mileage = if (index == 0) {
                null
            } else {
                val previous = entries[index - 1]
                val distance = entry.odometerKm - previous.odometerKm
                if (entry.liters > 0.0 && distance >= 0.0) distance / entry.liters else null
            }
            FuelRecord(entry = entry, mileageKmPerLitre = mileage)
        }
    }

    /**
     * Computes aggregate stats. The first fill's odometer is treated as the baseline,
     * so total distance = last odometer - first odometer. Its litres are still counted
     * in totalLiters/totalSpent (you did pay for that fuel), but the "unknown distance"
     * portion is not used to inflate the average.
     */
    fun computeStats(entries: List<FuelEntry>): VehicleStats {
        if (entries.isEmpty()) return VehicleStats()

        val totalLiters = entries.sumOf { it.liters }
        val totalSpent = entries.sumOf { it.amountPaid }
        val totalDistanceKm = (entries.last().odometerKm - entries.first().odometerKm).coerceAtLeast(0.0)
        val averageMileage = if (totalLiters > 0.0) totalDistanceKm / totalLiters else null
        val costPerKm = if (totalDistanceKm > 0.0) totalSpent / totalDistanceKm else null

        return VehicleStats(
            totalDistanceKm = totalDistanceKm,
            totalLiters = totalLiters,
            totalSpent = totalSpent,
            averageMileage = averageMileage,
            costPerKm = costPerKm,
            fillCount = entries.size
        )
    }
}