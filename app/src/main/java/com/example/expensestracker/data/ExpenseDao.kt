package com.example.expensestracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM cycles WHERE isActive = 1 LIMIT 1")
    fun getActiveCycle(): Flow<CycleEntity?>

    @Query("SELECT * FROM transactions WHERE cycleId = :cycleId ORDER BY timestamp DESC")
    fun getTransactionsForCycle(cycleId: Long): Flow<List<TransactionEntity>>

    @Insert
    suspend fun insertCycle(cycle: CycleEntity): Long

    @Update
    suspend fun updateCycle(cycle: CycleEntity)

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT SUM(amount) FROM transactions WHERE cycleId = :cycleId AND type = 'CARD_PAYMENT'")
    fun getSumCardPayments(cycleId: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE cycleId = :cycleId AND type = 'ATM_WITHDRAWAL'")
    fun getSumAtmWithdrawals(cycleId: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE cycleId = :cycleId AND type = 'CASH_EXPENSE'")
    fun getSumCashExpenses(cycleId: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE cycleId = :cycleId AND type = 'CARD_PAYMENT'")
    suspend fun getSumCardPaymentsSync(cycleId: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE cycleId = :cycleId AND type = 'ATM_WITHDRAWAL'")
    suspend fun getSumAtmWithdrawalsSync(cycleId: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE cycleId = :cycleId AND type = 'CASH_EXPENSE'")
    suspend fun getSumCashExpensesSync(cycleId: Long): Double?

    @Transaction
    @Query("SELECT * FROM cycles WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveCycleSync(): CycleEntity?
}
