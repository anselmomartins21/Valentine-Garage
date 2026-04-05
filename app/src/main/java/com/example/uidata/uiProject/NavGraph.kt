package com.example.uidata.uiProject

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.uidata.ui.theme.UidataTheme

sealed class Screen {
    data object Start : Screen()
    data object Login : Screen()
    data object TaskList : Screen()
}

@Composable
fun NavGraph() {
    // Start with the Start screen as per the new design
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Start) }

    when (currentScreen) {
        is Screen.Start -> {
            StartScreen(
                onLoginClick = {
                    currentScreen = Screen.Login
                }
            )
        }
        is Screen.Login -> {
            LoginScreen(
                onLoginSuccess = {
                    currentScreen = Screen.TaskList
                },
                onBack = {
                    currentScreen = Screen.Start
                }
            )
        }
        is Screen.TaskList -> {
            TaskListScreen(
                onBack = {
                    currentScreen = Screen.Login
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NavGraphPreview() {
    UidataTheme {
        NavGraph()
    }
}
