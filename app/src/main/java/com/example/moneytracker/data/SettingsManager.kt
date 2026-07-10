package com.example.moneytracker.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create the DataStore instance as an extension property on Context
val Context.dataStore by preferencesDataStore(name = "money_tracker_prefs")

class SettingsManager(private val context: Context) {
    private val gson = Gson()

    companion object {
        // The unique key used to identify our transaction data string inside storage
        val TRANSACTIONS_KEY = stringPreferencesKey("transactions_list")
    }

    // Read transactions from storage as a Flow (reactive stream of data)
    val getTransactions: Flow<List<Transaction>> = context.dataStore.data
        .map { preferences ->
            val jsonString = preferences[TRANSACTIONS_KEY] ?: return@map emptyList()
            val type = object : TypeToken<List<Transaction>>() {}.type
            gson.fromJson(jsonString, type)
        }

    // Save transactions to storage
    suspend fun saveTransactions(transactions: List<Transaction>) {
        val jsonString = gson.toJson(transactions)
        context.dataStore.edit { preferences ->
            preferences[TRANSACTIONS_KEY] = jsonString
        }
    }
}