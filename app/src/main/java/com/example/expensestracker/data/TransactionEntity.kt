package com.example.expensestracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cycleId: Long,
    val amount: Double,
    val type: TransactionType,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
)
