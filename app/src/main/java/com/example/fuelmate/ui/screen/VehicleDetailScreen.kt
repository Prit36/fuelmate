package com.example.fuelmate.ui.screen

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fuelmate.data.model.FuelRecord
import com.example.fuelmate.data.model.VehicleStats
import com.example.fuelmate.ui.components.ConfirmDeleteDialog
import com.example.fuelmate.ui.components.FuelEntryRow
import com.example.fuelmate.ui.components.MileageChart
import com.example.fuelmate.ui.components.StatCard
import com.example.fuelmate.ui.util.formatCostPerKm
import com.example.fuelmate.ui.util.formatDate
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
    onEditFuel: (Long) -> Unit,
    viewModel: VehicleDetailViewModel = koinViewModel(parameters = { parametersOf(vehicleId) })
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(state.vehicle?.name ?: "Vehicle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddFuel(vehicleId) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add fuel entry")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading...")
            }
        } else {
            DetailContent(
                padding = padding,
                stats = state.stats,
                records = state.records,
                onEdit = onEditFuel,
                onRequestDelete = viewModel::requestDeleteEntry
            )
        }
    }

    if (state.pendingDelete != null) {
        val record = state.pendingDelete!!
        ConfirmDeleteDialog(
            title = "Delete this fuel entry?",
            message = "The fill-up on ${formatDate(record.entry.date)} " +
                "(${formatLiters(record.entry.liters)} L) will be permanently removed. This cannot be undone.",
            onConfirm = viewModel::confirmDeleteEntry,
            onDismiss = viewModel::cancelDeleteEntry
        )
    }
}

@Composable
private fun DetailContent(
    padding: PaddingValues,
    stats: VehicleStats,
    records: List<FuelRecord>,
    onEdit: (Long) -> Unit,
    onRequestDelete: (FuelRecord) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeroSummaryCard(stats = stats)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Distance",
                    value = "${formatKm(stats.totalDistanceKm)} km",
                    icon = Icons.Filled.Speed
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Spent",
                    value = formatRupees(stats.totalSpent),
                    icon = Icons.Filled.LocalGasStation
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Cost / km",
                    value = formatCostPerKm(stats.costPerKm),
                    icon = Icons.Filled.LocalGasStation
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Fuel",
                    value = "${formatLiters(stats.totalLiters)} L",
                    icon = Icons.Filled.LocalGasStation
                )
            }
        }

        // Mileage trend chart (only when we have at least 2 computed mileages)
        val mileagePoints = records.mapNotNull { it.mileageKmPerLitre }
        if (mileagePoints.size >= 2) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Mileage Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    MileageChart(records = records)
                }
            }
        }

        if (records.isEmpty()) {
            item {
                EmptyRecords()
            }
        } else {
            item {
                Text(
                    "Fuel History",
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(records, key = { it.entry.id }) { record ->
                var menuExpanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Box {
                        FuelEntryRow(record = record)
                        // Trailing "more" button opens the edit/delete menu.
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(2.dp)
                        ) {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    Icons.Filled.MoreVert,
                                    contentDescription = "More options",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        onEdit(record.entry.id)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        onRequestDelete(record)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroSummaryCard(stats: VehicleStats) {
    val avg = stats.averageMileage
    val scheme = MaterialTheme.colorScheme
    // Blend primaryContainer with the surface background to get a solid, lighter tone
    // that matches the rest of the UI (no translucent tint over the surface, which
    // previously caused a visible color mismatch at the card edges).
    val heroColor = blend(scheme.primaryContainer, scheme.surface, 0.45f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = heroColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Average Mileage",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    formatMileage(avg),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Speed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyRecords() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.LocalGasStation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "No fuel entries yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Tap the + button to log your first fill-up.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Linearly blends two colors. [ratio] is the amount of [to] (0 = fully [from], 1 = fully [to]).
 * Used to derive a solid, lighter hero-card tone that matches the surrounding surface.
 */
private fun blend(from: Color, to: Color, ratio: Float): Color {
    return Color(
        red = from.red + (to.red - from.red) * ratio,
        green = from.green + (to.green - from.green) * ratio,
        blue = from.blue + (to.blue - from.blue) * ratio,
        alpha = from.alpha + (to.alpha - from.alpha) * ratio
    )
}
