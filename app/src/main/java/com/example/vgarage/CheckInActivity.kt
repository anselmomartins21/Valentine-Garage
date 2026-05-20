package com.example.vgarage

import android.content.Intent
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.launch

class CheckInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CheckInScreen(
                onBack = { finish() },
                onCheckIn = { truckId, plate ->
                    val intent = Intent(this, TasksActivity::class.java)
                    intent.putExtra("truckId", truckId)
                    intent.putExtra("plate", plate)
                    startActivity(intent)
                }
            )
        }
    }
}

val repairOptions = listOf(
    "Cracked Windshield/Glass",
    "Body Dents or Scratches",
    "Tire Tread/Sidewall Damage",
    "Exterior Lighting Faults",
    "Engine/Oil Issues",
    "Brake Problems"
)



@Composable
fun CheckInScreen(onBack: () -> Unit = {}, onCheckIn: (String, String) -> Unit) {
    val scope            = rememberCoroutineScope()
    var plate            by remember { mutableStateOf("") }
    var km               by remember { mutableStateOf("") }
    var condition        by remember { mutableStateOf("Good") }
    var selectedRepairs  by remember { mutableStateOf<Set<String>>(emptySet()) }
    var additionalNotes  by remember { mutableStateOf("") }
    var loading          by remember { mutableStateOf(false) }
    var error            by remember { mutableStateOf("") }
    var existingPlates   by remember { mutableStateOf<List<String>>(emptyList()) }
    var showTruckPicker  by remember { mutableStateOf(false) }
    var existingTrucks   by remember { mutableStateOf<List<Truck>>(emptyList()) }

    LaunchedEffect(Unit) {
        existingTrucks = GarageRepository.getAllTrucks()
    }

    // Progress: plate(25) + km(25) + condition always(25) + repair(25)
    val progress = (
            (if (plate.isNotEmpty()) 25 else 0) +
                    (if (km.isNotEmpty()) 25 else 0) +
                    25 + // condition always has default
                    (if (selectedRepairs.isNotEmpty()) 25 else 0)
            ) / 100f

    LaunchedEffect(Unit) {
        existingPlates = GarageRepository.getAllTrucks()
            .map { it.plateNumber }.distinct().sorted()
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
        focusedContainerColor = Color(0xFF0F172A), unfocusedContainerColor = Color(0xFF0F172A),
        focusedBorderColor = Color(0xFFF97318), unfocusedBorderColor = Color(0xFF334155),
        focusedLabelColor = Color(0xFFF97318), unfocusedLabelColor = Color(0xFF64748B)
    )

    Column(Modifier.fillMaxSize().background(Color(0xFF0F172A)).statusBarsPadding().navigationBarsPadding()){
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF1E293B))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                TextButton(onClick = onBack) {
                    Text("← Back", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
            Text("🚛", fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
            Column {
                Text("ASSET IDENTIFICATION",
                    color = Color(0xFFF97318), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("Vehicle Intake",
                    color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
        ) {
            // Plate input
            OutlinedTextField(
                value = plate,
                onValueChange = { plate = it.uppercase() },
                label = { Text("TRUCK ID / PLATE") },
                placeholder = { Text("e.g. N 12345 W", color = Color(0xFF475569)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            // Recent truck suggestions
            if (existingPlates.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("Recent trucks:", color = Color(0xFF64748B), fontSize = 11.sp)
                Spacer(Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(existingPlates.take(6)) { p ->
                        Box(
                            modifier = Modifier
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(4.dp))
                                .background(Color(0xFF1E293B), RoundedCornerShape(4.dp))
                                .clickable { plate = p }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(p, color = Color(0xFF94A3B8), fontSize = 12.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = km,
                onValueChange = { km = it },
                label = { Text("CURRENT KILOMETERS") },
                placeholder = { Text("e.g. 045000", color = Color(0xFF475569)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = fieldColors
            )
            Spacer(Modifier.height(20.dp))

            // Condition
            Text("VEHICLE CONDITION",
                color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Good","Fair").forEach { opt ->
                        Button(onClick = { condition = opt },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (condition == opt)
                                    Color(0xFFF97318) else Color(0xFF1E293B))
                        ) { Text(opt, color = Color.White) }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Poor","Critical").forEach { opt ->
                        Button(onClick = { condition = opt },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (condition == opt)
                                    if (opt == "Critical") Color(0xFFEF4444)
                                    else Color(0xFFF97318)
                                else Color(0xFF1E293B))
                        ) { Text(opt, color = Color.White) }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Circular progress
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    val orange = Color(0xFFF97318)
                    val track  = Color(0xFF334155)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val sw = 14.dp.toPx()
                        val r  = (size.minDimension / 2) - sw / 2
                        drawCircle(color = track, radius = r, style = Stroke(sw))
                        if (progress > 0f) {
                            drawArc(
                                color = orange, startAngle = -90f,
                                sweepAngle = 360f * progress, useCenter = false,
                                style = Stroke(sw, cap = StrokeCap.Round),
                                topLeft = Offset(size.width/2 - r, size.height/2 - r),
                                size = Size(r*2, r*2)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${(progress * 100).toInt()}%",
                            color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("INTAKE\nPROGRESS",
                            color = Color(0xFF64748B), fontSize = 9.sp,
                            textAlign = TextAlign.Center, lineHeight = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("Required fields completed",
                color = Color(0xFF64748B), fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

            Spacer(Modifier.height(24.dp))

            // Repairs section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("☑", fontSize = 18.sp, color = Color(0xFFF97318))
                Spacer(Modifier.width(8.dp))
                Text("REPAIRS", color = Color.White,
                    fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = Color(0xFF334155))

            repairOptions.forEach { repair ->
                val isSelected = repair in selectedRepairs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedRepairs = if (isSelected)
                                selectedRepairs - repair else selectedRepairs + repair
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .border(
                                1.5.dp,
                                if (isSelected) Color(0xFFF97318) else Color(0xFF475569),
                                RoundedCornerShape(3.dp)
                            )
                            .background(
                                if (isSelected) Color(0xFF2A1400) else Color.Transparent,
                                RoundedCornerShape(3.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected)
                            Text("✓", color = Color(0xFFF97318),
                                fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(repair, color = Color(0xFFCBD5E1), fontSize = 14.sp)
                }
                HorizontalDivider(color = Color(0xFF1E293B))
            }

            Spacer(Modifier.height(20.dp))

            // Additional observations
            Text("ADDITIONAL OBSERVATIONS",
                color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = additionalNotes,
                onValueChange = { additionalNotes = it },
                placeholder = {
                    Text("Detail any engine noises, fluid leaks, or driver complaints...",
                        color = Color(0xFF475569), fontSize = 13.sp)
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = fieldColors
            )

            if (error.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(error, color = Color(0xFFEF4444), fontSize = 13.sp)
            }

            Spacer(Modifier.height(32.dp))

            // Discard button
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("DISCARD ENTRY", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))

            // Submit button
            Button(
                onClick = {
                    if (plate.isEmpty() || km.isEmpty()) {
                        error = "Please fill plate and kilometres"
                        return@Button
                    }
                    val kmNum = km.toIntOrNull() ?: run {
                        error = "Kilometres must be a number"; return@Button
                    }
                    error = ""; loading = true
                    scope.launch {
                        try {
                            val truckId = GarageRepository.checkInTruck(
                                plate           = plate,
                                km              = kmNum,
                                condition       = condition,
                                repairs         = selectedRepairs.joinToString(", "),
                                additionalNotes = additionalNotes,
                                userId          = CurrentUser.id
                            )
                            onCheckIn(truckId, plate)
                        } catch (e: Exception) {
                            error = "Failed: ${e.message ?: "Check your internet connection"}"
                        } finally {
                            loading = false  // always reset — prevents permanent spinner
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97318)),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(color = Color.White,
                        modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("✓  COMPLETE CHECK-IN",
                        fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun CheckInPreview() {
    CheckInScreen(onCheckIn = { _, _ -> })
}