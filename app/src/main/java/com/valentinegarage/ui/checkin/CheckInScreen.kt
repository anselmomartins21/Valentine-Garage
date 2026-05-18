package com.valentinegarage.ui.checkin



import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// ── Brand Colors from Valentine's Garage Brand Toolkit ──
val MechanicRed   = Color(0xFFD32F2F) // main red color
val FadedRed      = Color(0xFFFFDAD6) // light red for chips
val SteelGray     = Color(0xFF54595E) // gray for hints
val VerifiedGreen = Color(0xFF2E7D32) // green for success

// ── List of repair issues mechanic can select ──
val REPAIR_OPTIONS = listOf(
    "Cracked Windshield/Glass",
    "Body Dents or Scratches",
    "Tire Tread/Sidewall Damage",
    "Exterior Lighting Faults",
    "Engine/Oil Issues",
    "Brake Problems"
)

// ── Condition options ──
val CONDITIONS = listOf("Good", "Fair", "Poor", "Critical")

// ── Main Screen ──────────────────────────────────────────
// viewModel() automatically creates our CheckInViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(viewModel: CheckInViewModel = viewModel()) {

    // collectAsState() means: watch the state and
    // redraw the screen every time something changes
    val state by viewModel.state.collectAsState()

    // If check-in was successful show success screen
    if (state.isSuccess) {
        SuccessScreen()
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "VALENTINE'S GARAGE",
                            fontSize = 11.sp,
                            color = MechanicRed,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Truck Check-In",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { padding ->

        // verticalScroll allows the screen to scroll up and down
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── SECTION 1: Vehicle Information ───────────
            SectionCard(
                title = "Vehicle Information",
                icon = Icons.Default.LocalShipping
            ) {
                // License plate field
                GarageTextField(
                    value = state.licensePlate,
                    onValueChange = viewModel::onLicensePlateChange,
                    label = "License Plate *",
                    placeholder = "e.g. N 12345 W",
                    icon = Icons.Default.DirectionsCar,
                    isError = state.licensePlateError != null,
                    errorMessage = state.licensePlateError
                )

                Spacer(Modifier.height(8.dp))

                // Kilometers field - only accepts numbers
                GarageTextField(
                    value = state.kilometersDriven,
                    onValueChange = viewModel::onKilometersChange,
                    label = "Kilometers Driven *",
                    placeholder = "e.g. 154200",
                    icon = Icons.Default.Speed,
                    keyboardType = KeyboardType.Number,
                    isError = state.kilometersError != null,
                    errorMessage = state.kilometersError
                )
            }

            // ── SECTION 2: Vehicle Condition ─────────────
            SectionCard(
                title = "Vehicle Condition",
                icon = Icons.Default.Assignment
            ) {
                Text(
                    "Select current condition:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SteelGray
                )
                Spacer(Modifier.height(8.dp))

                // Show condition buttons in 2 rows of 2
                CONDITIONS.chunked(2).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { condition ->
                            val isSelected = state.condition == condition
                            Button(
                                onClick = { viewModel.onConditionChange(condition) },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when {
                                        isSelected && condition == "Critical" -> Color.Red
                                        isSelected -> MechanicRed
                                        else -> SteelGray.copy(alpha = 0.2f)
                                    }
                                )
                            ) {
                                Text(
                                    condition,
                                    color = if (isSelected) Color.White
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }

            // ── SECTION 3: Repairs Needed ─────────────────
            SectionCard(
                title = "Repairs Needed",
                icon = Icons.Default.Build
            ) {
                Text(
                    "Tick all that apply:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SteelGray
                )
                Spacer(Modifier.height(8.dp))

                // Show each repair as a checkbox row
                REPAIR_OPTIONS.forEach { repair ->
                    val isSelected = repair in state.selectedRepairs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { viewModel.onRepairToggle(repair) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MechanicRed
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(repair, fontSize = 14.sp)
                    }
                    HorizontalDivider(color = SteelGray.copy(alpha = 0.2f))
                }

                Spacer(Modifier.height(8.dp))

                // Additional notes text area
                OutlinedTextField(
                    value = state.additionalNotes,
                    onValueChange = viewModel::onNotesChange,
                    label = { Text("Additional Notes") },
                    placeholder = { Text("Any other observations...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // ── SECTION 4: Driver Details ─────────────────
            SectionCard(
                title = "Driver Details",
                icon = Icons.Default.Person
            ) {
                GarageTextField(
                    value = state.driverName,
                    onValueChange = viewModel::onDriverNameChange,
                    label = "Driver Name",
                    placeholder = "e.g. John Muteka",
                    icon = Icons.Default.Person
                )
                Spacer(Modifier.height(8.dp))
                GarageTextField(
                    value = state.driverPhone,
                    onValueChange = viewModel::onDriverPhoneChange,
                    label = "Contact Number",
                    placeholder = "e.g. +264 81 000 0000",
                    icon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone
                )
            }

            // ── Error Message ─────────────────────────────
            AnimatedVisibility(visible = state.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = FadedRed
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Error, null, tint = MechanicRed)
                        Text(state.errorMessage ?: "", color = MechanicRed)
                    }
                }
            }

            // ── Submit Button ─────────────────────────────
            Button(
                onClick = viewModel::submitCheckIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MechanicRed
                ),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    // Show spinner while saving to Firebase
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "CHECK IN TRUCK",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Reusable Section Card ─────────────────────────────────
// Used to group related fields together with a title
@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(icon, null, tint = MechanicRed)
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            content()
        }
    }
}

// ── Reusable Text Field ───────────────────────────────────
// Used for license plate, kilometers, driver name, phone
@Composable
fun GarageTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder, color = SteelGray) },
            leadingIcon = icon?.let { { Icon(it, null) } },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = isError,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        // Show error message below field if there is one
        if (isError && errorMessage != null) {
            Text(
                errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

// ── Success Screen ────────────────────────────────────────
// Shows after truck is successfully checked in
@Composable
fun SuccessScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            null,
            tint = VerifiedGreen,
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Truck Checked In!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = VerifiedGreen
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "The vehicle has been logged successfully.\nMechanics can now access the service checklist.",
            textAlign = TextAlign.Center,
            color = SteelGray
        )
    }
}