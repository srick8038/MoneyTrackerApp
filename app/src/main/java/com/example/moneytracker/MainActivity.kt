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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background

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

        // The Scrolling List
        // Calculate our live dynamic salary periods
        val salaryCycles = groupTransactionsBySalaryAnchor(transactions)

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "History by Pay Cycle", fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp))

        // --- NEW GROUPED SCROLLING LIST ---
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            salaryCycles.forEach { cycle ->
                // 1. STICKY HEADER FOR THE SALARY ANCHOR PERIOD
                @OptIn(ExperimentalFoundationApi::class)
                stickyHeader {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface) // Prevents list transparency overlap
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = cycle.title,
                            fontSize = 14.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val balanceColor = if (cycle.totalBalance >= 0) Color(0xFF388E3C) else Color(0xFFD32F2F)
                        Text(
                            text = "Net: $${String.format(Locale.US, "%.2f", cycle.totalBalance)}",
                            fontSize = 12.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            color = balanceColor
                        )
                    }
                }

                // 2. ITEMS BELONGING TO THIS SPECIFIC CYCLE
                items(cycle.items, key = { it.id }) { item ->
                    TransactionItem(
                        transaction = item,
                        onDeleteClick = {
                            transactions.remove(item)
                            coroutineScope.launch {
                                settingsManager.saveTransactions(transactions)
                            }
                        }
                    )
                }
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
                            transactions.add(0, newTx)

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
                            transactions.add(0, newTx)

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
fun TransactionItem(
    transaction: Transaction,
    onDeleteClick: () -> Unit // 1. Added a click callback here
) {
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically // Keeps everything lined up straight
        ) {
            // Left side: Title
            Text(text = transaction.title, fontSize = 18.sp, modifier = Modifier.weight(1f))

            // Middle: Price
            val prefix = if (transaction.isExpense) "-" else "+"
            val priceColor = if (transaction.isExpense) Color(0xFFD32F2F) else Color(0xFF388E3C)
            Text(
                text = "$prefix$${String.format(Locale.US, "%.2f", transaction.amount)}",
                fontSize = 18.sp,
                color = priceColor,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Right side: Delete Button
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Transaction",
                    tint = Color.Gray
                )
            }
        }
    }
}

data class SalaryCycle(
    val title: String,
    val totalBalance: Double,
    val items: List<Transaction>
)

fun groupTransactionsBySalaryAnchor(allTransactions: List<Transaction>): List<SalaryCycle> {
    if (allTransactions.isEmpty()) return emptyList()

    // 1. Sort transactions chronologically (oldest to newest) to process history step-by-step
    val chronologicalList = allTransactions.sortedBy { it.timestamp }

    val cycles = mutableListOf<SalaryCycle>()
    var currentCycleItems = mutableListOf<Transaction>()
    var currentCycleStartDate = ""

    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)

    for (tx in chronologicalList) {
        // If this is the very first item in a cycle, capture its date as the cycle's starting point
        if (currentCycleItems.isEmpty()) {
            currentCycleStartDate = dateFormatter.format(Date(tx.timestamp))
        }

        // 2. ANCHOR CHECK: If we hit an income with "salary" in the title, close the existing cycle!
        if (!tx.isExpense && tx.title.lowercase().contains("salary")) {
            if (currentCycleItems.isNotEmpty()) {
                // Calculate the balance of the cycle that is ending
                val inc = currentCycleItems.filter { !it.isExpense }.sumOf { it.amount }
                val exp = currentCycleItems.filter { it.isExpense }.sumOf { it.amount }

                cycles.add(
                    SalaryCycle(
                        title = "Cycle starting $currentCycleStartDate",
                        totalBalance = inc - exp,
                        items = currentCycleItems.reversed() // Reverse back so newest shows on top of UI
                    )
                )
            }
            // Reset for the brand-new salary period
            currentCycleItems = mutableListOf()
            currentCycleStartDate = dateFormatter.format(Date(tx.timestamp))
        }

        currentCycleItems.add(tx)
    }

    // 3. Close out the final ongoing (current) cycle
    if (currentCycleItems.isNotEmpty()) {
        val inc = currentCycleItems.filter { !it.isExpense }.sumOf { it.amount }
        val exp = currentCycleItems.filter { it.isExpense }.sumOf { it.amount }
        cycles.add(
            SalaryCycle(
                title = "Current Cycle (from $currentCycleStartDate)",
                totalBalance = inc - exp,
                items = currentCycleItems.reversed()
            )
        )
    }

    // Return the list of cycles with the newest cycle at the top of the screen
    return cycles.reversed()
}