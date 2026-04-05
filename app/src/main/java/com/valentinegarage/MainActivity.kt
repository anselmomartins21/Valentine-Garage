package com.valentinegarage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import dagger.hilt.android.AndroidEntryPoint
import com.valentinegarage.ui.checkin.CheckInScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    CheckInScreen(
                        onCheckInSuccess = {
                            // Later this will navigate to the Dashboard
                        }
                    )
                }
            }
        }
    }
}