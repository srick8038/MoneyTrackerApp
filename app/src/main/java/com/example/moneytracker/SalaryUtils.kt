package com.example.moneytracker

import com.example.moneytracker.data.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SalaryCycle(
    val title: String,
    val totalBalance: Double,
    val items: List<Transaction>
)

fun groupTransactionsBySalaryAnchor(allTransactions: List<Transaction>): List<SalaryCycle> {
    if (allTransactions.isEmpty()) return emptyList()

    val chronologicalList = allTransactions.sortedBy { it.timestamp }
    val cycles = mutableListOf<SalaryCycle>()
    var currentCycleItems = mutableListOf<Transaction>()
    var currentCycleStartDate = ""
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)

    for (tx in chronologicalList) {
        if (currentCycleItems.isEmpty()) {
            currentCycleStartDate = dateFormatter.format(Date(tx.timestamp))
        }

        if (!tx.isExpense && tx.title.lowercase().contains("salary")) {
            if (currentCycleItems.isNotEmpty()) {
                val inc = currentCycleItems.filter { !it.isExpense }.sumOf { it.amount }
                val exp = currentCycleItems.filter { it.isExpense }.sumOf { it.amount }

                cycles.add(
                    SalaryCycle(
                        title = "Cycle starting $currentCycleStartDate",
                        totalBalance = inc - exp,
                        items = currentCycleItems.reversed()
                    )
                )
            }
            currentCycleItems = mutableListOf()
            currentCycleStartDate = dateFormatter.format(Date(tx.timestamp))
        }
        currentCycleItems.add(tx)
    }

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

    return cycles.reversed()
}