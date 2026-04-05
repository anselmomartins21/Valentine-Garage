package com.example.uidata.uiProject

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uidata.R
import com.example.uidata.ui.theme.UidataTheme

data class ServiceTask(
    val id: Int,
    val labelRes: Int,
    val iconRes: Int,
    val activeColor: Color,
    val activeTint: Color,
    var isCompleted: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val tasks = remember {
        mutableStateListOf(
            ServiceTask(1, R.string.change_oil_filter, R.drawable.ic_oil_filter, Color(0xFFFFF8E1), Color(0xFFFFB300)),
            ServiceTask(2, R.string.check_tire_pressure, R.drawable.ic_tire_pressure, Color(0xFFE1F5FE), Color(0xFF039BE5)),
            ServiceTask(3, R.string.replace_brake_pads, R.drawable.ic_task_alt, Color(0xFFF3E5F5), Color(0xFF8E24AA)),
            ServiceTask(4, R.string.refill_coolant, R.drawable.ic_task_alt, Color(0xFFE8F5E9), Color(0xFF43A047))
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(id = R.string.service_tasks),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 48.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_arrow),
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            tasks.forEachIndexed { index, task ->
                MenuItem(
                    iconRes = task.iconRes,
                    label = stringResource(id = task.labelRes),
                    isCompleted = task.isCompleted,
                    activeBgColor = task.activeColor,
                    activeTint = task.activeTint,
                    onClick = {
                        tasks[index] = task.copy(isCompleted = !task.isCompleted)
                    }
                )
            }
        }
    }
}

@Composable
fun MenuItem(
    iconRes: Int,
    label: String,
    isCompleted: Boolean,
    activeBgColor: Color,
    activeTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val bgColor = if (isCompleted) activeBgColor else Color(0xFFF5F5F5)
    val tintColor = if (isCompleted) activeTint else Color(0xFFBDBDBD)
    val labelColor = if (isCompleted) Color.Black else Color.Gray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(160.dp)
            .clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(20.dp),
            color = bgColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = if (isCompleted) R.drawable.ic_task_alt else iconRes),
                    contentDescription = label,
                    modifier = Modifier.size(36.dp),
                    tint = tintColor
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Medium,
            color = labelColor,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TaskListScreenPreview() {
    UidataTheme {
        TaskListScreen()
    }
}
