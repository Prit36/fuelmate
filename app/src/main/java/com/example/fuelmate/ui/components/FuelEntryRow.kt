package com.example.fuelmate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fuelmate.data.model.FuelRecord
import com.example.fuelmate.ui.util.formatDate
import com.example.fuelmate.ui.util.formatKm
import com.example.fuelmate.ui.util.formatLiters
import com.example.fuelmate.ui.util.formatMileage
import com.example.fuelmate.ui.util.formatRupees

@Composable
fun FuelEntryRow(
    record: FuelRecord,
    modifier: Modifier = Modifier
) {
    val entry = record.entry
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 56.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = formatDate(entry.date),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${formatKm(entry.odometerKm)} km • ${formatRupees(entry.amountPaid)} • ${formatLiters(entry.liters)} L",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Mileage: ${formatMileage(record.mileageKmPerLitre)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (!entry.note.isNullOrBlank()) {
                Text(
                    text = entry.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}