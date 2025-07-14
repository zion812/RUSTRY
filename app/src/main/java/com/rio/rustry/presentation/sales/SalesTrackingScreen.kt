package com.rio.rustry.presentation.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rio.rustry.data.model.SaleRecord
import com.rio.rustry.presentation.viewmodel.SalesViewModel
import com.rio.rustry.utils.formatCurrency
import com.rio.rustry.utils.formatDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesTrackingScreen(
    farmId: String,
    onNavigateBack: () -> Unit,
    viewModel: SalesViewModel = viewModel()
) {
    val context = LocalContext.current
    val salesRecords by viewModel.salesRecords.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val monthlyRevenue by viewModel.monthlyRevenue.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf("All Time") }
    
    LaunchedEffect(farmId) {
        viewModel.loadSalesRecords(farmId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales Tracking") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Sale")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Sale Record")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Revenue Summary Cards
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    RevenueSummarySection(
                        totalRevenue = totalRevenue,
                        monthlyRevenue = monthlyRevenue,
                        salesCount = salesRecords.size
                    )
                }
                
                item {
                    // Period Filter
                    PeriodFilterSection(
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = { period ->
                            selectedPeriod = period
                            viewModel.filterByPeriod(period)
                        }
                    )
                }
                
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (error != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text(
                                text = error!!,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                } else if (salesRecords.isEmpty()) {
                    item {
                        EmptySalesState(
                            onAddSale = { showAddDialog = true }
                        )
                    }
                } else {
                    items(salesRecords) { sale ->
                        SaleRecordCard(
                            sale = sale,
                            onEdit = { viewModel.updateSaleRecord(it) },
                            onDelete = { viewModel.deleteSaleRecord(it.id) }
                        )
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddSaleRecordDialog(
            onDismiss = { showAddDialog = false },
            onSave = { sale ->
                viewModel.addSaleRecord(farmId, sale)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun RevenueSummarySection(
    totalRevenue: Double,
    monthlyRevenue: Double,
    salesCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Revenue Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RevenueMetricCard(
                    title = "Total Revenue",
                    value = formatCurrency(totalRevenue),
                    icon = Icons.Default.AttachMoney,
                    color = MaterialTheme.colorScheme.primary
                )
                
                RevenueMetricCard(
                    title = "This Month",
                    value = formatCurrency(monthlyRevenue),
                    icon = Icons.Default.TrendingUp,
                    color = Color.Green
                )
                
                RevenueMetricCard(
                    title = "Total Sales",
                    value = salesCount.toString(),
                    icon = Icons.Default.Receipt,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun RevenueMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = color
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodFilterSection(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit
) {
    val periods = listOf("All Time", "This Month", "Last 3 Months", "This Year")
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Filter by Period",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedPeriod,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Period") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    periods.forEach { period ->
                        DropdownMenuItem(
                            text = { Text(period) },
                            onClick = {
                                onPeriodSelected(period)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySalesState(
    onAddSale: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Receipt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Sales Records",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Start tracking your sales to monitor revenue and growth",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddSale,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Record First Sale")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaleRecordCard(
    sale: SaleRecord,
    onEdit: (SaleRecord) -> Unit,
    onDelete: (SaleRecord) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sale.fowlName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Sold to: ${sale.buyerName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = formatDate(sale.saleDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formatCurrency(sale.salePrice),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    onEdit(sale)
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    onDelete(sale)
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                            )
                        }
                    }
                }
            }
            
            if (sale.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = sale.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Payment status indicator
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statusColor = when (sale.paymentStatus.lowercase()) {
                    "paid" -> Color.Green
                    "pending" -> Color(0xFFFF9800)
                    "overdue" -> Color.Red
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                
                Icon(
                    imageVector = when (sale.paymentStatus.lowercase()) {
                        "paid" -> Icons.Default.CheckCircle
                        "pending" -> Icons.Default.Schedule
                        "overdue" -> Icons.Default.Warning
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = statusColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = sale.paymentStatus,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSaleRecordDialog(
    onDismiss: () -> Unit,
    onSave: (SaleRecord) -> Unit
) {
    var fowlName by remember { mutableStateOf("") }
    var buyerName by remember { mutableStateOf("") }
    var buyerContact by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var paymentStatus by remember { mutableStateOf("Paid") }
    var notes by remember { mutableStateOf("") }
    
    val paymentMethods = listOf("Cash", "Bank Transfer", "UPI", "Cheque", "Other")
    val paymentStatuses = listOf("Paid", "Pending", "Partial", "Overdue")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Sale") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = fowlName,
                    onValueChange = { fowlName = it },
                    label = { Text("Fowl/Product Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    label = { Text("Buyer Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = buyerContact,
                    onValueChange = { buyerContact = it },
                    label = { Text("Buyer Contact") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = salePrice,
                    onValueChange = { salePrice = it },
                    label = { Text("Sale Price (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Payment Method dropdown
                var paymentMethodExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = paymentMethodExpanded,
                    onExpandedChange = { paymentMethodExpanded = it }
                ) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Payment Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentMethodExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = paymentMethodExpanded,
                        onDismissRequest = { paymentMethodExpanded = false }
                    ) {
                        paymentMethods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    paymentMethod = method
                                    paymentMethodExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Payment Status dropdown
                var paymentStatusExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = paymentStatusExpanded,
                    onExpandedChange = { paymentStatusExpanded = it }
                ) {
                    OutlinedTextField(
                        value = paymentStatus,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Payment Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentStatusExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = paymentStatusExpanded,
                        onDismissRequest = { paymentStatusExpanded = false }
                    ) {
                        paymentStatuses.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    paymentStatus = status
                                    paymentStatusExpanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (fowlName.isNotEmpty() && buyerName.isNotEmpty() && salePrice.isNotEmpty()) {
                        onSave(
                            SaleRecord(
                                id = UUID.randomUUID().toString(),
                                farmId = "",
                                fowlId = "",
                                fowlName = fowlName,
                                buyerName = buyerName,
                                buyerContact = buyerContact,
                                salePrice = salePrice.toDoubleOrNull() ?: 0.0,
                                saleDate = Date(),
                                paymentMethod = paymentMethod,
                                paymentStatus = paymentStatus,
                                notes = notes,
                                createdAt = Date(),
                                updatedAt = Date()
                            )
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}