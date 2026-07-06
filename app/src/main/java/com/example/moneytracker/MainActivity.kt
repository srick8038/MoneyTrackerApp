package com.example.moneytracker // Make sure this matches your actual package name!

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.moneytracker.ui.theme.MoneyTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoneyTrackerTheme {
                // Surface is just a background container using your app's theme colors
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Your app starts here!
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    // This is a temporary placeholder text
    Text(text = "Welcome to your Money Tracker app!")
}