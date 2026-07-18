package com.example.fuelmate.data.model

import com.example.fuelmate.data.local.entity.FuelEntry

/**
 * A fuel entry enriched with derived per-fill mileage.
 *
 * @param mileageKmPerLitre Distance since the previous fill-up divided by litres filled.
 *                          `null` for the very first entry (no previous reading to diff against).
 */
data class FuelRecord(
    val entry: FuelEntry,
    val mileageKmPerLitre: Double?
)

/**
 * Aggregate statistics for a vehicle, derived from its fuel entries.
 */
data class VehicleStats(
    val totalDistanceKm: Double = 0.0,
    val totalLiters: Double = 0.0,
    val totalSpent: Double = 0.0,
    val averageMileage: Double? = null,   // totalDistance / totalLiters (excludes first fill's unknown distance)
    val costPerKm: Double? = null,        // totalSpent / totalDistance
    val fillCount: Int = 0
)