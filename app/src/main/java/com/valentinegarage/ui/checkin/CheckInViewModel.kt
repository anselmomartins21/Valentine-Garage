package com.valentinegarage.ui.checkin



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valentinegarage.data.GarageRepository
import com.valentinegarage.data.VehicleCheckIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// This holds all the data for our check-in form
// When any value changes, the screen updates automatically
data class CheckInState(
    val licensePlate: String = "",
    val kilometersDriven: String = "",
    val condition: String = "Good",
    val selectedRepairs: Set<String> = emptySet(),
    val additionalNotes: String = "",
    val driverName: String = "",
    val driverPhone: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val licensePlateError: String? = null,
    val kilometersError: String? = null
)

// ViewModel is the middleman between the screen and the database
// It contains all the business logic - no database code in the screen!
class CheckInViewModel : ViewModel() {

    // Repository handles all Firebase operations
    private val repository = GarageRepository()

    // _state is private - only ViewModel can change it
    private val _state = MutableStateFlow(CheckInState())

    // state is public - the screen can only READ it
    val state: StateFlow<CheckInState> = _state.asStateFlow()

    // Called every time user types in the license plate field
    fun onLicensePlateChange(value: String) {
        _state.update {
            it.copy(
                licensePlate = value.uppercase(), // auto capitalize
                licensePlateError = null // clear error when user types
            )
        }
    }

    // Called every time user types in the kilometers field
    fun onKilometersChange(value: String) {
        // Only allow numbers - no letters!
        if (value.all { it.isDigit() }) {
            _state.update {
                it.copy(
                    kilometersDriven = value,
                    kilometersError = null
                )
            }
        }
    }

    // Called when user selects Good/Fair/Poor/Critical
    fun onConditionChange(value: String) {
        _state.update { it.copy(condition = value) }
    }

    // Called when user ticks/unticks a repair checkbox
    fun onRepairToggle(repair: String) {
        _state.update { state ->
            val updated = state.selectedRepairs.toMutableSet()
            // If already selected → remove it, if not → add it
            if (repair in updated) updated.remove(repair)
            else updated.add(repair)
            state.copy(selectedRepairs = updated)
        }
    }

    // Called when user types in the notes field
    fun onNotesChange(value: String) {
        _state.update { it.copy(additionalNotes = value) }
    }

    // Called when user types driver name
    fun onDriverNameChange(value: String) {
        _state.update { it.copy(driverName = value) }
    }

    // Called when user types driver phone
    fun onDriverPhoneChange(value: String) {
        _state.update { it.copy(driverPhone = value) }
    }

    // Called when user presses the CHECK IN button
    fun submitCheckIn() {
        // Step 1: Validate - make sure required fields are filled
        var isValid = true

        if (_state.value.licensePlate.isBlank()) {
            _state.update { it.copy(licensePlateError = "License plate is required") }
            isValid = false
        }

        if (_state.value.kilometersDriven.isBlank()) {
            _state.update { it.copy(kilometersError = "Kilometers is required") }
            isValid = false
        }

        // Stop here if validation failed
        if (!isValid) return

        // Step 2: Build the check-in object with all form data
        val repairs = _state.value.selectedRepairs.joinToString(", ")
        val fullCondition = buildString {
            append(_state.value.condition)
            if (repairs.isNotEmpty()) append(" | Repairs: $repairs")
            if (_state.value.additionalNotes.isNotBlank())
                append(" | Notes: ${_state.value.additionalNotes}")
        }

        val checkIn = VehicleCheckIn(
            licensePlate = _state.value.licensePlate,
            kilometersDriven = _state.value.kilometersDriven.toInt(),
            initialCondition = fullCondition,
            driverName = _state.value.driverName,
            driverPhone = _state.value.driverPhone
        )

        // Step 3: Save to Firebase in the background
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.checkInVehicle(checkIn)
                // Success! Show success screen
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                // Something went wrong - show error message
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to save. Check your connection and try again."
                    )
                }
            }
        }
    }
}