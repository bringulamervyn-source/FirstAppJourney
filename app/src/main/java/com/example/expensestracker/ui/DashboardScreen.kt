package com.example.expensestracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensestracker.data.TransactionEntity
import com.example.expensestracker.data.TransactionType
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: ExpenseViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val showAddTransactionDialog = remember { mutableStateOf(value = false) }
    val showNewCycleDialog = remember { mutableStateOf(value = false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Expenses Tracker") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTransactionDialog.value = true }) {
                Text("+")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SummaryCards(uiState)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { showNewCycleDialog.value = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start New Cycle")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            TransactionList(uiState.transactions)
        }

        if (showAddTransactionDialog.value) {
            AddTransactionDialog(
                onDismiss = { showAddTransactionDialog.value = false },
            ) { amount, type, desc ->
                viewModel.addTransaction(amount, type, desc)
                showAddTransactionDialog.value = false
            }
        }

        if (showNewCycleDialog.value) {
            NewCycleDialog(
                onDismiss = { showNewCycleDialog.value = false },
            ) { salary ->
                viewModel.startNewCycle(salary)
                showNewCycleDialog.value = false
            }
        }
    }
}

@Composable
fun SummaryCards(uiState: ExpenseUiState) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("KW").build())

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BalanceCard("Cash on Bank", currencyFormatter.format(uiState.bankBalance), MaterialTheme.colorScheme.primaryContainer)
        BalanceCard("Cash on Hand", currencyFormatter.format(uiState.cashOnHand), MaterialTheme.colorScheme.secondaryContainer)
        BalanceCard("Total Net Worth", currencyFormatter.format(uiState.totalNetWorth), MaterialTheme.colorScheme.tertiaryContainer)
    }
}

@Composable
fun BalanceCard(title: String, amount: String, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(amount, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TransactionList(transactions: List<TransactionEntity>) {
    val kuwaitLocale = Locale.Builder().setLanguage("en").setRegion("KW").build()
    val currencyFormatter = NumberFormat.getCurrencyInstance(kuwaitLocale)
    
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(transactions) { tx ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(tx.description, style = MaterialTheme.typography.bodyLarge)
                        Text(tx.type.name, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        text = currencyFormatter.format(tx.amount),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double, TransactionType, String) -> Unit,
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.CARD_PAYMENT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Transaction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                
                Text("Type:")
                TransactionType.entries.forEach { t ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = type == t, onClick = { type = t })
                        Text(t.name)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(amount.toDoubleOrNull() ?: 0.0, type, description) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun NewCycleDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
) {
    var salary by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start New Cycle") },
        text = {
            Column {
                Text("Enter Credited Salary for this cycle:")
                OutlinedTextField(value = salary, onValueChange = { salary = it }, label = { Text("Salary") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(salary.toDoubleOrNull() ?: 0.0) }) {
                Text("Start")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
