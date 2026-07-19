package com.example.fuelmate.data.backup

import com.example.fuelmate.data.local.entity.FuelEntry
import com.example.fuelmate.data.local.entity.Vehicle
import org.json.JSONArray
import org.json.JSONObject

/**
 * Pure (no Android, no Room) (de)serialization of the backup document.
 * Kept separate from [BackupRepository] so it is trivially unit-testable on the JVM.
 */
object BackupJson {

    fun serialize(vehicles: List<Pair<Vehicle, List<FuelEntry>>>): String {
        val root = JSONObject()
        root.put("app", "FuelMate")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val vehiclesArr = JSONArray()
        for ((v, entries) in vehicles) {
            val vObj = JSONObject()
            vObj.put("id", v.id)
            vObj.put("name", v.name)
            vObj.put("createdAt", v.createdAt)

            val entriesArr = JSONArray()
            for (e in entries) {
                val eObj = JSONObject()
                eObj.put("id", e.id)
                eObj.put("vehicleId", e.vehicleId)
                eObj.put("odometerKm", e.odometerKm)
                eObj.put("amountPaid", e.amountPaid)
                eObj.put("liters", e.liters)
                eObj.put("date", e.date)
                eObj.put("note", e.note)
                entriesArr.put(eObj)
            }
            vObj.put("entries", entriesArr)
            vehiclesArr.put(vObj)
        }
        root.put("vehicles", vehiclesArr)
        return root.toString(2)
    }

    /** Parses a backup document. Throws [org.json.JSONException] on malformed input. */
    fun deserialize(json: String): List<Pair<Vehicle, List<FuelEntry>>> {
        val root = JSONObject(json)
        val vehiclesArr = root.getJSONArray("vehicles")
        val result = mutableListOf<Pair<Vehicle, List<FuelEntry>>>()
        for (i in 0 until vehiclesArr.length()) {
            val vObj = vehiclesArr.getJSONObject(i)
            val vehicle = Vehicle(
                id = vObj.optLong("id", 0L),
                name = vObj.getString("name"),
                createdAt = vObj.optLong("createdAt", System.currentTimeMillis())
            )
            val entriesArr = vObj.optJSONArray("entries") ?: JSONArray()
            val entries = mutableListOf<FuelEntry>()
            for (j in 0 until entriesArr.length()) {
                val eObj = entriesArr.getJSONObject(j)
                entries.add(
                    FuelEntry(
                        id = eObj.optLong("id", 0L),
                        vehicleId = eObj.getLong("vehicleId"),
                        odometerKm = eObj.getDouble("odometerKm"),
                        amountPaid = eObj.getDouble("amountPaid"),
                        liters = eObj.getDouble("liters"),
                        date = eObj.getLong("date"),
                        note = if (eObj.isNull("note")) null else eObj.optString("note")
                    )
                )
            }
            result.add(vehicle to entries)
        }
        return result
    }
}
