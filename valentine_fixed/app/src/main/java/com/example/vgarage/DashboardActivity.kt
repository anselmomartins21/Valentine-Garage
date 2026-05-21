package com.example.vgarage

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class DashboardActivity : ComponentActivity() {

    // Incremented in onResume so the composable re-fetches every time we return here
    private val refreshTrigger = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashboardScreen(
                refreshTrigger  = refreshTrigger.intValue,
                onNewCheckIn    = { startActivity(Intent(this, CheckInActivity::class.java)) },
                // Navigate to the dedicated full screen instead of a fragile dialog
                onViewInService = { startActivity(Intent(this, TrucksInServiceActivity::class.java)) },
                onLogout = {
                    CurrentUser.clear()
                    val i = Intent(this, LoginActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(i)
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
fun DashboardScreen(
    refreshTrigger:  Int        = 0,
    onNewCheckIn:    () -> Unit = {},
    onViewInService: () -> Unit = {},
    onLogout:        () -> Unit = {}
) {
    var stats   by remember { mutableStateOf(FleetStats(0, 0, 0, 0)) }
    var loading by remember { mutableStateOf(true) }

    // Re-fetches whenever refreshTrigger changes (every onResume)
    LaunchedEffect(refreshTrigger) {
        loading = true
        try {
            val trucks = GarageRepository.getAllTrucks()
            val tasks  = GarageRepository.getAllTasks()
            val inRepair = trucks.count { truck ->
                tasks.filter { it.truckId == truck.id }.any { it.status == "pending" }
            }
            stats = FleetStats(
                total = trucks.size,
                inRepair = inRepair,
                completed = trucks.count { truck ->
                    val tt = tasks.filter { it.truckId == truck.id }
                    tt.isNotEmpty() && tt.all { it.status == "completed" }
                },
                needsAttention = trucks.count {
                    it.condition == "Poor" || it.condition == "Critical"
                }
            )
        } catch (e: Exception) {
            android.util.Log.e("VGARAGE", "Dashboard refresh error: ${e.message}")
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
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
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text("MASTER CONTROL",
                    color = Color(0xFFF97318), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Fleet Overview",
                    color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Welcome, ${CurrentUser.name}",
                    color = Color(0xFF64748B), fontSize = 12.sp)
            }
            TextButton(onClick = onLogout) {
                Text("Logout", color = Color(0xFF64748B), fontSize = 13.sp)
            }
        }

        LazyColumn(
            modifier            = Modifier.fillMaxSize(),
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Action buttons ────────────────────────────────────────────
            item {
                // Primary: New Check-In
                Button(
                    onClick  = onNewCheckIn,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97318)),
                    shape    = RoundedCornerShape(8.dp)
                ) {
                    Text("+ NEW CHECK-IN",
                        fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                }
                Spacer(Modifier.height(8.dp))

                // Secondary: navigate to TrucksInServiceActivity (proper screen, no dialog)
                OutlinedButton(
                    onClick  = onViewInService,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(8.dp),
                    border   = BorderStroke(1.5.dp, Color(0xFFF97318)),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFF97318))
                ) {
                    Text(
                        "🔧  VIEW TRUCKS IN SERVICE  (${if (loading) "…" else stats.inRepair.toString()})",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 13.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // ── Stat cards ────────────────────────────────────────────────
            item {
                if (loading) {
                    Box(
                        Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFF97318))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                        // IN REPAIR — tapping the card also navigates to the service screen
                        Box(modifier = Modifier.clickable { onViewInService() }) {
                            StatCard(
                                label = if (stats.inRepair > 0)
                                    "IN REPAIR  (tap to view)" else "IN REPAIR",
                                value       = stats.inRepair.toString().padStart(2, '0'),
                                accentColor = Color(0xFFF97318),
                                icon        = "🔧"
                            )
                        }

                        StatCard(
                            label       = "COMPLETED",
                            value       = stats.completed.toString().padStart(2, '0'),
                            accentColor = Color(0xFF334155),
                            icon        = "✅"
                        )

                        StatCard(
                            label       = "NEEDS ATTENTION",
                            value       = stats.needsAttention.toString().padStart(2, '0'),
                            accentColor = Color(0xFFEF4444),
                            icon        = "⚠️"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, accentColor: Color, icon: String) {
    val hasAccent = accentColor != Color(0xFF334155)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape    = RoundedCornerShape(8.dp),
        border   = if (hasAccent) BorderStroke(1.5.dp, accentColor) else null
    ) {
        Box(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Column {
                Text(label,
                    color      = if (hasAccent) accentColor else Color(0xFF94A3B8),
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(value,
                    color      = if (hasAccent) accentColor else Color.White,
                    fontSize   = 42.sp,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Box(
                    Modifier
                        .width(28.dp)
                        .height(2.dp)
                        .background(if (hasAccent) accentColor else Color(0xFF475569))
                )
            }
            Text(icon,
                modifier  = Modifier.align(Alignment.CenterEnd).alpha(0.18f),
                fontSize  = 52.sp)
        }
    }
}
