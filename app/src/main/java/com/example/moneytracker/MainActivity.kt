package com.example.moneytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import com.example.moneytracker.data.SettingsManager
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
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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
    val transactions = remember { mutableStateListOf<Transaction>() }

    LaunchedEffect(Unit) {
        settingsManager.getTransactions.collect { savedList ->
            transactions.clear()
            transactions.addAll(savedList)
        }
    }

    val totalIncome = transactions.filter { !it.isExpense }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.isExpense }.sumOf { it.amount }
    val totalBalance = totalIncome - totalExpense
    val salaryCycles = groupTransactionsBySalaryAnchor(transactions) // Reads from SalaryUtils.kt automatically!

    var titleInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Text(text = "Money Tracker", fontSize = 24.sp, modifier = Modifier.padding(16.dp))

        // Dashboard Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Total Balance", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(text = "$${String.format(Locale.US, "%.2f", totalBalance)}", fontSize = 32.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
        Text(text = "History by Pay Cycle", fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp))

        // Scrolling List
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            salaryCycles.forEach { cycle ->
                @OptIn(ExperimentalFoundationApi::class)
                stickyHeader {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(text = cycle.title, fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        val balanceColor = if (cycle.totalBalance >= 0) Color(0xFF388E3C) else Color(0xFFD32F2F)
                        Text(text = "Net: $${String.format(Locale.US, "%.2f", cycle.totalBalance)}", fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, color = balanceColor)
                    }
                }

                items(cycle.items, key = { it.id }) { item ->
                    TransactionItem(transaction = item, onDeleteClick = { // Reads from TransactionItem.kt automatically!
                        transactions.remove(item)
                        coroutineScope.launch { settingsManager.saveTransactions(transactions) }
                    })
                }
            }
        }

        // Input Form
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding().imePadding()) {
            TextField(value = titleInput, onValueChange = { titleInput = it }, label = { Text("Transaction Title") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = amountInput, onValueChange = { amountInput = it }, label = { Text("Amount ($)") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val amount = amountInput.toDoubleOrNull() ?: 0.0
                        if (titleInput.isNotBlank() && amount > 0.0) {
                            transactions.add(0, Transaction(UUID.randomUUID().toString(), titleInput, amount, isExpense = true))
                            coroutineScope.launch { settingsManager.saveTransactions(transactions) }
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
                            transactions.add(0, Transaction(UUID.randomUUID().toString(), titleInput, amount, isExpense = false))
                            coroutineScope.launch { settingsManager.saveTransactions(transactions) }
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