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

// Main Reports screen activity
// Used to display reports
class ReportsActivity : ComponentActivity() {

    // Shared state variable
    // Whenever this number changes, the screen refreshes/recomposes
    private val refreshTrigger = mutableIntStateOf(0)

    // Runs when activity starts
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // it tells the Android system what to display.
        setContent {

            // Open ReportsScreen composable
            ReportsScreen(

                // Pass refresh trigger value
                refreshTrigger = refreshTrigger.intValue,

                // Back button closes current screen
                onBack = { finish() },

                // Logout function
                onLogout = {

                    // Clear logged-in user
                    CurrentUser.clear()

                    // Open Login screen
                    val i = Intent(this, LoginActivity::class.java)

                    // Clear old activities so user cannot go back
                    i.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                    // Start login activity
                    startActivity(i)
                }
            )
        }
    }

    // Runs every time user returns to this screen
    override fun onResume() {
        super.onResume()

        // Increase trigger value
        // This forces ReportsScreen to refresh data
        refreshTrigger.intValue++
    }
}

@Composable
fun ReportsScreen(

    // Trigger for refreshing screen data
    refreshTrigger: Int = 0,

    // Back button function
    onBack: () -> Unit = {},

    // Logout button function
    onLogout: () -> Unit = {}
) {

    // Store trucks list
    var trucks by remember {
        mutableStateOf<List<Truck>>(emptyList())
    }

    // Store tasks list
    var tasks by remember {
        mutableStateOf<List<Task>>(emptyList())
    }

    // Store users list
    var users by remember {
        mutableStateOf<List<User>>(emptyList())
    }

    // Store statistics
    var stats by remember {
        mutableStateOf(FleetStats(0, 0, 0, 0))
    }

    // Loading state
    var loading by remember {
        mutableStateOf(true)
    }

    // Runs whenever refreshTrigger changes
    // Usually after onResume()
    LaunchedEffect(refreshTrigger) {

        // Show loading spinner
        loading = true

        // Get all trucks from repository/database
        trucks = GarageRepository.getAllTrucks()

        // Get all tasks
        tasks = GarageRepository.getAllTasks()

        // Get all users
        users = GarageRepository.getAllUsers()

        // Calculate fleet statistics
        stats = FleetStats(

            // Total trucks
            total = trucks.size,

            // Trucks with pending tasks
            inRepair = trucks.count { truck ->

                // Find tasks for this truck
                tasks.filter {
                    it.truckId == truck.id
                }

                    // Check if any task is still pending
                    .any {
                        it.status == "pending"
                    }
            },

            // Trucks with all tasks completed
            completed = trucks.count { truck ->

                val tt = tasks.filter {// Find tasks for this truck
                    it.truckId == truck.id
                }

                // Truck must have tasks
                // and all tasks must be completed
                tt.isNotEmpty() &&
                        tt.all {
                            it.status == "completed"
                        }
            },

            // Trucks needing attention
            needsAttention = trucks.count {

                // Poor or Critical condition
                it.condition == "Poor" ||
                        it.condition == "Critical"
            }
        )

        // Stop loading
        loading = false
    }

    // Main screen container
    Column(
        Modifier
            .fillMaxSize()

            // Dark background color
            .background(Color(0xFF0F172A))

            // Avoid overlapping system bars
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {

        // ================= TOP BAR =================
        Row(// Top bar container
            modifier = Modifier
                .fillMaxWidth()

                // Dark top bar background
                .background(Color(0xFF1E293B))

                .padding(horizontal = 8.dp, vertical = 4.dp),

            horizontalArrangement = Arrangement.SpaceBetween,

            verticalAlignment = Alignment.CenterVertically
        ) {

            // Left side of top bar
            Row(verticalAlignment = Alignment.CenterVertically) {

                // Back button
                IconButton(onClick = onBack) {

                    TextButton(onClick = onBack) {

                        Text(
                            "← Back",

                            color = Color.White,

                            fontSize = 14.sp,

                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Titles
                Column(Modifier.padding(start = 4.dp)) {

                    Text(
                        "MASTER CONTROL",

                        color = Color(0xFFF97318),

                        fontSize = 11.sp,

                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Fleet Reports",

                        color = Color.White,

                        fontSize = 16.sp,

                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Logout button
            TextButton(onClick = onLogout) {

                Text(
                    "Logout",

                    color = Color(0xFF64748B),

                    fontSize = 13.sp
                )
            }
        }

        // ================= LOADING SCREEN =================
        if (loading) {

            // Center loading content
            Box(
                Modifier.fillMaxSize(),

                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    // Loading spinner
                    CircularProgressIndicator(
                        color = Color(0xFFF97318)
                    )

                    Spacer(Modifier.height(12.dp))

                    // Loading text
                    Text(
                        "Loading reports...",

                        color = Color(0xFF64748B),

                        fontSize = 13.sp
                    )
                }
            }

            // Stop drawing rest of UI while loading
            return@Column
        }

        // ================= MAIN SCROLLABLE CONTENT =================
        LazyColumn(

            Modifier.fillMaxSize(),

            contentPadding = PaddingValues(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            // ================= SUMMARY BOXES =================
            item {

                Row(
                    Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    // Total trucks stat
                    MiniStat(
                        "TOTAL",

                        stats.total.toString(),

                        Color(0xFF94A3B8),

                        Modifier.weight(1f)
                    )

                    // Trucks in repair stat
                    MiniStat(
                        "IN REPAIR",

                        stats.inRepair.toString(),

                        Color(0xFFF97318),

                        Modifier.weight(1f)
                    )

                    // Completed trucks stat
                    MiniStat(
                        "DONE",

                        stats.completed.toString(),

                        Color(0xFF22C55E),

                        Modifier.weight(1f)
                    )

                    // Urgent trucks stat
                    MiniStat(
                        "URGENT",

                        stats.needsAttention.toString(),

                        Color(0xFFEF4444),

                        Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(6.dp))
            }

            // ================= ACTIVE REPAIRS SECTION =================
            item {

                SectionHeader(
                    "ACTIVE REPAIRS",

                    "Trucks currently in service"
                )
            }

            // Get trucks with pending tasks
            val inRepairTrucks = trucks.filter { truck ->

                tasks.filter {
                    it.truckId == truck.id
                }

                    .any {
                        it.status == "pending"
                    }
            }

            // If no trucks are being repaired
            if (inRepairTrucks.isEmpty()) {

                item {

                    Text(
                        "No trucks currently in repair",

                        color = Color(0xFF64748B),

                        fontSize = 13.sp,

                        modifier =
                            Modifier.padding(vertical = 8.dp)
                    )
                }

            } else {

                // Display each repair card
                items(inRepairTrucks) { truck ->

                    // Get tasks for this truck
                    val truckTasks = tasks.filter {
                        it.truckId == truck.id
                    }

                    // Count completed tasks
                    val done = truckTasks.count {
                        it.status == "completed"
                    }

                    // Total tasks
                    val total = truckTasks.size

                    // Check if all tasks completed
                    val allDone =
                        total > 0 && done == total

                    // Show repair card
                    ActiveRepairCard(
                        truck,
                        done,
                        total,
                        allDone
                    )
                }
            }

            // ================= MECHANIC ACTIVITY SECTION =================
            item {

                Spacer(Modifier.height(6.dp))

                SectionHeader(
                    "MECHANIC ACTIVITY",

                    "Tasks completed by each mechanic"
                )
            }

            // Get only mechanics
            val mechanics = users.filter {
                it.role == "mechanic"
            }

            // If no mechanics found
            if (mechanics.isEmpty()) {

                item {

                    Text(
                        "No mechanic data available",

                        color = Color(0xFF64748B),

                        fontSize = 13.sp
                    )
                }

            } else {

                // Display mechanic activity cards
                items(mechanics) { mechanic ->

                    // Tasks assigned to this mechanic
                    val myTasks = tasks.filter {
                        it.mechanicId == mechanic.id
                    }

                    // Completed tasks count
                    val done = myTasks.count {
                        it.status == "completed"
                    }

                    // Show mechanic card
                    MechanicActivityCard(
                        mechanic,
                        myTasks,
                        trucks,
                        done,
                        myTasks.size
                    )
                }
            }

            // Bottom spacing
            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}