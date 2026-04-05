package com.example.uidata.uiProject

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
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
            MenuItem(
                iconRes = R.drawable.ic_oil_filter,
                label = stringResource(id = R.string.change_oil_filter),
                iconBgColor = Color(0xFFF1F5F9)
            )
            MenuItem(
                iconRes = R.drawable.ic_tire_pressure,
                label = stringResource(id = R.string.check_tire_pressure),
                iconBgColor = Color(0xFFFDF2F0)
            )
            MenuItem(
                iconRes = R.drawable.ic_task_alt,
                label = stringResource(id = R.string.replace_brake_pads),
                iconBgColor = Color(0xFFEDF7F1)
            )
            MenuItem(
                iconRes = R.drawable.ic_task_alt,
                label = stringResource(id = R.string.refill_coolant),
                iconBgColor = Color(0xFFEEF2FF)
            )
        }
    }
}

@Composable
fun MenuItem(
    iconRes: Int,
    label: String,
    iconBgColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(160.dp)
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(20.dp),
            color = iconBgColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    modifier = Modifier.size(36.dp),
                    tint = Color.Black
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
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
