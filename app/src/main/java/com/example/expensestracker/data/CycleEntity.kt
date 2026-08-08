package com.example.expensestracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cycles")
data class CycleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val forwardedBalance: Double = 0.0,
    val creditedSalary: Double = 0.0,
    val isActive: Boolean = true,
) {
    val initialBankBalance: Double
        get() = forwardedBalance + creditedSalary
}
