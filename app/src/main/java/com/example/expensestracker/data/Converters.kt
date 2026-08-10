package com.example.expensestracker.data

import androidx.room.TypeConverter

@Suppress("unused")
class Converters {
    @TypeConverter
    @Suppress("unused")
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    @Suppress("unused")
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }
}
