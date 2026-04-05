package com.example.uidata.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.uidata.ui.theme.UidataTheme

// Temporary data model until the Data Layer is ready
data class TaskItem(
    val id: String,
    val description: String,
    val isCompleted: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen() {
    // Sample data
    val tasks = remember {
        mutableStateListOf(
            TaskItem("1", "Change oil filter", false),
            TaskItem("2", "Check tire pressure", true),
            TaskItem("3", "Replace brake pads", false),
            TaskItem("4", "Refill coolant", false)
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Service Tasks") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks) { task ->
                TaskRow(
                    task = task,
                    onCheckedChange = { isChecked ->
                        val index = tasks.indexOf(task)
                        if (index != -1) {
                            tasks[index] = task.copy(isCompleted = isChecked)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TaskRow(
    task: TaskItem,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyLarge
            )
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0x2E, 0x7D, 0x32) // Verified Green from Brand Toolkit
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskListScreenPreview() {
    UidataTheme {
        TaskListScreen()
    }
}