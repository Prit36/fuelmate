package com.example.fuelmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A vehicle (e.g. "My Scooty") that the user tracks fuel-ups for.
 */
@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)