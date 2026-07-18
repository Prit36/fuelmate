package com.example.fuelmate.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fuelmate.data.model.FuelRecord
import com.example.fuelmate.data.model.VehicleStats
import com.example.fuelmate.ui.components.FuelEntryRow
import com.example.fuelmate.ui.components.StatCard
import com.example.fuelmate.ui.util.formatCostPerKm
import com.example.fuelmate.ui.util.formatKm
import com.example.fuelmate.ui.util.formatLiters
import com.example.fuelmate.ui.util.formatMileage
import com.example.fuelmate.ui.util.formatRupees
import com.example.fuelmate.ui.viewmodel.VehicleDetailViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    vehicleId: Long,
    onBack: () -> Unit,
    onAddFuel: (Long) -> Unit,
    viewModel: VehicleDetailViewModel = koinViewModel(parameters = { parametersOf(vehicleId) })
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.vehicle?.name ?: "Vehicle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddFuel(vehicleId) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add fuel entry")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Loading...")
            }
        } else {
            DetailContent(
                padding = padding,
                stats = state.stats,
                records = state.records,
                onDelete = viewModel::deleteEntry
            )
        }
    }
}

@Composable
private fun DetailContent(
    padding: PaddingValues,
    stats: VehicleStats,
    records: List<FuelRecord>,
    onDelete: (FuelRecord) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Avg Mileage",
                        value = formatMileage(stats.averageMileage)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Distance",
                        value = "${formatKm(stats.totalDistanceKm)} km"
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Spent",
                        value = formatRupees(stats.totalSpent)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Cost / km",
                        value = formatCostPerKm(stats.costPerKm)
                    )
                }
                StatCard(
                    title = "Total Fuel",
                    value = "${formatLiters(stats.totalLiters)} L"
                )
            }
        }

        if (records.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(padding).padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No fuel entries yet. Tap + to add one.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            item {
                Text(
                    "Fuel History",
                    modifier = Modifier.padding(padding).padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            items(records, key = { it.entry.id }) { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    FuelEntryRow(record = record, onDelete = { onDelete(record) })
                }
            }
        }
    }
}