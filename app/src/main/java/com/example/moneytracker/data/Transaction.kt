package com.example.moneytracker.data // 1. Must match its folder location

/**
 * Represents a single financial transaction.
 */
data class Transaction(
    val id: String,          // Unique identifier (useful for lists and databases)
    val title: String,       // What the transaction was for (e.g., "Groceries", "Paycheck")
    val amount: Double,      // The monetary value (e.g., 45.50)
    val isExpense: Boolean   // true if money went out, false if money came in
)