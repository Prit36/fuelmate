package com.example.fuelmate.ui.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.fuelmate.ui.screen.AddFuelEntryScreen
import com.example.fuelmate.ui.screen.SettingsScreen
import com.example.fuelmate.ui.screen.VehicleDetailScreen
import com.example.fuelmate.ui.screen.VehicleListScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                },
                onSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.VEHICLE_DETAIL,
            arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
        ) { backStack ->
            val vehicleId = backStack.arguments?.getLong("vehicleId") ?: 0L
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            // Capture CSV content set by onExportCsv, then write it to the user-picked URI.
            var pendingCsv by remember { mutableStateOf<String?>(null) }
            val csvLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.CreateDocument("text/csv")
            ) { uri ->
                val csv = pendingCsv ?: return@rememberLauncherForActivityResult
                uri ?: return@rememberLauncherForActivityResult
                // File I/O off the main thread to avoid jank on large exports.
                scope.launch(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter().use { it?.write(csv) }
                }
                pendingCsv = null
            }
            VehicleDetailScreen(
                vehicleId = vehicleId,
                onBack = { navController.popBackStack() },
                onAddFuel = { id -> navController.navigate(Routes.addFuel(id)) },
                onEditFuel = { entryId -> navController.navigate(Routes.editFuel(vehicleId, entryId)) },
                onExportCsv = { csv, fileName ->
                    pendingCsv = csv
                    csvLauncher.launch(fileName)
                }
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