package com.example.fuelmate.data.export

import com.example.fuelmate.data.local.entity.Vehicle
import com.example.fuelmate.data.model.FuelRecord
import com.example.fuelmate.ui.util.formatDate
import com.example.fuelmate.ui.util.formatLiters
import com.example.fuelmate.ui.util.formatNum
import com.example.fuelmate.ui.util.formatRupees

/**
 * Pure function: renders a vehicle's fuel history as CSV (RFC-4180 style quoting).
 * No Android dependencies, so it is trivially unit-testable.
 */
object CsvExport {

    private val header = listOf(
        "Date", "Odometer (km)", "Litres", "Amount (Rs)", "Mileage (km/L)", "Note"
    )

    fun toCsv(vehicle: Vehicle, records: List<FuelRecord>): String {
        val rows = mutableListOf<String>()
        rows.add(header.joinToString(",") { it.quote() })
        for (r in records) {
            val e = r.entry
            val mileage = r.mileageKmPerLitre?.let { "%.2f".format(it) } ?: ""
            rows.add(
                listOf(
                    formatDate(e.date),
                    formatNum(e.odometerKm),
                    formatLiters(e.liters),
                    formatRupees(e.amountPaid),
                    mileage,
                    e.note ?: ""
                ).joinToString(",") { it.quote() }
            )
        }
        return rows.joinToString("\r\n")
    }

    /** Quotes a field if it contains comma, quote, or newline; escapes inner quotes by doubling. */
    private fun String.quote(): String =
        if (any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + replace("\"", "\"\"") + "\""
        } else {
            this
        }
}
