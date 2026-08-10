package com.example.expensestracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val editingTransaction = remember { mutableStateOf<TransactionEntity?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Expenses Tracker") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                editingTransaction.value = null
                showAddTransactionDialog.value = true 
            }) {
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

            TransactionList(
                transactions = uiState.transactions,
                onEdit = { 
                    editingTransaction.value = it
                    showAddTransactionDialog.value = true
                },
                onDelete = { viewModel.deleteTransaction(it) }
            )
        }

        if (showAddTransactionDialog.value) {
            AddOrEditTransactionDialog(
                transaction = editingTransaction.value,
                onDismiss = { showAddTransactionDialog.value = false },
            ) { amount, type, desc ->
                if (editingTransaction.value == null) {
                    viewModel.addTransaction(amount, type, desc)
                } else {
                    viewModel.updateTransaction(editingTransaction.value!!.copy(
                        amount = amount,
                        type = type,
                        description = desc
                    ))
                }
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
fun BalanceCard(title: String, amount: String, color: Color) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionList(
    transactions: List<TransactionEntity>,
    onEdit: (TransactionEntity) -> Unit,
    onDelete: (TransactionEntity) -> Unit,
) {
    val kuwaitLocale = Locale.Builder().setLanguage("en").setRegion("KW").build()
    val currencyFormatter = NumberFormat.getCurrencyInstance(kuwaitLocale)
    
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(
            items = transactions,
            key = { it.id }
        ) { tx ->
            val dismissState = rememberSwipeToDismissBoxState()

            LaunchedEffect(dismissState.currentValue) {
                if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                    onDelete(tx)
                }
            }

            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {
                    val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                        Color.Red
                    } else Color.Transparent
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                    }
                },
                enableDismissFromStartToEnd = false
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onEdit(tx) }
                ) {
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
}

@Composable
fun AddOrEditTransactionDialog(
    transaction: TransactionEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (Double, TransactionType, String) -> Unit,
) {
    var amount by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
    var description by remember { mutableStateOf(transaction?.description ?: "") }
    var type by remember { mutableStateOf(transaction?.type ?: TransactionType.CARD_PAYMENT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (transaction == null) "Add Transaction" else "Edit Transaction") },
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
                Text(if (transaction == null) "Add" else "Save")
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
