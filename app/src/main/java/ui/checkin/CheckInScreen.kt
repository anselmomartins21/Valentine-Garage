package com.valentinegarage.ui.checkin

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

// ─────────────────────────────────────────────
//  Brand colours (matches your Brand Toolkit)
// ─────────────────────────────────────────────
val MechanicRed   = Color(0xFFD32F2F)
val FadedRed      = Color(0xFFFFDAD6)
val GreaseGray    = Color(0xFF121212)
val SteelGray     = Color(0xFF54595E)
val VerifiedGreen = Color(0xFF2E7D32)
val SurfaceLight  = Color(0xFFF5F5F5)

// ─────────────────────────────────────────────
//  Condition options shown as chips
// ─────────────────────────────────────────────
val CONDITION_OPTIONS = listOf(
    "Good – No visible damage",
    "Minor scratches / dents",
    "Cracked windshield",
    "Low tyre pressure",
    "Oil leaking",
    "Engine warning light",
    "Body damage",
    "Other (see notes)"
)

// ─────────────────────────────────────────────
//  Screen
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    onCheckInSuccess: () -> Unit,
    viewModel: CheckInViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "VALENTINE'S GARAGE",
                            fontSize = 12.sp,
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->

        // Show success state
        if (uiState.isSuccess) {
            CheckInSuccessScreen(onDone = onCheckInSuccess)
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Section 1: Vehicle Info ──────────────────────────
            SectionCard(title = "Vehicle Information", icon = Icons.Default.LocalShipping) {

                // License Plate
                GarageTextField(
                    value = uiState.licensePlate,
                    onValueChange = viewModel::onLicensePlateChange,
                    label = "License Plate *",
                    placeholder = "e.g. N 12345 W",
                    isError = uiState.licensePlateError != null,
                    errorMessage = uiState.licensePlateError,
                    leadingIcon = Icons.Default.DirectionsCar
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Kilometers Driven
                GarageTextField(
                    value = uiState.kilometersDriven,
                    onValueChange = viewModel::onKilometersChange,
                    label = "Kilometers Driven *",
                    placeholder = "e.g. 154200",
                    isError = uiState.kilometersError != null,
                    errorMessage = uiState.kilometersError,
                    leadingIcon = Icons.Default.Speed,
                    keyboardType = KeyboardType.Number
                )
            }

            // ── Section 2: Vehicle Condition ─────────────────────
            SectionCard(title = "Initial Condition", icon = Icons.Default.Assignment) {

                Text(
                    text = "Select all that apply:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SteelGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Condition chips – 2 per row
                val chunked = CONDITION_OPTIONS.chunked(2)
                chunked.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { option ->
                            val selected = option in uiState.selectedConditions
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.onConditionToggle(option) },
                                label = {
                                    Text(
                                        text = option,
                                        fontSize = 12.sp,
                                        maxLines = 2
                                    )
                                },
                                leadingIcon = if (selected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FadedRed,
                                    selectedLabelColor = MechanicRed,
                                    selectedLeadingIconColor = MechanicRed
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Pad row if odd number of items
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Additional condition notes
                OutlinedTextField(
                    value = uiState.conditionNotes,
                    onValueChange = viewModel::onConditionNotesChange,
                    label = { Text("Additional condition notes") },
                    placeholder = { Text("Describe any damage, issues, or notable observations…") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // ── Section 3: Driver Info ───────────────────────────
            SectionCard(title = "Driver / Owner Details", icon = Icons.Default.Person) {

                GarageTextField(
                    value = uiState.driverName,
                    onValueChange = viewModel::onDriverNameChange,
                    label = "Driver Name",
                    placeholder = "e.g. John Muteka",
                    leadingIcon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.height(8.dp))

                GarageTextField(
                    value = uiState.driverPhone,
                    onValueChange = viewModel::onDriverPhoneChange,
                    label = "Contact Number",
                    placeholder = "e.g. +264 81 000 0000",
                    leadingIcon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone
                )
            }

            // ── Error banner ─────────────────────────────────────
            AnimatedVisibility(visible = uiState.submitError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = FadedRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MechanicRed)
                        Text(
                            text = uiState.submitError ?: "",
                            color = MechanicRed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // ── Submit Button ────────────────────────────────────
            Button(
                onClick = { viewModel.submitCheckIn(onSuccess = onCheckInSuccess) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MechanicRed),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CHECK IN TRUCK",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────
//  Reusable Section Card
// ─────────────────────────────────────────────
@Composable
fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(icon, contentDescription = null, tint = MechanicRed)
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            content()
        }
    }
}

// ─────────────────────────────────────────────
//  Reusable Text Field
// ─────────────────────────────────────────────
@Composable
fun GarageTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isError: Boolean = false,
    errorMessage: String? = null,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder, color = SteelGray) },
            isError = isError,
            leadingIcon = leadingIcon?.let {
                { Icon(it, contentDescription = null) }
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
//  Success Screen
// ─────────────────────────────────────────────
@Composable
fun CheckInSuccessScreen(onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = VerifiedGreen,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Truck Checked In!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = VerifiedGreen
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "The vehicle has been logged. Mechanics can now access the service checklist.",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = SteelGray
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen)
        ) {
            Text("GO TO DASHBOARD", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}
