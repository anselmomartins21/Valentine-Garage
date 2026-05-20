package com.example.vgarage

import android.content.Intent
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ReportsActivity : ComponentActivity() {

    // Shared state so onResume can trigger a recompose-driven refresh
    private val refreshTrigger = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReportsScreen(
                refreshTrigger = refreshTrigger.intValue,
                onBack = { finish() },
                onLogout = {
                    CurrentUser.clear()
                    val i = Intent(this, LoginActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(i)
                }
            )
        }
    }

    /** Refresh data every time admin returns to this screen */
    override fun onResume() {
        super.onResume()
        refreshTrigger.intValue++
    }
}

@Composable
fun ReportsScreen(
    refreshTrigger: Int       = 0,
    onBack:         () -> Unit = {},
    onLogout:       () -> Unit = {}
) {
    var trucks  by remember { mutableStateOf<List<Truck>>(emptyList()) }
    var tasks   by remember { mutableStateOf<List<Task>>(emptyList()) }
    var users   by remember { mutableStateOf<List<User>>(emptyList()) }
    var stats   by remember { mutableStateOf(FleetStats(0, 0, 0, 0)) }
    var loading by remember { mutableStateOf(true) }

    // Re-fetches on every onResume (refreshTrigger change)
    LaunchedEffect(refreshTrigger) {
        loading = true
        // Fetch all data in a single pass
        trucks  = GarageRepository.getAllTrucks()
        tasks   = GarageRepository.getAllTasks()
        users   = GarageRepository.getAllUsers()
        // Compute stats from the data we just fetched (no extra DB round-trips)
        stats = FleetStats(
            total = trucks.size,
            inRepair = trucks.count { truck ->
                tasks.filter { it.truckId == truck.id }.any { it.status == "pending" }
            },
            completed = trucks.count { truck ->
                val tt = tasks.filter { it.truckId == truck.id }
                tt.isNotEmpty() && tt.all { it.status == "completed" }
            },
            needsAttention = trucks.count {
                it.condition == "Poor" || it.condition == "Critical"
            }
        )
        loading = false
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    TextButton(onClick = onBack) {
                        Text("← Back", color = Color.White,
                            fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Column(Modifier.padding(start = 4.dp)) {
                    Text("MASTER CONTROL",
                        color = Color(0xFFF97318), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Fleet Reports",
                        color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            TextButton(onClick = onLogout) {
                Text("Logout", color = Color(0xFF64748B), fontSize = 13.sp)
            }
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFFF97318))
                    Spacer(Modifier.height(12.dp))
                    Text("Loading reports...", color = Color(0xFF64748B), fontSize = 13.sp)
                }
            }
            return@Column
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Summary boxes ─────────────────────────────────────────────────
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniStat("TOTAL",     stats.total.toString(),         Color(0xFF94A3B8), Modifier.weight(1f))
                    MiniStat("IN REPAIR", stats.inRepair.toString(),      Color(0xFFF97318), Modifier.weight(1f))
                    MiniStat("DONE",      stats.completed.toString(),     Color(0xFF22C55E), Modifier.weight(1f))
                    MiniStat("URGENT",    stats.needsAttention.toString(), Color(0xFFEF4444), Modifier.weight(1f))
                }
                Spacer(Modifier.height(6.dp))
            }

            // ── Active repairs ────────────────────────────────────────────────
            item {
                SectionHeader("ACTIVE REPAIRS", "Trucks currently in service")
            }

            val inRepairTrucks = trucks.filter { truck ->
                tasks.filter { it.truckId == truck.id }.any { it.status == "pending" }
            }

            if (inRepairTrucks.isEmpty()) {
                item {
                    Text(
                        "No trucks currently in repair",
                        color    = Color(0xFF64748B),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(inRepairTrucks) { truck ->
                    val truckTasks = tasks.filter { it.truckId == truck.id }
                    val done   = truckTasks.count { it.status == "completed" }
                    val total  = truckTasks.size
                    val allDone = total > 0 && done == total
                    ActiveRepairCard(truck, done, total, allDone)
                }
            }

            // ── Mechanic activity ─────────────────────────────────────────────
            item {
                Spacer(Modifier.height(6.dp))
                SectionHeader("MECHANIC ACTIVITY", "Tasks completed by each mechanic")
            }

            val mechanics = users.filter { it.role == "mechanic" }

            if (mechanics.isEmpty()) {
                item {
                    Text(
                        "No mechanic data available",
                        color    = Color(0xFF64748B),
                        fontSize = 13.sp
                    )
                }
            } else {
                items(mechanics) { mechanic ->
                    val myTasks = tasks.filter { it.mechanicId == mechanic.id }
                    val done    = myTasks.count { it.status == "completed" }
                    MechanicActivityCard(mechanic, myTasks, trucks, done, myTasks.size)
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun MiniStat(label: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors   = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape    = RoundedCornerShape(8.dp)
    ) {
        Column(
            Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(label,  color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(title,    color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Color(0xFF475569), fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun ActiveRepairCard(truck: Truck, done: Int, total: Int, allDone: Boolean) {
    val statusText  = if (allDone) "COMPLETED" else "IN REPAIR"
    val statusColor = if (allDone) Color(0xFF22C55E) else Color(0xFFF97318)
    val statusBg    = if (allDone) Color(0xFF0D2818) else Color(0xFF2A1400)
    val progress    = if (total > 0) done.toFloat() / total else 0f

    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, if (allDone) Color(0xFF22C55E) else Color(0xFF334155)),
        shape  = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(truck.plateNumber,
                        color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    Modifier
                        .background(statusBg, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(statusText, color = statusColor,
                        fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("${truck.kilometers} km  ·  Condition: ${truck.condition}",
                color = Color(0xFF94A3B8), fontSize = 12.sp)
            if (truck.repairs.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text("Repairs: ${truck.repairs}",
                    color = Color(0xFF64748B), fontSize = 12.sp)
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$done / $total tasks done",
                    color = Color(0xFF64748B), fontSize = 11.sp)
                Text("${(progress * 100).toInt()}%",
                    color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress   = { progress },
                modifier   = Modifier.fillMaxWidth().height(4.dp),
                color      = statusColor,
                trackColor = Color(0xFF334155)
            )
        }
    }
}

@Composable
fun MechanicActivityCard(
    mechanic:  User,
    myTasks:   List<Task>,
    allTrucks: List<Truck>,
    done:      Int,
    total:     Int
) {
    val progress = if (total > 0) done.toFloat() / total else 0f
    val allDone  = total > 0 && done == total
    val completed = myTasks.filter { it.status == "completed" }

    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape  = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(mechanic.name,
                    color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("$done / $total tasks",
                    color = Color(0xFF94A3B8), fontSize = 13.sp)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress   = { progress },
                modifier   = Modifier.fillMaxWidth().height(6.dp),
                color      = if (allDone) Color(0xFF22C55E) else Color(0xFFF97318),
                trackColor = Color(0xFF334155)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (allDone) "All done ✓"
                else "${(progress * 100).toInt()}% done  ·  ${total - done} remaining",
                color    = if (allDone) Color(0xFF22C55E) else Color(0xFF64748B),
                fontSize = 12.sp
            )

            // Completed task log
            if (completed.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFF334155))
                Spacer(Modifier.height(8.dp))
                Text("COMPLETED TASKS",
                    color = Color(0xFF475569), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                completed.forEach { task ->
                    val truck = allTrucks.find { it.id == task.truckId }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text("✓ ", color = Color(0xFF22C55E), fontSize = 13.sp)
                        Column {
                            Text(task.description,
                                color = Color(0xFFCBD5E1), fontSize = 13.sp)
                            if (truck != null) {
                                Text("Truck: ${truck.plateNumber}",
                                    color = Color(0xFF64748B), fontSize = 11.sp)
                            }
                            if (task.notes.isNotEmpty()) {
                                Text("Notes: ${task.notes}",
                                    color     = Color(0xFF64748B),
                                    fontSize  = 11.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun ReportsPreview() {
    val trucks = listOf(
        Truck("1", "N 12345 W", "Good",  45200,  "", "", "Engine/Oil Issues",   "Noise from engine"),
        Truck("2", "N 98760 W", "Poor",  112000, "", "", "Body Dents or Scratches", ""),
    )
    val tasks = listOf(
        Task("1", "1", "m1", "Engine/Oil Issues",       "completed", "Changed oil"),
        Task("2", "1", "m1", "Brake Problems",           "completed", "Pads replaced"),
        Task("3", "1", "m1", "Tire Tread/Sidewall Damage", "pending", ""),
        Task("4", "2", "m2", "Body Dents or Scratches",  "completed", "Dents fixed"),
        Task("5", "2", "m2", "Exterior Lighting Faults", "pending",   ""),
    )
    val users = listOf(
        User("m1", "John Doe",   "john@vgarage.com",  "mechanic", "1111"),
        User("m2", "Maria Doe",  "maria@vgarage.com", "mechanic", "2222")
    )
    val fStats = FleetStats(2, 2, 0, 1)

    Column(Modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        Row(
            Modifier.fillMaxWidth().background(Color(0xFF1E293B)).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text("MASTER CONTROL", color = Color(0xFFF97318),
                    fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("Fleet Reports", color = Color.White,
                    fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = {}) {
                Text("Logout", color = Color(0xFF64748B), fontSize = 13.sp)
            }
        }
        LazyColumn(
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStat("TOTAL",     fStats.total.toString(),          Color(0xFF94A3B8), Modifier.weight(1f))
                    MiniStat("IN REPAIR", fStats.inRepair.toString(),       Color(0xFFF97318), Modifier.weight(1f))
                    MiniStat("DONE",      fStats.completed.toString(),      Color(0xFF22C55E), Modifier.weight(1f))
                    MiniStat("URGENT",    fStats.needsAttention.toString(), Color(0xFFEF4444), Modifier.weight(1f))
                }
            }
            item { SectionHeader("ACTIVE REPAIRS", "Trucks currently in service") }
            items(trucks) { truck ->
                val tt = tasks.filter { it.truckId == truck.id }
                val d  = tt.count { it.status == "completed" }
                ActiveRepairCard(truck, d, tt.size, d == tt.size && tt.isNotEmpty())
            }
            item { SectionHeader("MECHANIC ACTIVITY", "Tasks completed by each mechanic") }
            items(users.filter { it.role == "mechanic" }) { mechanic ->
                val mt   = tasks.filter { it.mechanicId == mechanic.id }
                val done = mt.count { it.status == "completed" }
                MechanicActivityCard(mechanic, mt, trucks, done, mt.size)
            }
        }
    }
}
