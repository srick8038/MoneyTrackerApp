package com.example.moneytracker

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneytracker.data.Transaction
import java.util.Locale

@Composable
fun TransactionItem(
    transaction: Transaction,
    onDeleteClick: () -> Unit
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
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(text = transaction.title, fontSize = 18.sp, modifier = Modifier.weight(1f))

            val prefix = if (transaction.isExpense) "-" else "+"
            val priceColor = if (transaction.isExpense) Color(0xFFD32F2F) else Color(0xFF388E3C)
            Text(
                text = "$prefix$${String.format(Locale.US, "%.2f", transaction.amount)}",
                fontSize = 18.sp,
                color = priceColor,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

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