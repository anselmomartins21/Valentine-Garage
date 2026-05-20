package com.example.vgarage

import android.os.Bundle
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class TasksActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val truckId = intent.getStringExtra("truckId") ?: ""
        val plate   = intent.getStringExtra("plate")   ?: ""
        setContent {
            TasksScreen(truckId = truckId, plate = plate, onBack = { finish() })
        }
    }
}

@Composable
fun TasksScreen(truckId: String, plate: String, onBack: () -> Unit = {}) {
    val scope   = rememberCoroutineScope()
    var tasks   by remember { mutableStateOf<List<Task>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(truckId) {
        tasks   = GarageRepository.getTasksForTruck(truckId)
        loading = false
    }

    val completedCount = tasks.count { it.status == "completed" }
    val progress = if (tasks.isNotEmpty())
        completedCount.toFloat() / tasks.size else 0f

    Column(Modifier.fillMaxSize().background(Color(0xFF0F172A)).statusBarsPadding().navigationBarsPadding()) {

        // Top bar with progress
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(start = 4.dp, end = 16.dp,
                    top = 4.dp, bottom = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    TextButton(onClick = onBack) {
                        Text("← Back", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Column {
                    Text("SERVICE TASKS",
                        color = Color(0xFFF97318),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold)
                    Text("Tasks — $plate",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold)
                }
            }

            // Progress indicator
            if (!loading && tasks.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("$completedCount / ${tasks.size} tasks completed",
                        color = Color(0xFF64748B), fontSize = 12.sp)
                    Text("${(progress * 100).toInt()}%",
                        color = if (progress >= 1f)
                            Color(0xFF22C55E) else Color(0xFFF97318),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(6.dp),
                    color = if (progress >= 1f)
                        Color(0xFF22C55E) else Color(0xFFF97318),
                    trackColor = Color(0xFF334155)
                )
            }
        }

        when {
            loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFFF97318))
                        Spacer(Modifier.height(12.dp))
                        Text("Loading tasks...",
                            color = Color(0xFF64748B), fontSize = 13.sp)
                    }
                }
            }
            tasks.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tasks found for this truck",
                        color = Color(0xFF64748B), fontSize = 14.sp)
                }
            }
            else -> {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskCard(task) { notes ->
                            scope.launch {
                                GarageRepository.completeTask(task.id, notes)
                                tasks = tasks.map {
                                    if (it.id == task.id)
                                        it.copy(status = "completed", notes = notes)
                                    else it
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
fun TaskCard(task: Task, onMarkDone: (String) -> Unit) {
    var notes  by remember { mutableStateOf(task.notes) }
    val isDone = task.status == "completed"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isDone,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(
                        checkedColor   = Color(0xFFF97318),
                        uncheckedColor = Color(0xFF334155)
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text(task.description,
                    color      = if (isDone) Color(0xFF64748B) else Color.White,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value         = notes,
                onValueChange = { if (!isDone) notes = it },
                placeholder   = {
                    Text("Add notes about what was done...",
                        color = Color(0xFF475569), fontSize = 13.sp)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled  = !isDone,
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor        = Color.White,
                    unfocusedTextColor      = Color.White,
                    focusedContainerColor   = Color(0xFF0F172A),
                    unfocusedContainerColor = Color(0xFF0F172A),
                    focusedBorderColor      = Color(0xFFF97318),
                    unfocusedBorderColor    = Color(0xFF334155),
                    disabledTextColor       = Color(0xFF64748B),
                    disabledContainerColor  = Color(0xFF0F172A),
                    disabledBorderColor     = Color(0xFF1E293B)
                )
            )

            Spacer(Modifier.height(10.dp))

            if (!isDone) {
                Button(
                    onClick = { onMarkDone(notes) },
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF97318)),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Mark Done", color = Color.White, fontSize = 12.sp)
                }
            } else {
                Text("✓ Completed",
                    color = Color(0xFF22C55E), fontSize = 12.sp,
                    fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun TasksPreview() {
    // Fake data so preview shows content
    val fakeTasks = listOf(
        Task("1","t","m","Oil Change",     "completed", "Used 5W-30 oil"),
        Task("2","t","m","Brake Inspection","completed", "Pads replaced"),
        Task("3","t","m","Tyre Check",      "pending",   ""),
        Task("4","t","m","Fluid Levels",    "pending",   ""),
        Task("5","t","m","Engine Diagnostics","pending", "")
    )
    var tasks by remember { mutableStateOf(fakeTasks) }
    Column(Modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        Column(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF1E293B))
                .padding(16.dp)
        ) {
            Text("SERVICE TASKS", color = Color(0xFFF97318),
                fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("Tasks — N 12345 W", color = Color.White,
                fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            val done = tasks.count { it.status == "completed" }
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$done / ${tasks.size} tasks completed",
                    color = Color(0xFF64748B), fontSize = 12.sp)
                Text("${(done * 100 / tasks.size)}%",
                    color = Color(0xFFF97318), fontSize = 12.sp,
                    fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { done.toFloat() / tasks.size },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = Color(0xFFF97318), trackColor = Color(0xFF334155)
            )
        }
        LazyColumn(contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tasks) { task ->
                TaskCard(task) { notes ->
                    tasks = tasks.map {
                        if (it.id == task.id) it.copy(status="completed", notes=notes) else it
                    }
                }
            }
        }
    }
}
