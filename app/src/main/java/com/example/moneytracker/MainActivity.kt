package com.example.moneytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneytracker.data.Transaction
import com.example.moneytracker.ui.theme.MoneyTrackerTheme
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoneyTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // Start with an empty, reactive list
    val transactions = remember { mutableStateListOf<Transaction>() }

    // LOAD DATA ON LAUNCH
    // This blocks runs once when the screen opens to read your data stream from storage
    LaunchedEffect(Unit) {
        settingsManager.getTransactions.collect { savedList ->
            transactions.clear()
            transactions.addAll(savedList)
        }
    }

    // Dynamic calculations using explicit Locale formatting
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
        Text(
            text = "Money Tracker",
            fontSize = 24.sp,
            modifier = Modifier.padding(16.dp)
        )

        // Dashboard Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Total Balance", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(
                    text = "$${String.format(Locale.US, "%.2f", totalBalance)}",
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Income", fontSize = 12.sp, color = Color(0xFF388E3C))
                        Text(text = "+$${String.format(Locale.US, "%.2f", totalIncome)}", fontSize = 16.sp, color = Color(0xFF388E3C))
                    }
                    Column {
                        Text(text = "Expenses", fontSize = 12.sp, color = Color(0xFFD32F2F))
                        Text(text = "-$${String.format(Locale.US, "%.2f", totalExpense)}", fontSize = 16.sp, color = Color(0xFFD32F2F))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Transaction List
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
                .imePadding() // Automatically adjusts above the onscreen keyboard
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
                // Expense Button
                Button(
                    onClick = {
                        val amount = amountInput.toDoubleOrNull() ?: 0.0
                        if (titleInput.isNotBlank() && amount > 0.0) {
                            val newTx = Transaction(UUID.randomUUID().toString(), titleInput, amount, isExpense = true)
                            transactions.add(newTx)

                            // Save asynchronously to local device memory
                            coroutineScope.launch {
                                settingsManager.saveTransactions(transactions)
                            }

                            titleInput = ""
                            amountInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    modifier = Modifier.weight(1f)
                ) { Text("Expense") }

                Spacer(modifier = Modifier.width(8.dp))

                // Income Button
                Button(
                    onClick = {
                        val amount = amountInput.toDoubleOrNull() ?: 0.0
                        if (titleInput.isNotBlank() && amount > 0.0) {
                            val newTx = Transaction(UUID.randomUUID().toString(), titleInput, amount, isExpense = false)
                            transactions.add(newTx)

                            // Save asynchronously to local device memory
                            coroutineScope.launch {
                                settingsManager.saveTransactions(transactions)
                            }

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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
            Text(text = "$prefix$${String.format(Locale.US, "%.2f", transaction.amount)}", fontSize = 18.sp, color = priceColor)
        }
    }
}