package com.rio.rustry.presentation.screen.health

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rio.rustry.data.model.*
import com.rio.rustry.presentation.viewmodel.AddHealthRecordViewModel
import com.rio.rustry.presentation.viewmodel.SaveResult
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHealthRecordScreen(
    fowlId: String,
    onNavigateBack: () -> Unit,
    onRecordSaved: () -> Unit,
    viewModel: AddHealthRecordViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showFollowUpDatePicker by remember { mutableStateOf(false) }
    var showNextDueDatePicker by remember { mutableStateOf(false) }
    var newSymptom by remember { mutableStateOf("") }
    
    LaunchedEffect(fowlId) {
        viewModel.initializeForFowl(fowlId)
    }
    
    // Handle save result
    LaunchedEffect(Unit) {
        viewModel.saveResult.collect { result ->
            when (result) {
                is SaveResult.Success -> {
                    onRecordSaved()
                }
                is SaveResult.Error -> {
                    // Error is handled in UI state
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Health Record") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveHealthRecord() },
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Validation Errors
            if (uiState.validationErrors.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Please fix the following errors:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        uiState.validationErrors.forEach { error ->
                            Text(
                                text = "• $error",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            // Record Type Selection
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Record Type",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(HealthEventType.values()) { type ->
                            HealthTypeCard(
                                type = type,
                                isSelected = uiState.type == type,
                                onClick = { viewModel.updateType(type) }
                            )
                        }
                    }
                }
            }
            
            // Basic Information
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Basic Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = viewModel::updateTitle,
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = viewModel::updateDescription,
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                    
                    // Date Selection
                    OutlinedTextField(
                        value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(uiState.date)),
                        onValueChange = { },
                        label = { Text("Date") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                            }
                        }
                    )
                    
                    // Severity Selection
                    Text(
                        text = "Severity",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HealthSeverity.values().forEach { severity ->
                            FilterChip(
                                selected = uiState.severity == severity,
                                onClick = { viewModel.updateSeverity(severity) },
                                label = { Text(severity.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(android.graphics.Color.parseColor(severity.color)).copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                }
            }
            
            // Veterinarian Information (for certain types)
            if (uiState.type in listOf(HealthEventType.VACCINATION, HealthEventType.SURGERY, HealthEventType.CHECKUP)) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Veterinarian Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        OutlinedTextField(
                            value = uiState.vetName,
                            onValueChange = viewModel::updateVetName,
                            label = { Text("Veterinarian Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = uiState.vetLicense,
                            onValueChange = viewModel::updateVetLicense,
                            label = { Text("License Number (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
            
            // Medication Information (for medication/treatment types)
            if (uiState.type in listOf(HealthEventType.MEDICATION, HealthEventType.TREATMENT)) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Medication Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        OutlinedTextField(
                            value = uiState.medication,
                            onValueChange = viewModel::updateMedication,
                            label = { Text("Medication Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = uiState.dosage,
                            onValueChange = viewModel::updateDosage,
                            label = { Text("Dosage") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = uiState.treatment,
                            onValueChange = viewModel::updateTreatment,
                            label = { Text("Treatment Details") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4
                        )
                        
                        // Next Due Date for recurring medications
                        OutlinedTextField(
                            value = uiState.nextDueDate?.let { 
                                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                            } ?: "",
                            onValueChange = { },
                            label = { Text("Next Due Date (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                Row {
                                    if (uiState.nextDueDate != null) {
                                        IconButton(onClick = { viewModel.updateNextDueDate(null) }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                    IconButton(onClick = { showNextDueDatePicker = true }) {
                                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                                    }
                                }
                            }
                        )
                    }
                }
            }
            
            // Symptoms (for illness/injury types)
            if (uiState.type in listOf(HealthEventType.ILLNESS, HealthEventType.INJURY)) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Symptoms",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Add symptom field
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newSymptom,
                                onValueChange = { newSymptom = it },
                                label = { Text("Add Symptom") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            IconButton(
                                onClick = {
                                    viewModel.addSymptom(newSymptom)
                                    newSymptom = ""
                                },
                                enabled = newSymptom.isNotBlank()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Symptom")
                            }
                        }
                        
                        // Display symptoms
                        if (uiState.symptoms.isNotEmpty()) {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(120.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.height(100.dp)
                            ) {
                                items(uiState.symptoms) { symptom ->
                                    InputChip(
                                        selected = true,
                                        onClick = { viewModel.removeSymptom(symptom) },
                                        label = { Text(symptom) },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Measurements
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Measurements (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.weight?.toString() ?: "",
                            onValueChange = { 
                                val weight = it.toDoubleOrNull()
                                viewModel.updateWeight(weight)
                            },
                            label = { Text("Weight (kg)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = uiState.temperature?.toString() ?: "",
                            onValueChange = { 
                                val temp = it.toDoubleOrNull()
                                viewModel.updateTemperature(temp)
                            },
                            label = { Text("Temperature (°C)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                }
            }
            
            // Cost and Notes
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Additional Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = if (uiState.cost > 0) uiState.cost.toString() else "",
                        onValueChange = { 
                            val cost = it.toDoubleOrNull() ?: 0.0
                            viewModel.updateCost(cost)
                        },
                        label = { Text("Cost (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = viewModel::updateNotes,
                        label = { Text("Additional Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }
            
            // Follow-up
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Follow-up",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = uiState.followUpRequired,
                            onCheckedChange = viewModel::updateFollowUpRequired
                        )
                        Text("Follow-up required")
                    }
                    
                    if (uiState.followUpRequired) {
                        OutlinedTextField(
                            value = uiState.followUpDate?.let { 
                                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                            } ?: "",
                            onValueChange = { },
                            label = { Text("Follow-up Date") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showFollowUpDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Date Pickers would be implemented here
    // For now, using placeholder implementations
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HealthTypeCard(
    type: HealthEventType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                when (type) {
                    HealthEventType.VACCINATION -> Icons.Default.Vaccines
                    HealthEventType.MEDICATION -> Icons.Default.Medication
                    HealthEventType.CHECKUP -> Icons.Default.MedicalServices
                    HealthEventType.ILLNESS -> Icons.Default.Sick
                    HealthEventType.INJURY -> Icons.Default.LocalHospital
                    HealthEventType.SURGERY -> Icons.Default.MedicalServices
                    else -> Icons.Default.Assignment
                },
                contentDescription = type.displayName,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = type.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}