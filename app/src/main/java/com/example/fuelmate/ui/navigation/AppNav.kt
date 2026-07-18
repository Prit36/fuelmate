package com.example.fuelmate.ui.navigation

object Routes {
    const val VEHICLE_LIST = "vehicle_list"
    const val VEHICLE_DETAIL = "vehicle_detail/{vehicleId}"
    const val ADD_FUEL_ENTRY = "add_fuel/{vehicleId}"

    fun vehicleDetail(vehicleId: Long) = "vehicle_detail/$vehicleId"
    fun addFuel(vehicleId: Long) = "add_fuel/$vehicleId"
}