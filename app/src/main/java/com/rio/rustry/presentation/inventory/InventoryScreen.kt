package com.rio.rustry.presentation.inventory

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
import com.rio.rustry.data.model.InventoryItem
import com.rio.rustry.presentation.viewmodel.InventoryViewModel
import com.rio.rustry.utils.formatCurrency
import com.rio.rustry.utils.formatDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    farmId: String,
    onNavigateBack: () -> Unit,
    viewModel: InventoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val inventoryItems by viewModel.inventoryItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val totalValue by viewModel.totalValue.collectAsState()
    val lowStockItems by viewModel.lowStockItems.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }
    
    LaunchedEffect(farmId) {
        viewModel.loadInventoryItems(farmId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Item")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Inventory Item")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    InventorySummarySection(
                        totalValue = totalValue,
                        totalItems = inventoryItems.size,
                        lowStockCount = lowStockItems.size
                    )
                }
                
                if (lowStockItems.isNotEmpty()) {
                    item {
                        LowStockAlert(
                            lowStockItems = lowStockItems,
                            onViewItem = { /* Navigate to item details */ }
                        )
                    }
                }
                
                item {
                    CategoryFilterSection(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { category ->
                            selectedCategory = category
                            viewModel.filterByCategory(category)
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
                } else if (inventoryItems.isEmpty()) {
                    item {
                        EmptyInventoryState(
                            onAddItem = { showAddDialog = true }
                        )
                    }
                } else {
                    items(inventoryItems) { item ->
                        InventoryItemCard(
                            item = item,
                            onEdit = { viewModel.updateInventoryItem(it) },
                            onDelete = { viewModel.deleteInventoryItem(it.id) },
                            onUpdateStock = { itemId, newQuantity ->
                                viewModel.updateStock(itemId, newQuantity)
                            }
                        )
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddInventoryItemDialog(
            onDismiss = { showAddDialog = false },
            onSave = { item ->
                viewModel.addInventoryItem(farmId, item)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun InventorySummarySection(
    totalValue: Double,
    totalItems: Int,
    lowStockCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Inventory Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InventoryMetricCard(
                    title = "Total Value",
                    value = formatCurrency(totalValue),
                    icon = Icons.Default.AttachMoney,
                    color = MaterialTheme.colorScheme.primary
                )
                
                InventoryMetricCard(
                    title = "Total Items",
                    value = totalItems.toString(),
                    icon = Icons.Default.Inventory,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                InventoryMetricCard(
                    title = "Low Stock",
                    value = lowStockCount.toString(),
                    icon = Icons.Default.Warning,
                    color = if (lowStockCount > 0) Color.Red else Color.Green
                )
            }
        }
    }
}

@Composable
private fun InventoryMetricCard(
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

@Composable
private fun LowStockAlert(
    lowStockItems: List<InventoryItem>,
    onViewItem: (InventoryItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Low Stock Alert",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${lowStockItems.size} items are running low on stock",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            lowStockItems.take(3).forEach { item ->
                Text(
                    text = "• ${item.name} (${item.currentQuantity} ${item.unit} remaining)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            if (lowStockItems.size > 3) {
                Text(
                    text = "... and ${lowStockItems.size - 3} more",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterSection(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("All", "Feed", "Medicine", "Equipment", "Supplements", "Other")
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Filter by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                onCategorySelected(category)
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
private fun EmptyInventoryState(
    onAddItem: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Inventory,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Inventory Items",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Start managing your farm inventory by adding items",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddItem,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add First Item")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventoryItemCard(
    item: InventoryItem,
    onEdit: (InventoryItem) -> Unit,
    onDelete: (InventoryItem) -> Unit,
    onUpdateStock: (String, Double) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showStockDialog by remember { mutableStateOf(false) }
    
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
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (item.description.isNotEmpty()) {
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Update Stock") },
                            onClick = {
                                showStockDialog = true
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Update, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                onEdit(item)
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDelete(item)
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stock information
                Column {
                    Text(
                        text = "Stock",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val stockColor = when {
                            item.currentQuantity <= item.minimumQuantity -> Color.Red
                            item.currentQuantity <= item.minimumQuantity * 1.5 -> Color(0xFFFF9800)
                            else -> Color.Green
                        }
                        
                        Text(
                            text = "${item.currentQuantity} ${item.unit}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = stockColor
                        )
                        
                        if (item.currentQuantity <= item.minimumQuantity) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Low stock",
                                modifier = Modifier.size(16.dp),
                                tint = Color.Red
                            )
                        }
                    }
                }
                
                // Price information
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Unit Price",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(item.unitPrice),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Total value
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Total Value",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(item.currentQuantity * item.unitPrice),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (item.expiryDate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Expires: ${formatDate(item.expiryDate!!)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    if (showStockDialog) {
        UpdateStockDialog(
            item = item,
            onDismiss = { showStockDialog = false },
            onUpdate = { newQuantity ->
                onUpdateStock(item.id, newQuantity)
                showStockDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddInventoryItemDialog(
    onDismiss: () -> Unit,
    onSave: (InventoryItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Feed") }
    var description by remember { mutableStateOf("") }
    var currentQuantity by remember { mutableStateOf("") }
    var minimumQuantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }
    var unitPrice by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    
    val categories = listOf("Feed", "Medicine", "Equipment", "Supplements", "Other")
    val units = listOf("kg", "g", "L", "ml", "pieces", "bags", "bottles")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Inventory Item") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Category dropdown
                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = currentQuantity,
                        onValueChange = { currentQuantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Unit dropdown
                    var unitExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            units.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u) },
                                    onClick = {
                                        unit = u
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                OutlinedTextField(
                    value = minimumQuantity,
                    onValueChange = { minimumQuantity = it },
                    label = { Text("Minimum Stock Level") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = unitPrice,
                    onValueChange = { unitPrice = it },
                    label = { Text("Unit Price (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("Supplier (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotEmpty() && currentQuantity.isNotEmpty() && 
                        minimumQuantity.isNotEmpty() && unitPrice.isNotEmpty()) {
                        onSave(
                            InventoryItem(
                                id = UUID.randomUUID().toString(),
                                farmId = "",
                                name = name,
                                category = category,
                                description = description,
                                currentQuantity = currentQuantity.toDoubleOrNull() ?: 0.0,
                                minimumQuantity = minimumQuantity.toDoubleOrNull() ?: 0.0,
                                unit = unit,
                                unitPrice = unitPrice.toDoubleOrNull() ?: 0.0,
                                supplier = supplier,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdateStockDialog(
    item: InventoryItem,
    onDismiss: () -> Unit,
    onUpdate: (Double) -> Unit
) {
    var newQuantity by remember { mutableStateOf(item.currentQuantity.toString()) }
    var operation by remember { mutableStateOf("Set") } // Set, Add, Remove
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Stock - ${item.name}") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Current Stock: ${item.currentQuantity} ${item.unit}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Operation selection
                var operationExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = operationExpanded,
                    onExpandedChange = { operationExpanded = it }
                ) {
                    OutlinedTextField(
                        value = operation,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Operation") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = operationExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = operationExpanded,
                        onDismissRequest = { operationExpanded = false }
                    ) {
                        listOf("Set", "Add", "Remove").forEach { op ->
                            DropdownMenuItem(
                                text = { Text(op) },
                                onClick = {
                                    operation = op
                                    operationExpanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = newQuantity,
                    onValueChange = { newQuantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    suffix = { Text(item.unit) }
                )
                
                // Show preview of new stock level
                val quantity = newQuantity.toDoubleOrNull() ?: 0.0
                val finalQuantity = when (operation) {
                    "Set" -> quantity
                    "Add" -> item.currentQuantity + quantity
                    "Remove" -> item.currentQuantity - quantity
                    else -> item.currentQuantity
                }
                
                Text(
                    text = "New Stock Level: $finalQuantity ${item.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val quantity = newQuantity.toDoubleOrNull() ?: 0.0
                    val finalQuantity = when (operation) {
                        "Set" -> quantity
                        "Add" -> item.currentQuantity + quantity
                        "Remove" -> item.currentQuantity - quantity
                        else -> item.currentQuantity
                    }
                    onUpdate(finalQuantity)
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}