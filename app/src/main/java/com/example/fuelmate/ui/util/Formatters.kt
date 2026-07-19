package com.example.fuelmate.ui.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
    .withZone(ZoneId.systemDefault())

fun formatDate(millis: Long): String = dateFormatter.format(Instant.ofEpochMilli(millis))

fun formatKm(value: Double): String =
    if (value % 1.0 == 0.0) "%,d".format(value.toLong()) else "%,.2f".format(value)

fun formatLiters(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else "%.2f".format(value)

fun formatRupees(value: Double): String =
    if (value % 1.0 == 0.0) "Rs %,d".format(value.toLong()) else "Rs %,.2f".format(value)

fun formatMileage(value: Double?): String =
    if (value == null) "—" else "%.1f km/L".format(value)

fun formatCostPerKm(value: Double?): String =
    if (value == null) "—" else "Rs %.2f/km".format(value)

/** Formats a numeric value without trailing decimals when it is a whole number. */
fun formatNum(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else "%.2f".format(value)