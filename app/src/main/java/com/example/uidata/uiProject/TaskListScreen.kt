package com.example.uidata.uiProject

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Task(val id: String, val description: String, var isCompleted: Boolean = false)

@Composable
fun TaskListScreen() {
    var tasks by remember {
        mutableStateOf(
            listOf(
                Task("1", "Change oil filter"),
                Task("2", "Check tire pressure"),
                Task("3", "Replace air filter")
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tasks to do:", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(tasks) { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(task.description)
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { isChecked ->
                            tasks = tasks.map {
                                if (it.id == task.id) it.copy(isCompleted = isChecked) else it
                            }
                        }
                    )
                }
            }
        }
    }
}

