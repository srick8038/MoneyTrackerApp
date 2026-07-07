package com.example.moneytracker // Make sure this matches your actual package name!

import android.os.Bundle   //import packages
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.moneytracker.ui.theme.MoneyTrackerTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneytracker.data.Transaction
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import java.util.UUID // Used to generate unique IDs
import androidx.compose.foundation.layout.navigationBarsPadding

class MainActivity : ComponentActivity() { //first activity when app is laucned
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

@Composable  //an annotation that tells jetpack that the function is a user interface piece
fun MainScreen() {
    val transactions = remember {
        mutableStateListOf(
            Transaction("1", "Groceries", 45.50, isExpense = true),
            Transaction("2", "Salary", 1500.00, isExpense = false)
        )
    }

    // State variables to hold whatever text the user is typing right now
    var titleInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // App Title
        Text(
            text = "Money Tracker",
            fontSize = 24.sp,
            modifier = Modifier.padding(16.dp)
        )

        // The Scrolling List (takes up remaining space except the input area)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(transactions) { item ->
                TransactionItem(transaction = item)
            }
        }

        // --- INPUT FORM SECTION (At the bottom) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            // Title Input Field
            TextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                label = { Text("Transaction Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Amount Input Field
            TextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                label = { Text("Amount ($)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Row (Add Expense / Add Income)
            Row(modifier = Modifier.fillMaxWidth()) {

                // Add Expense Button (Red)
                Button(
                    onClick = {
                        val amount = amountInput.toDoubleOrNull() ?: 0.0
                        if (titleInput.isNotBlank() && amount > 0.0) {
                            transactions.add(
                                Transaction(UUID.randomUUID().toString(), titleInput, amount, isExpense = true)
                            )
                            // Clear inputs after adding
                            titleInput = ""
                            amountInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Expense")
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Add Income Button (Green)
                Button(
                    onClick = {
                        val amount = amountInput.toDoubleOrNull() ?: 0.0
                        if (titleInput.isNotBlank() && amount > 0.0) {
                            transactions.add(
                                Transaction(UUID.randomUUID().toString(), titleInput, amount, isExpense = false)
                            )
                            // Clear inputs after adding
                            titleInput = ""
                            amountInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Income")
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = transaction.title, fontSize = 18.sp)

            val prefix = if (transaction.isExpense) "-" else "+"
            val priceColor = if (transaction.isExpense) Color(0xFFD32F2F) else Color(0xFF388E3C)

            Text(
                text = "$prefix$${transaction.amount}",
                fontSize = 18.sp,
                color = priceColor
            )
        }
    }
}