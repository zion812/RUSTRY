// generated/phase3/app/src/main/java/com/rio/rustry/breeding/VaccinationSchedulerScreen.kt

package com.rio.rustry.breeding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinationSchedulerScreen(
    fowlId: String,
    viewModel: BreedingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<VaccinationEvent?>(null) }

    LaunchedEffect(fowlId) {
        viewModel.loadVaccinationSchedule(fowlId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Vaccination Schedule",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Vaccination")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is BreedingUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is BreedingUiState.VaccinationScheduleLoaded -> {
                if (state.events.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No vaccinations scheduled",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.events) { event ->
                            VaccinationEventCard(
                                event = event,
                                onEdit = { editingEvent = event },
                                onDelete = { viewModel.deleteVaccinationEvent(event.id) },
                                onMarkComplete = { viewModel.markVaccinationComplete(event.id) }
                            )
                        }
                    }
                }
            }
            is BreedingUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {
                // Handle other states
            }
        }
    }

    // Add Vaccination Dialog
    if (showAddDialog) {
        AddVaccinationDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { event ->
                viewModel.addVaccinationEvent(fowlId, event)
                showAddDialog = false
            }
        )
    }

    // Edit Vaccination Dialog
    editingEvent?.let { event ->
        EditVaccinationDialog(
            event = event,
            onDismiss = { editingEvent = null },
            onUpdate = { updatedEvent ->
                viewModel.updateVaccinationEvent(updatedEvent)
                editingEvent = null
            }
        )
    }
}

@Composable
fun VaccinationEventCard(
    event: VaccinationEvent,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarkComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.vaccineName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                VaccinationStatusChip(status = event.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Scheduled: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(event.scheduledDate))}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (event.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (event.status == VaccinationStatus.PENDING) {
                    Button(
                        onClick = onMarkComplete,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Mark Complete")
                    }
                }
                
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun VaccinationStatusChip(status: VaccinationStatus) {
    val (color, text) = when (status) {
        VaccinationStatus.PENDING -> MaterialTheme.colorScheme.primary to "Pending"
        VaccinationStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary to "Completed"
        VaccinationStatus.OVERDUE -> MaterialTheme.colorScheme.error to "Overdue"
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun AddVaccinationDialog(
    onDismiss: () -> Unit,
    onAdd: (VaccinationEvent) -> Unit
) {
    var vaccineName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Vaccination") },
        text = {
            Column {
                OutlinedTextField(
                    value = vaccineName,
                    onValueChange = { vaccineName = it },
                    label = { Text("Vaccine Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
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
            Button(
                onClick = {
                    if (vaccineName.isNotBlank()) {
                        onAdd(
                            VaccinationEvent(
                                id = "",
                                fowlId = "",
                                vaccineName = vaccineName,
                                scheduledDate = selectedDate,
                                completedDate = null,
                                status = VaccinationStatus.PENDING,
                                notes = notes
                            )
                        )
                    }
                },
                enabled = vaccineName.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditVaccinationDialog(
    event: VaccinationEvent,
    onDismiss: () -> Unit,
    onUpdate: (VaccinationEvent) -> Unit
) {
    var vaccineName by remember { mutableStateOf(event.vaccineName) }
    var notes by remember { mutableStateOf(event.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Vaccination") },
        text = {
            Column {
                OutlinedTextField(
                    value = vaccineName,
                    onValueChange = { vaccineName = it },
                    label = { Text("Vaccine Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
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
            Button(
                onClick = {
                    onUpdate(
                        event.copy(
                            vaccineName = vaccineName,
                            notes = notes
                        )
                    )
                },
                enabled = vaccineName.isNotBlank()
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

data class VaccinationEvent(
    val id: String,
    val fowlId: String,
    val vaccineName: String,
    val scheduledDate: Long,
    val completedDate: Long?,
    val status: VaccinationStatus,
    val notes: String
)

enum class VaccinationStatus {
    PENDING, COMPLETED, OVERDUE
}