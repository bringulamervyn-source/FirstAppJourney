package com.example.expensestracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.expensestracker.data.AppDatabase
import com.example.expensestracker.domain.ExpenseRepository
import com.example.expensestracker.ui.DashboardScreen
import com.example.expensestracker.ui.ExpenseViewModel
import com.example.expensestracker.ui.ExpenseViewModelFactory
import com.example.expensestracker.ui.theme.ExpensesTrackerTheme

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { ExpenseRepository(database.expenseDao()) }
    private val viewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpensesTrackerTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }
    }
}
