package com.example.fuelmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fuelmate.data.backup.BackupRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isBusy: Boolean = false,
    val message: String? = null
)

/** One-shot event carrying the backup content for the UI to write to a file. */
sealed interface SettingsEvent {
    data class ShareText(val text: String, val fileName: String) : SettingsEvent
}

class SettingsViewModel(
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _state

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /** Builds a JSON backup string and emits it for the UI to save/share. */
    fun backup() {
        if (_state.value.isBusy) return
        _state.value = _state.value.copy(isBusy = true)
        viewModelScope.launch {
            runCatching {
                val json = backupRepository.exportAll()
                val stamp = System.currentTimeMillis()
                _state.value = _state.value.copy(
                    isBusy = false,
                    message = "Backup ready"
                )
                _events.trySend(
                    SettingsEvent.ShareText(
                        text = json,
                        fileName = "fuelmate-backup-${stampToName(stamp)}.json"
                    )
                )
            }.onFailure {
                _state.value = _state.value.copy(isBusy = false, message = "Backup failed: ${it.message}")
            }
        }
    }

    /** Imports a previously exported JSON backup (replaces all data). */
    fun restore(json: String) {
        if (_state.value.isBusy) return
        _state.value = _state.value.copy(isBusy = true)
        viewModelScope.launch {
            runCatching {
                val count = backupRepository.importAll(json)
                _state.value = _state.value.copy(isBusy = false, message = "Restored $count vehicle(s)")
            }.onFailure {
                _state.value = _state.value.copy(isBusy = false, message = "Restore failed: ${it.message}")
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    private fun stampToName(stamp: Long): String {
        return java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", java.util.Locale.ENGLISH)
            .withZone(java.time.ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochMilli(stamp))
    }
}
