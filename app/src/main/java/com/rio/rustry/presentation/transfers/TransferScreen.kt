package com.rio.rustry.presentation.transfers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rio.rustry.domain.model.TransferRequest
import com.rio.rustry.domain.model.TransferStatus
import com.rio.rustry.domain.model.TransferVerification
import com.rio.rustry.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    onNavigateBack: () -> Unit,
    viewModel: TransferViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedTransfer by remember { mutableStateOf<TransferRequest?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadUserTransfers()
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
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            
            Text(
                text = "Transfers",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Create Transfer")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is TransferUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is TransferUiState.TransfersLoaded -> {
                if (state.transfers.isEmpty()) {
                    EmptyTransfersState(onCreateTransfer = { showCreateDialog = true })
                } else {
                    TransfersList(
                        transfers = state.transfers,
                        onTransferClick = { selectedTransfer = it },
                        onVerifyTransfer = { transferId -> 
                            viewModel.showVerificationDialog(transferId)
                        }
                    )
                }
            }
            
            is TransferUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadUserTransfers() }
                )
            }
            
            else -> {
                // Handle other states
            }
        }
    }

    // Create Transfer Dialog
    if (showCreateDialog) {
        CreateTransferDialog(
            onDismiss = { showCreateDialog = false },
            onCreateTransfer = { fowlId, buyerId, price, notes ->
                viewModel.createTransfer(fowlId, buyerId, price, notes)
                showCreateDialog = false
            }
        )
    }

    // Transfer Details Dialog
    selectedTransfer?.let { transfer ->
        TransferDetailsDialog(
            transfer = transfer,
            onDismiss = { selectedTransfer = null },
            onVerify = { verification ->
                viewModel.submitVerification(transfer.id, verification)
                selectedTransfer = null
            },
            onCancel = {
                viewModel.cancelTransfer(transfer.id)
                selectedTransfer = null
            }
        )
    }

    // Verification Dialog
    if (uiState is TransferUiState.ShowingVerification) {
        VerificationDialog(
            transferId = uiState.transferId,
            onDismiss = { viewModel.hideVerificationDialog() },
            onSubmitVerification = { verification ->
                viewModel.submitVerification(uiState.transferId, verification)
            }
        )
    }
}

@Composable
private fun TransfersList(
    transfers: List<TransferRequest>,
    onTransferClick: (TransferRequest) -> Unit,
    onVerifyTransfer: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(transfers) { transfer ->
            TransferCard(
                transfer = transfer,
                onClick = { onTransferClick(transfer) },
                onVerify = { onVerifyTransfer(transfer.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransferCard(
    transfer: TransferRequest,
    onClick: () -> Unit,
    onVerify: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
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
                    text = "Fowl ID: ${transfer.fowlId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TransferStatusChip(status = transfer.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Price: $${transfer.price}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Buyer: ${transfer.buyerId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (transfer.status == TransferStatus.PENDING_VERIFICATION) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onVerify,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Verify Transfer")
                }
            }
        }
    }
}

@Composable
private fun TransferStatusChip(status: TransferStatus) {
    val (color, text) = when (status) {
        TransferStatus.PENDING -> Color.Orange to "Pending"
        TransferStatus.PENDING_VERIFICATION -> Color.Blue to "Pending Verification"
        TransferStatus.COMPLETED -> Color.Green to "Completed"
        TransferStatus.CANCELLED -> Color.Red to "Cancelled"
        TransferStatus.DISPUTED -> Color.Red to "Disputed"
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun EmptyTransfersState(onCreateTransfer: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.SwapHoriz,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No transfers yet",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onCreateTransfer) {
            Text("Create First Transfer")
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateTransferDialog(
    onDismiss: () -> Unit,
    onCreateTransfer: (String, String, Double, String) -> Unit
) {
    var fowlId by remember { mutableStateOf("") }
    var buyerId by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Transfer") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = fowlId,
                    onValueChange = { fowlId = it },
                    label = { Text("Fowl ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = buyerId,
                    onValueChange = { buyerId = it },
                    label = { Text("Buyer ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
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
            TextButton(
                onClick = {
                    val priceValue = price.toDoubleOrNull() ?: 0.0
                    onCreateTransfer(fowlId, buyerId, priceValue, notes)
                },
                enabled = fowlId.isNotBlank() && buyerId.isNotBlank() && price.isNotBlank()
            ) {
                Text("Create")
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
private fun TransferDetailsDialog(
    transfer: TransferRequest,
    onDismiss: () -> Unit,
    onVerify: (TransferVerification) -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transfer Details") },
        text = {
            Column {
                Text("Fowl ID: ${transfer.fowlId}")
                Text("Buyer: ${transfer.buyerId}")
                Text("Price: $${transfer.price}")
                Text("Status: ${transfer.status}")
                if (transfer.notes.isNotBlank()) {
                    Text("Notes: ${transfer.notes}")
                }
            }
        },
        confirmButton = {
            if (transfer.status == TransferStatus.PENDING_VERIFICATION) {
                TextButton(
                    onClick = {
                        // Create verification with basic details
                        val verification = TransferVerification(
                            transferId = transfer.id,
                            verifierId = "",
                            verified = true,
                            notes = "Verified via dialog",
                            timestamp = System.currentTimeMillis()
                        )
                        onVerify(verification)
                    }
                ) {
                    Text("Verify")
                }
            }
        },
        dismissButton = {
            Row {
                if (transfer.status == TransferStatus.PENDING) {
                    TextButton(onClick = onCancel) {
                        Text("Cancel Transfer")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerificationDialog(
    transferId: String,
    onDismiss: () -> Unit,
    onSubmitVerification: (TransferVerification) -> Unit
) {
    var verified by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Verify Transfer") },
        text = {
            Column {
                Text("Please verify the transfer details:")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = verified,
                        onCheckedChange = { verified = it }
                    )
                    Text("I verify this transfer is accurate")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Verification Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val verification = TransferVerification(
                        transferId = transferId,
                        verifierId = "",
                        verified = verified,
                        notes = notes,
                        timestamp = System.currentTimeMillis()
                    )
                    onSubmitVerification(verification)
                    onDismiss()
                },
                enabled = verified
            ) {
                Text("Submit Verification")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}