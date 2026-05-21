package com.example.vgarage

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class TrucksInServiceActivity : ComponentActivity() {

    private val refreshTrigger = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrucksInServiceScreen(
                refreshTrigger = refreshTrigger.intValue,
                onBack = { finish() },
                onTruckSelected = { truck ->
                    val intent = Intent(this, TasksActivity::class.java)
                    intent.putExtra("truckId", truck.id)
                    intent.putExtra("plate",   truck.plateNumber)
                    startActivity(intent)
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refreshTrigger.intValue++
    }
}

@Composable
fun TrucksInServiceScreen(
    refreshTrigger: Int = 0,
    onBack: () -> Unit = {},
    onTruckSelected: (Truck) -> Unit = {}
) {
    var trucksInService by remember { mutableStateOf<List<Pair<Truck, List<Task>>>>(emptyList()) }
    var loading         by remember { mutableStateOf(true) }

    // Reload on every resume so the list stays fresh after a mechanic finishes tasks
    LaunchedEffect(refreshTrigger) {
        loading = true
        val allTrucks = GarageRepository.getAllTrucks()
        val allTasks  = GarageRepository.getAllTasks()
        trucksInService = allTrucks
            .map { truck -> truck to allTasks.filter { it.truckId == truck.id } }
            .filter { (_, tasks) -> tasks.any { it.status == "pending" } }
            .sortedByDescending { (_, tasks) ->
                // Show trucks with fewest completed tasks first (most work remaining)
                tasks.count { it.status == "pending" }
            }
        loading = false
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ── Top bar ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                TextButton(onClick = onBack) {
                    Text("← Back", color = Color.White,
                        fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
            Column(Modifier.padding(start = 4.dp)) {
                Text("ACTIVE REPAIRS",
                    color = Color(0xFFF97318), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("Trucks In Service",
                    color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                if (!loading) {
                    Text(
                        "${trucksInService.size} truck${if (trucksInService.size != 1) "s" else ""} awaiting work",
                        color = Color(0xFF64748B), fontSize = 12.sp
                    )
                }
            }
        }

        // ── Body ──────────────────────────────────────────────────────────
        when {
            loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFFF97318))
                        Spacer(Modifier.height(12.dp))
                        Text("Loading trucks...", color = Color(0xFF64748B), fontSize = 13.sp)
                    }
                }
            }

            trucksInService.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✅", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("No trucks currently in service",
                            color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text("All repairs are complete",
                            color = Color(0xFF64748B), fontSize = 13.sp)
                    }
                }
            }

            else -> {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            "Tap a truck to open its task list",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                        )
                    }

                    items(trucksInService, key = { (truck, _) -> truck.id }) { (truck, tasks) ->
                        val pending   = tasks.count { it.status == "pending" }
                        val done      = tasks.count { it.status == "completed" }
                        val total     = tasks.size
                        val progress  = if (total > 0) done.toFloat() / total else 0f
                        val condColor = when (truck.condition) {
                            "Critical" -> Color(0xFFEF4444)
                            "Poor"     -> Color(0xFFF97318)
                            else       -> Color(0xFF22C55E)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTruckSelected(truck) },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape  = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {

                                // Header row
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            truck.plateNumber,
                                            color      = Color.White,
                                            fontSize   = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            "${truck.kilometers} km  ·  Condition: ${truck.condition}",
                                            color    = Color(0xFF94A3B8),
                                            fontSize = 12.sp
                                        )
                                    }

                                    // Condition badge
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                condColor.copy(alpha = 0.15f),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            truck.condition,
                                            color      = condColor,
                                            fontSize   = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                // Repairs listed
                                if (truck.repairs.isNotEmpty()) {
                                    Text(
                                        "Repairs: ${truck.repairs}",
                                        color    = Color(0xFF64748B),
                                        fontSize = 12.sp
                                    )
                                    Spacer(Modifier.height(8.dp))
                                }

                                // Task summary chips
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TaskChip(
                                        label = "$pending pending",
                                        bgColor = Color(0xFF2A1400),
                                        textColor = Color(0xFFF97318)
                                    )
                                    TaskChip(
                                        label = "$done done",
                                        bgColor = Color(0xFF0D2818),
                                        textColor = Color(0xFF22C55E)
                                    )
                                }

                                Spacer(Modifier.height(10.dp))

                                // Progress bar
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("$done / $total tasks",
                                        color = Color(0xFF64748B), fontSize = 11.sp)
                                    Text("${(progress * 100).toInt()}%",
                                        color = if (progress >= 1f) Color(0xFF22C55E)
                                                else Color(0xFFF97318),
                                        fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress   = { progress },
                                    modifier   = Modifier.fillMaxWidth().height(6.dp),
                                    color      = if (progress >= 1f) Color(0xFF22C55E)
                                                 else Color(0xFFF97318),
                                    trackColor = Color(0xFF334155)
                                )

                                Spacer(Modifier.height(10.dp))
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text("Open Tasks →",
                                        color      = Color(0xFFF97318),
                                        fontSize   = 13.sp,
                                        fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskChip(label: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
