package com.example.fuelmate.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.fuelmate.ui.screen.AddFuelEntryScreen
import com.example.fuelmate.ui.screen.VehicleDetailScreen
import com.example.fuelmate.ui.screen.VehicleListScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.VEHICLE_LIST
    ) {
        composable(Routes.VEHICLE_LIST) {
            VehicleListScreen(
                onVehicleClick = { id ->
                    navController.navigate(Routes.vehicleDetail(id))
                }
            )
        }

        composable(
            route = Routes.VEHICLE_DETAIL,
            arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
        ) { backStack ->
            val vehicleId = backStack.arguments?.getLong("vehicleId") ?: 0L
            VehicleDetailScreen(
                vehicleId = vehicleId,
                onBack = { navController.popBackStack() },
                onAddFuel = { id -> navController.navigate(Routes.addFuel(id)) }
            )
        }

        composable(
            route = Routes.ADD_FUEL_ENTRY,
            arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
        ) { backStack ->
            val vehicleId = backStack.arguments?.getLong("vehicleId") ?: 0L
            AddFuelEntryScreen(
                vehicleId = vehicleId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
    }
}