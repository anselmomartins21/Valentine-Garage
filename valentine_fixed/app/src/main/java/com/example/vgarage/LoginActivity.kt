package com.example.vgarage

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen { user ->
                CurrentUser.id   = user.id
                CurrentUser.name = user.name
                CurrentUser.role = user.role
                CurrentUser.recordActivity() 
                val destinationPage = if (user.role == "admin") ReportsActivity::class.java
                else DashboardActivity::class.java
                startActivity(Intent(this@LoginActivity, destinationPage))        }
    }
}

@Composable
fun LoginScreen(onLogin: (User) -> Unit) {
    val scope   = rememberCoroutineScope()
    var email   by remember { mutableStateOf("") }
    var pin     by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error   by remember { mutableStateOf("") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor        = Color.White,
        unfocusedTextColor      = Color.White,
        focusedContainerColor   = Color(0xFF1E293B),
        unfocusedContainerColor = Color(0xFF1E293B),
        focusedBorderColor      = Color(0xFFF97318),
        unfocusedBorderColor    = Color(0xFF334155),
        focusedLabelColor       = Color(0xFFF97318),
        unfocusedLabelColor     = Color(0xFF64748B)
    )

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Text("🔧", fontSize = 40.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text("VALENTINE'S",
                color = Color(0xFFF97318), fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("GARAGE",
                color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Box(Modifier.width(40.dp).height(3.dp).background(Color(0xFFF97318)))
            Spacer(Modifier.height(8.dp))
            Text("Vehicle Management System",
                color = Color(0xFF64748B), fontSize = 13.sp)
            Spacer(Modifier.height(40.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email address") },
                placeholder = { Text("e.g. john@vgarage.com",
                    color = Color(0xFF475569)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )
            Spacer(Modifier.height(12.dp))

            // PIN
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it },
                label = { Text("PIN") },
                placeholder = { Text("Enter your PIN",
                    color = Color(0xFF475569)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            if (error.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(error,
                    color = Color(0xFFEF4444),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    when {
                        email.isEmpty() ->
                        { error = "Please enter your email"; return@Button }
                        pin.isEmpty() ->
                        { error = "Please enter your PIN";   return@Button }
                    }
                    error = ""; loading = true
                    scope.launch {
                        val user = GarageRepository.login(email, pin)
                        loading = false
                        if (user == null)
                            error = "Wrong email or PIN. Try again."
                        else onLogin(user)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF97318)),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp)
                } else {
                    Text("LOGIN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun LoginPreview() {
    LoginScreen { }
}
}