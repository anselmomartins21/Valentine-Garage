package com.valentinegarage.ui.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valentinegarage.data.model.VehicleCheckIn
import com.valentinegarage.data.repository.GarageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─────────────────────────────────────────────
//  UI State data class
// ─────────────────────────────────────────────
data class CheckInUiState(
    // Form fields
    val licensePlate: String = "",
    val kilometersDriven: String = "",
    val selectedConditions: Set<String> = emptySet(),
    val conditionNotes: String = "",
    val driverName: String = "",
    val driverPhone: String = "",

    // Validation errors
    val licensePlateError: String? = null,
    val kilometersError: String? = null,

    // Submission state
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val submitError: String? = null
)

// ─────────────────────────────────────────────
//  ViewModel
// ─────────────────────────────────────────────
@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val repository: GarageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckInUiState())
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    // ── Field update handlers ────────────────

    fun onLicensePlateChange(value: String) {
        _uiState.update {
            it.copy(
                licensePlate = value.uppercase(),
                licensePlateError = null,
                submitError = null
            )
        }
    }

    fun onKilometersChange(value: String) {
        // Only allow digits
        if (value.all { it.isDigit() }) {
            _uiState.update {
                it.copy(
                    kilometersDriven = value,
                    kilometersError = null,
                    submitError = null
                )
            }
        }
    }

    fun onConditionToggle(condition: String) {
        _uiState.update { state ->
            val updated = state.selectedConditions.toMutableSet()
            if (condition in updated) updated.remove(condition) else updated.add(condition)
            state.copy(selectedConditions = updated)
        }
    }

    fun onConditionNotesChange(value: String) {
        _uiState.update { it.copy(conditionNotes = value) }
    }

    fun onDriverNameChange(value: String) {
        _uiState.update { it.copy(driverName = value) }
    }

    fun onDriverPhoneChange(value: String) {
        _uiState.update { it.copy(driverPhone = value) }
    }

    // ── Validation ───────────────────────────

    private fun validate(): Boolean {
        var valid = true

        val plate = _uiState.value.licensePlate.trim()
        if (plate.isBlank()) {
            _uiState.update { it.copy(licensePlateError = "License plate is required") }
            valid = false
        }

        val km = _uiState.value.kilometersDriven.trim()
        if (km.isBlank()) {
            _uiState.update { it.copy(kilometersError = "Kilometers driven is required") }
            valid = false
        } else if (km.toIntOrNull() == null || km.toInt() < 0) {
            _uiState.update { it.copy(kilometersError = "Enter a valid kilometer reading") }
            valid = false
        }

        return valid
    }

    // ── Submit ───────────────────────────────

    fun submitCheckIn(onSuccess: () -> Unit) {
        if (!validate()) return

        val state = _uiState.value

        // Build the condition string from chips + notes
        val conditionParts = state.selectedConditions.toMutableList()
        if (state.conditionNotes.isNotBlank()) conditionParts.add(state.conditionNotes.trim())
        val conditionSummary = conditionParts.joinToString("; ")

        val checkIn = VehicleCheckIn(
            licensePlate = state.licensePlate.trim(),
            kilometersDriven = state.kilometersDriven.trim().toInt(),
            initialCondition = conditionSummary,
            driverName = state.driverName.trim(),
            driverPhone = state.driverPhone.trim()
            // checkedInByEmployeeId is set inside the repository from the logged-in user session
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, submitError = null) }
            try {
                repository.checkInVehicle(checkIn)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        submitError = "Failed to save check-in. Please try again."
                    )
                }
            }
        }
    }
}
