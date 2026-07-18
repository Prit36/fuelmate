package com.example.fuelmate.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.fuelmate.ui.screen.AddFuelEntryScreen
import com.example.fuelmate.ui.screen.VehicleDetailScreen
import com.example.fuelmate.ui.screen.VehicleListScreen

private const val TRANSITION_DURATION = 300

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.VEHICLE_LIST,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
            )
        }
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
                onAddFuel = { id -> navController.navigate(Routes.addFuel(id)) },
                onEditFuel = { entryId -> navController.navigate(Routes.editFuel(vehicleId, entryId)) }
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

        composable(
            route = Routes.EDIT_FUEL_ENTRY,
            arguments = listOf(
                navArgument("vehicleId") { type = NavType.LongType },
                navArgument("entryId") { type = NavType.LongType }
            )
        ) { backStack ->
            val vehicleId = backStack.arguments?.getLong("vehicleId") ?: 0L
            val entryId = backStack.arguments?.getLong("entryId") ?: 0L
            AddFuelEntryScreen(
                vehicleId = vehicleId,
                entryId = entryId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
    }
}