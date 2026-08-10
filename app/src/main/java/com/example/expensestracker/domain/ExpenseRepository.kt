package com.example.expensestracker.domain

import com.example.expensestracker.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseRepository(private val expenseDao: ExpenseDao) {

    val activeCycle: Flow<CycleEntity?> = expenseDao.getActiveCycle()

    val transactions: Flow<List<TransactionEntity>> = activeCycle.flatMapLatest { cycle ->
        cycle?.let { expenseDao.getTransactionsForCycle(it.id) } ?: flowOf(emptyList())
    }

    val bankBalance: Flow<Double> = activeCycle.flatMapLatest { cycle ->
        if (cycle == null) flowOf(0.0)
        else {
            combine(
                expenseDao.getSumCardPayments(cycle.id),
                expenseDao.getSumAtmWithdrawals(cycle.id),
            ) { card, atm ->
                cycle.initialBankBalance - (card ?: 0.0) - (atm ?: 0.0)
            }
        }
    }

    val cashOnHand: Flow<Double> = activeCycle.flatMapLatest { cycle ->
        if (cycle == null) flowOf(0.0)
        else {
            combine(
                expenseDao.getSumAtmWithdrawals(cycle.id),
                expenseDao.getSumCashExpenses(cycle.id),
            ) { atm, cash ->
                (atm ?: 0.0) - (cash ?: 0.0)
            }
        }
    }

    val totalNetWorth: Flow<Double> = combine(bankBalance, cashOnHand) { bank, cash ->
        bank + cash
    }

    suspend fun addTransaction(amount: Double, type: TransactionType, description: String) {
        val activeCycle = expenseDao.getActiveCycleSync() ?: return
        val transaction = TransactionEntity(
            cycleId = activeCycle.id,
            amount = amount,
            type = type,
            description = description,
        )
        expenseDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        expenseDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        expenseDao.deleteTransaction(transaction)
    }

    suspend fun startNewCycle(salary: Double) {
        val currentCycle = expenseDao.getActiveCycleSync()
        var forwardedBalance = 0.0

        if (currentCycle != null) {
            val card = expenseDao.getSumCardPaymentsSync(currentCycle.id) ?: 0.0
            val atm = expenseDao.getSumAtmWithdrawalsSync(currentCycle.id) ?: 0.0
            val cash = expenseDao.getSumCashExpensesSync(currentCycle.id) ?: 0.0

            val bank = currentCycle.initialBankBalance - card - atm
            val hand = atm - cash
            forwardedBalance = bank + hand

            expenseDao.updateCycle(
                currentCycle.copy(
                    isActive = false,
                    endDate = System.currentTimeMillis(),
                ),
            )
        }

        val newCycle = CycleEntity(
            forwardedBalance = forwardedBalance,
            creditedSalary = salary,
            isActive = true,
        )
        expenseDao.insertCycle(newCycle)
    }
}
