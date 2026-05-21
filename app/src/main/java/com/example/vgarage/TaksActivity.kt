package com.example.vgarage

// Android lifecycle tools
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

// ================= MAIN ACTIVITY =================
class TasksActivity : ComponentActivity() { //→ The colon means inheritance. TasksActivity is a subclass of ComponentActivity. That gives it all the behavior of an Android activity (like lifecycle management: onCreate, onStart, etc.).

    // Runs when screen opens
    override fun onCreate(savedInstanceState: Bundle?) {
            // Initialize activity
        super.onCreate(savedInstanceState)

        // Get truck ID passed from previous screen
        val truckId =
            intent.getStringExtra("truckId") ?: ""

        // Get truck plate number
        val plate =
            intent.getStringExtra("plate") ?: ""

        // Set Compose UI
        // function that Display screen
        setContent {

            // Open TasksScreen composable
            TasksScreen(

                // Pass truck information
                truckId = truckId,
                plate = plate,

                // Back button closes screen
                onBack = { finish() }
            )
        }
    }
}

// ================= MAIN TASKS SCREEN =================
@Composable // marks a function as a piece of UI that can update itself when data changes.
fun TasksScreen(

    // Truck ID used to fetch tasks
    truckId: String,

    // Truck plate number shown on screen
    plate: String,

    // Back button action
    onBack: () -> Unit = {}
) {

    // Coroutine scope for background operations
    val scope = rememberCoroutineScope() //rememberCoroutineScope() gives you a special helper in Jetpack Compose that lets you run background work (coroutines) safely.

    // Store all tasks
    var tasks by remember {
        mutableStateOf<List<Task>>(emptyList())
    }

    // Loading state
    //Make a reactive variable called loading, starting as true. If I change it, Jetpack Compose will automatically update the UI.
    var loading by remember {
        mutableStateOf(true)
    }

    // Runs when truckId changes
    // Used to load tasks from repository/database
    LaunchedEffect(truckId) { //Do this background work when the screen shows, and redo it if the truck ID changes.

        // Fetch tasks for selected truck
        tasks = GarageRepository.getTasksForTruck(truckId)

        // Stop loading
        loading = false
    }

    // Count completed tasks
    val completedCount = tasks.count {
        it.status == "completed"
    }

    // Calculate progress percentage
    val progress =
        if (tasks.isNotEmpty())

            completedCount.toFloat() / tasks.size

        else 0f

    // ================= MAIN SCREEN LAYOUT =================
    Column(// Main screen container

        Modifier
            .fillMaxSize() //Tells the composable (like your Column) to expand and take up all available width and height of its parent container.

            // Dark background color
            .background(Color(0xFF0F172A))

            // Avoid overlap with system bars
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {

        // ================= TOP BAR =================
        Column(
            modifier = Modifier
                .fillMaxWidth()

                // Top bar background
                .background(Color(0xFF1E293B))

                .padding(
                    start = 4.dp,
                    end = 16.dp,
                    top = 4.dp,
                    bottom = 12.dp
                )
        ) {

            // Top row containing back button and titles
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

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

                // Screen titles
                Column {

                    Text(
                        "SERVICE TASKS",

                        color = Color(0xFFF97318),

                        fontSize = 11.sp,

                        fontWeight = FontWeight.Bold
                    )

                    // Display truck plate
                    Text(
                        "Tasks — $plate",

                        color = Color.White,

                        fontSize = 18.sp,

                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ================= PROGRESS BAR SECTION =================

            // Only show if tasks loaded successfully
            if (!loading && tasks.isNotEmpty()) {//If we are not loading anymore AND the tasks list has items, then run the code inside the block.

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),

                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    // Show completed task count
                    Text(
                        "$completedCount / ${tasks.size} tasks completed",

                        color = Color(0xFF64748B),

                        fontSize = 12.sp
                    )

                    // Show percentage completed
                    Text(
                        "${(progress * 100).toInt()}%",

                        // Green if all tasks completed
                        color =
                            if (progress >= 1f)
                                Color(0xFF22C55E)

                            // Orange if still in progress
                            else Color(0xFFF97318),

                        fontSize = 12.sp,

                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(6.dp))

                // Visual progress bar
                LinearProgressIndicator(

                    // Progress value
                    progress = { progress },

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(6.dp),

                    // Progress color
                    color =
                        if (progress >= 1f)
                            Color(0xFF22C55E)
                        else
                            Color(0xFFF97318),

                    // Background track color
                    trackColor = Color(0xFF334155)
                )
            }
        }

        // ================= SCREEN STATES =================
        when {

            // ================= LOADING STATE =================
            loading -> {

                Box(
                    Modifier.fillMaxSize(),

                    contentAlignment =
                        Alignment.Center
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

                        // Loading message
                        Text(
                            "Loading tasks...",

                            color = Color(0xFF64748B),

                            fontSize = 13.sp
                        )
                    }
                }
            }

            // ================= EMPTY TASKS STATE =================
            tasks.isEmpty() -> {

                Box(
                    Modifier.fillMaxSize(),

                    contentAlignment =
                        Alignment.Center
                ) {

                    // Message if no tasks exist
                    Text(
                        "No tasks found for this truck",

                        color = Color(0xFF64748B),

                        fontSize = 14.sp
                    )
                }
            }

            // ================= TASK LIST STATE =================
            else -> {

                // Scrollable list of tasks
                LazyColumn(

                    Modifier.fillMaxSize(),

                    contentPadding =
                        PaddingValues(12.dp),

                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    // Display each task
                    items(
                        tasks,

                        // Use task ID as unique key
                        key = { it.id }
                    ) { task ->

                        // Display task card
                        TaskCard(task) { notes ->

                            // Run background operation
                            scope.launch {

                                // Save completed task
                                GarageRepository.completeTask(
                                    task.id,
                                    notes
                                )

                                // Update task list in UI
                                tasks = tasks.map {

                                    // Update selected task
                                    if (it.id == task.id)

                                        it.copy(
                                            status = "completed",
                                            notes = notes
                                        )

                                    // Leave other tasks unchanged
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

// ================= TASK CARD =================
@Composable
fun TaskCard(

    // Current task object
    task: Task,

    // Function called when task completed
    onMarkDone: (String) -> Unit
) {

    // Store notes entered by user
    var notes by remember {
        mutableStateOf(task.notes)
    }

    // Check if task completed
    val isDone =
        task.status == "completed"

    // Task container card
    Card(

        modifier = Modifier.fillMaxWidth(),

        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B)
        )
    ) {

        Column(Modifier.padding(16.dp)) {

            // ================= TASK HEADER =================
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                // Checkbox showing completion status
                Checkbox(

                    checked = isDone,

                    // Disabled manual clicking
                    onCheckedChange = null,

                    colors = CheckboxDefaults.colors(

                        // Orange when checked
                        checkedColor =
                            Color(0xFFF97318),

                        // Dark color when unchecked
                        uncheckedColor =
                            Color(0xFF334155)
                    )
                )

                Spacer(Modifier.width(8.dp))

                // Task description
                Text(
                    task.description,

                    // Grey if completed
                    color =
                        if (isDone)
                            Color(0xFF64748B)
                        else
                            Color.White,

                    fontSize = 15.sp,

                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(10.dp))

            // ================= NOTES FIELD =================
            OutlinedTextField(

                // Current notes value
                value = notes,

                // Update notes while typing
                onValueChange = {

                    // Prevent editing after completion
                    if (!isDone)
                        notes = it
                },

                // Placeholder text
                placeholder = {

                    Text(
                        "Add notes about what was done...",

                        color = Color(0xFF475569),

                        fontSize = 13.sp
                    )
                },

                modifier = Modifier.fillMaxWidth(),

                // Disable field if task completed
                enabled = !isDone,

                // Minimum visible lines
                minLines = 2,

                // Text field colors
                colors = OutlinedTextFieldDefaults.colors(

                    focusedTextColor = Color.White,

                    unfocusedTextColor = Color.White,

                    focusedContainerColor =
                        Color(0xFF0F172A),

                    unfocusedContainerColor =
                        Color(0xFF0F172A),

                    focusedBorderColor =
                        Color(0xFFF97318),

                    unfocusedBorderColor =
                        Color(0xFF334155),

                    disabledTextColor =
                        Color(0xFF64748B),

                    disabledContainerColor =
                        Color(0xFF0F172A),

                    disabledBorderColor =
                        Color(0xFF1E293B)
                )
            )

            Spacer(Modifier.height(10.dp))

            // ================= BUTTON / COMPLETED STATUS =================

            // Show button only if task incomplete
            if (!isDone) {

                Button(

                    // Mark task as done
                    onClick = {
                        onMarkDone(notes)
                    },

                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFFF97318)
                    ),

                    modifier =
                        Modifier.align(Alignment.End)
                ) {

                    Text(
                        "Mark Done",

                        color = Color.White,

                        fontSize = 12.sp
                    )
                }

            } else {

                // Show completed message
                Text(
                    "✓ Completed",

                    color = Color(0xFF22C55E),

                    fontSize = 12.sp,

                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ================= PREVIEW SCREEN =================
@Preview(
    showBackground = true,

    backgroundColor = 0xFF0F172A
)
@Composable
fun TasksPreview() {

    // Fake data used only for Android Studio preview
    val fakeTasks = listOf(

        Task(
            "1",
            "t",
            "m",
            "Oil Change",
            "completed",
            "Used 5W-30 oil"
        ),

        Task(
            "2",
            "t",
            "m",
            "Brake Inspection",
            "completed",
            "Pads replaced"
        ),

        Task(
            "3",
            "t",
            "m",
            "Tyre Check",
            "pending",
            ""
        ),

        Task(
            "4",
            "t",
            "m",
            "Fluid Levels",
            "pending",
            ""
        ),

        Task(
            "5",
            "t",
            "m",
            "Engine Diagnostics",
            "pending",
            ""
        )
    )

    // Preview state
    var tasks by remember {
        mutableStateOf(fakeTasks)
    }

    // Main preview container
    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {

        // ================= PREVIEW TOP BAR =================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(16.dp)
        ) {

            Text(
                "SERVICE TASKS",

                color = Color(0xFFF97318),

                fontSize = 11.sp,

                fontWeight = FontWeight.Bold
            )

            Text(
                "Tasks — N 12345 W",

                color = Color.White,

                fontSize = 18.sp,

                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            // Count completed tasks
            val done = tasks.count {
                it.status == "completed"
            }

            Row(
                Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    "$done / ${tasks.size} tasks completed",

                    color = Color(0xFF64748B),

                    fontSize = 12.sp
                )

                Text(
                    "${(done * 100 / tasks.size)}%",

                    color = Color(0xFFF97318),

                    fontSize = 12.sp,

                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(6.dp))

            // Progress bar preview
            LinearProgressIndicator(

                progress = {
                    done.toFloat() / tasks.size
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),

                color = Color(0xFFF97318),

                trackColor = Color(0xFF334155)
            )
        }

        // ================= PREVIEW TASK LIST =================
        LazyColumn(

            contentPadding =
                PaddingValues(12.dp),

            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            // Display preview task cards
            items(tasks) { task ->

                TaskCard(task) { notes ->

                    // Update preview state only
                    tasks = tasks.map {

                        if (it.id == task.id)

                            it.copy(
                                status = "completed",
                                notes = notes
                            )

                        else it
                    }
                }
            }
        }
    }
}