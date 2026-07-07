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
import androidx.compose.foundation.layout.imePadding
import java.util.Locale

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

@Composable
fun MainScreen() {
    val transactions = remember {
        mutableStateListOf(
            Transaction("1", "Groceries", 45.50, isExpense = true),
            Transaction("2", "Salary", 1500.00, isExpense = false)
        )
    }

    // 1. DYNAMIC CALCULATIONS
    // Calculate total income, total expenses, and the remaining balance
    val totalIncome = transactions.filter { !it.isExpense }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.isExpense }.sumOf { it.amount }
    val totalBalance = totalIncome - totalExpense

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
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
        )

        // 2. THE DASHBOARD CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Total Balance", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(
                    text = "$${String.format(Locale.US,"%.2f", totalBalance)}",
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Income", fontSize = 12.sp, color = Color(0xFF388E3C))
                        Text(text = "+$${String.format(Locale.US,"%.2f", totalIncome)}", fontSize = 16.sp, color = Color(0xFF388E3C))
                    }
                    Column {
                        Text(text = "Expenses", fontSize = 12.sp, color = Color(0xFFD32F2F))
                        Text(text = "-$${String.format(Locale.US,"%.2f", totalExpense)}", fontSize = 16.sp, color = Color(0xFFD32F2F))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // The Scrolling List
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

        // --- INPUT FORM SECTION ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            TextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                label = { Text("Transaction Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                label = { Text("Amount ($)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val amount = amountInput.toDoubleOrNull() ?: 0.0
                        if (titleInput.isNotBlank() && amount > 0.0) {
                            transactions.add(Transaction(UUID.randomUUID().toString(), titleInput, amount, isExpense = true))
                            titleInput = ""
                            amountInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    modifier = Modifier.weight(1f)
                ) { Text("Expense") }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        val amount = amountInput.toDoubleOrNull() ?: 0.0
                        if (titleInput.isNotBlank() && amount > 0.0) {
                            transactions.add(Transaction(UUID.randomUUID().toString(), titleInput, amount, isExpense = false))
                            titleInput = ""
                            amountInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                    modifier = Modifier.weight(1f)
                ) { Text("Income") }
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