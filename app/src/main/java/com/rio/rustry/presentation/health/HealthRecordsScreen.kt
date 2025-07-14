package com.rio.rustry.presentation.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.rio.rustry.data.model.HealthEventType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rio.rustry.R
import com.rio.rustry.data.model.HealthRecord
import com.rio.rustry.presentation.viewmodel.HealthViewModel
import com.rio.rustry.utils.formatDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthRecordsScreen(
    fowlId: String,
    onNavigateBack: () -> Unit,
    viewModel: HealthViewModel = viewModel()
) {
    val context = LocalContext.current
    val healthRecords by viewModel.healthRecords.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(fowlId) {
        viewModel.loadHealthRecords(fowlId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Records") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Record")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Health Record")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = error!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            } else if (healthRecords.isEmpty()) {
                EmptyHealthRecordsState(
                    onAddRecord = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(healthRecords) { record ->
                        HealthRecordCard(
                            record = record,
                            onEdit = { viewModel.updateHealthRecord(it) },
                            onDelete = { viewModel.deleteHealthRecord(it.id) }
                        )
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddHealthRecordDialog(
            onDismiss = { showAddDialog = false },
            onSave = { record ->
                viewModel.addHealthRecord(fowlId, record)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun EmptyHealthRecordsState(
    onAddRecord: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.HealthAndSafety,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Health Records",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Add your first health record to track your fowl's health",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddRecord,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Health Record")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HealthRecordCard(
    record: HealthRecord,
    onEdit: (HealthRecord) -> Unit,
    onDelete: (HealthRecord) -> Unit
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
                        text = record.type.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = formatDate(record.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                            text = { Text("Edit") },
                            onClick = {
                                onEdit(record)
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDelete(record)
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                        )
                    }
                }
            }
            
            if (record.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = record.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (record.veterinarian.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = record.veterinarian,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Health status indicator
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statusColor = when (record.status.lowercase()) {
                    "healthy" -> Color.Green
                    "sick" -> Color.Red
                    "recovering" -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(statusColor, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = record.status,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHealthRecordDialog(
    onDismiss: () -> Unit,
    onSave: (HealthRecord) -> Unit
) {
    var type by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var veterinarian by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Healthy") }
    var date by remember { mutableStateOf(Date()) }
    
    val statusOptions = listOf("Healthy", "Sick", "Recovering", "Under Treatment")
    val typeOptions = listOf("Vaccination", "Treatment", "Checkup", "Surgery", "Medication", "Other")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Health Record") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Type dropdown
                var typeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        typeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    type = option
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                OutlinedTextField(
                    value = veterinarian,
                    onValueChange = { veterinarian = it },
                    label = { Text("Veterinarian (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Status dropdown
                var statusExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it }
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        statusOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    status = option
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (type.isNotEmpty() && description.isNotEmpty()) {
                        onSave(
                            HealthRecord(
                                id = UUID.randomUUID().toString(),
                                fowlId = "",
                                type = HealthEventType.valueOf(type.uppercase()),
                                description = description,
                                date = date,
                                veterinarian = veterinarian,
                                status = status,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
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