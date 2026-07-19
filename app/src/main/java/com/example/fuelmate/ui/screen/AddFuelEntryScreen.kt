package com.example.fuelmate.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fuelmate.ui.util.formatDate
import com.example.fuelmate.ui.viewmodel.AddFuelEntryEvent
import com.example.fuelmate.ui.viewmodel.AddFuelEntryViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFuelEntryScreen(
    vehicleId: Long,
    entryId: Long? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddFuelEntryViewModel = koinViewModel(
        parameters = { parametersOf(vehicleId, entryId) }
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Consume one-shot events (e.g. navigate back after a successful save) via the
    // ViewModel's event channel. This avoids re-trigger bugs from LaunchedEffect on state.
    val event by viewModel.events.collectAsStateWithLifecycle(initialValue = null)
    if (event is AddFuelEntryEvent.Saved) {
        onSaved()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditing) "Edit Fuel Entry" else "Add Fuel Entry") },
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
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!viewModel.isEditing && viewModel.hasLatest) {
                androidx.compose.material3.OutlinedButton(
                    onClick = viewModel::fillFromLatest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.LocalGasStation, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Same as last fill-up")
                }
            }
            LabeledField(
                icon = Icons.Filled.Route,
                label = "Odometer reading (km)",
                value = state.odometer,
                onValueChange = viewModel::onOdometerChange,
                keyboardType = KeyboardType.Decimal,
                isError = state.odometerError != null,
                errorText = state.odometerError,
                placeholder = "e.g. 12450"
            )
            LabeledField(
                icon = Icons.Filled.Savings,
                label = "Amount paid (Rs)",
                value = state.amount,
                onValueChange = viewModel::onAmountChange,
                keyboardType = KeyboardType.Decimal,
                isError = state.amountError != null,
                errorText = state.amountError,
                placeholder = "e.g. 1500"
            )
            LabeledField(
                icon = Icons.Filled.LocalGasStation,
                label = "Litres filled",
                value = state.liters,
                onValueChange = viewModel::onLitersChange,
                keyboardType = KeyboardType.Decimal,
                isError = state.litersError != null,
                errorText = state.litersError,
                placeholder = "e.g. 12.5"
            )
            LabeledField(
                icon = Icons.Filled.Speed,
                label = "Note (optional)",
                value = state.note,
                onValueChange = viewModel::onNoteChange,
                singleLine = false,
                placeholder = "e.g. Full tank, highway trip"
            )

            // Date picker field
            OutlinedTextField(
                value = formatDate(state.dateMillis),
                onValueChange = {},
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                shape = RoundedCornerShape(14.dp),
                leadingIcon = {
                    Icon(Icons.Filled.DateRange, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.DateRange, contentDescription = "Pick date")
                    }
                }
            )

            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    if (state.isSaving) "Saving..." else if (viewModel.isEditing) "Save Changes" else "Save Entry",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            initialMillis = state.dateMillis,
            onDismiss = { showDatePicker = false },
            onConfirm = { millis ->
                viewModel.onDateChange(millis)
                showDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabeledField(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorText: String? = null,
    singleLine: Boolean = true,
    placeholder: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        isError = isError,
        shape = RoundedCornerShape(14.dp),
        leadingIcon = { Icon(icon, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        placeholder = placeholder?.let { { Text(it) } },
        supportingText = errorText?.let { { Text(it) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    initialMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                onConfirm(datePickerState.selectedDateMillis ?: initialMillis)
            }) { Text("OK") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        androidx.compose.material3.DatePicker(state = datePickerState)
    }
}
