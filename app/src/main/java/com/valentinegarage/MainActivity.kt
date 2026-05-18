package com.valentinegarage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.valentinegarage.ui.checkin.CheckInScreen

// MainActivity is the ENTRY POINT of the app
// When you open the app, this is the first thing that runs
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContent tells Android what to show on screen
        // We show our CheckInScreen here
        setContent {
            MaterialTheme {
                Surface {
                    CheckInScreen()
                }
            }
        }
    }
}