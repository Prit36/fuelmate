package com.example.fuelmate.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.fuelmate.data.model.FuelRecord
import com.example.fuelmate.ui.util.formatMileage

/**
 * A lightweight bar chart showing per-fill mileage (km/L) over time.
 * Only entries with a computed mileage are plotted.
 */
@Composable
fun MileageChart(
    records: List<FuelRecord>,
    modifier: Modifier = Modifier
) {
    val points = records.mapNotNull { rec ->
        rec.mileageKmPerLitre?.let { it to rec.entry.date }
    }
    if (points.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Not enough data yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val values = points.map { it.first }
    val min = values.minOrNull() ?: 0.0
    val max = values.maxOrNull() ?: 1.0
    val range = (max - min).coerceAtLeast(0.1)

    val primary = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val chartHeight = 150.dp
            val sideInset = 16.dp
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
            ) {
                val w = size.width
                val h = size.height
                val pad = 10.dp.toPx()
                val usableH = h - pad * 2
                val left = sideInset.toPx()
                val right = w - sideInset.toPx()
                val plotW = right - left
                val stepX = if (points.size > 1) plotW / (points.size - 1) else 0f

                // Baseline grid
                val gridColor = Color.Gray.copy(alpha = 0.22f)
                for (i in 0..3) {
                    val y = pad + usableH * (i / 3f)
                    drawLine(
                        color = gridColor,
                        start = Offset(left, y),
                        end = Offset(right, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )
                }

                // Bars
                val barWidth = (stepX * 0.45f).coerceAtMost(34.dp.toPx()).coerceAtLeast(8.dp.toPx())
                points.forEachIndexed { index, (value, _) ->
                    val x = if (points.size > 1) left + index * stepX else w / 2f
                    val barH = ((value - min) / range).toFloat() * usableH
                    val top = pad + (usableH - barH)
                    drawRect(
                        color = primary,
                        topLeft = Offset(x - barWidth / 2f, top),
                        size = androidx.compose.ui.geometry.Size(
                            barWidth,
                            barH.coerceAtLeast(3.dp.toPx())
                        )
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Min / max legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Low: ${formatMileage(min)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "High: ${formatMileage(max)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
