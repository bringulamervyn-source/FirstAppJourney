package com.example.expensestracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensestracker.data.CycleEntity
import com.example.expensestracker.data.TransactionEntity
import com.example.expensestracker.data.TransactionType
import com.example.expensestracker.domain.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ExpenseUiState(
    val activeCycle: CycleEntity? = null,
    val transactions: List<TransactionEntity> = emptyList(),
    val bankBalance: Double = 0.0,
    val cashOnHand: Double = 0.0,
    val totalNetWorth: Double = 0.0,
)

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    val uiState: StateFlow<ExpenseUiState> = combine(
        repository.activeCycle,
        repository.transactions,
        repository.bankBalance,
        repository.cashOnHand,
        repository.totalNetWorth,
    ) { activeCycle, transactions, bankBalance, cashOnHand, totalNetWorth ->
        ExpenseUiState(
            activeCycle = activeCycle,
            transactions = transactions,
            bankBalance = bankBalance,
            cashOnHand = cashOnHand,
            totalNetWorth = totalNetWorth
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExpenseUiState()
    )

    fun addTransaction(amount: Double, type: TransactionType, description: String) {
        viewModelScope.launch {
            repository.addTransaction(amount, type, description)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun startNewCycle(salary: Double) {
        viewModelScope.launch {
            repository.startNewCycle(salary)
        }
    }
}

class ExpenseViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
