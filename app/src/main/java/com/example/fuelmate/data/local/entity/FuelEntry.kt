package com.example.fuelmate.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single fuel-up event. Mileage is derived (distance / liters) and is NOT stored here.
 *
 * @param odometerKm Odometer reading (km) at the time of this fill-up.
 * @param amountPaid Amount paid in INR (₹) for this fill-up.
 * @param liters     Litres of fuel filled.
 * @param date       Epoch millis when the fill-up happened.
 * @param note       Optional free-text note.
 */
@Entity(
    tableName = "fuel_entries",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("vehicleId")]
)
data class FuelEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleId: Long,
    val odometerKm: Double,
    val amountPaid: Double,
    val liters: Double,
    val date: Long,
    val note: String? = null
)