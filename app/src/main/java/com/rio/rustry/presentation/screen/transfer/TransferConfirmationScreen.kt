package com.rio.rustry.presentation.screen.transfer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.rio.rustry.data.model.*
import com.rio.rustry.presentation.viewmodel.TransferConfirmationViewModel
import com.rio.rustry.presentation.viewmodel.ConfirmationResult
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferConfirmationScreen(
    transferId: String,
    onNavigateBack: () -> Unit,
    onTransferConfirmed: () -> Unit,
    onTransferCancelled: () -> Unit,
    onViewCertificate: (String) -> Unit,
    viewModel: TransferConfirmationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    
    LaunchedEffect(transferId) {
        viewModel.loadTransfer(transferId)
    }
    
    // Handle confirmation result
    LaunchedEffect(Unit) {
        viewModel.confirmationResult.collect { result ->
            when (result) {
                is ConfirmationResult.Success -> {
                    onTransferConfirmed()
                }
                is ConfirmationResult.Cancelled -> {
                    onTransferCancelled()
                }
                is ConfirmationResult.Error -> {
                    // Error is handled in UI state
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer Confirmation") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                uiState.transfer?.let { transfer ->
                    // Transfer Status Card
                    TransferStatusCard(
                        transfer = transfer,
                        userRole = uiState.userRole
                    )
                    
                    // Fowl Information
                    uiState.fowl?.let { fowl ->
                        FowlInformationCard(fowl = fowl)
                    }
                    
                    // Transfer Details
                    TransferDetailsCard(transfer = transfer)
                    
                    // Health Summary
                    HealthSummaryCard(healthSummary = uiState.healthSummary)
                    
                    // Verification Section
                    if (transfer.requiresVerification && uiState.canConfirm) {
                        VerificationCard(
                            transfer = transfer,
                            verificationCode = verificationCode,
                            onVerificationCodeChange = { 
                                verificationCode = it
                                viewModel.updateVerificationCode(it)
                            }
                        )
                    }
                    
                    // Action Buttons
                    ActionButtonsSection(
                        transfer = transfer,
                        canConfirm = uiState.canConfirm,
                        isProcessing = uiState.isProcessing,
                        userRole = uiState.userRole,
                        certificateGenerated = uiState.certificateGenerated,
                        certificateId = uiState.certificateId,
                        onConfirm = { 
                            viewModel.confirmTransfer(verificationCode)
                        },
                        onCancel = { showCancelDialog = true },
                        onViewCertificate = { onViewCertificate(uiState.certificateId) }
                    )
                    
                    // Transfer Timeline
                    TransferTimelineCard(transfer = transfer)
                }
            }
        }
    }
    
    // Cancel Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Transfer") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Are you sure you want to cancel this transfer?")
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Reason for cancellation") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelTransfer(cancelReason)
                        showCancelDialog = false
                    },
                    enabled = cancelReason.isNotBlank()
                ) {
                    Text("Cancel Transfer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Transfer")
                }
            }
        )
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar
        }
    }
}

@Composable
private fun TransferStatusCard(
    transfer: OwnershipTransfer,
    userRole: UserRole?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (transfer.status) {
                TransferStatus.COMPLETED -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                TransferStatus.CANCELLED -> Color(0xFFF44336).copy(alpha = 0.1f)
                TransferStatus.DISPUTED -> Color(0xFFFF9800).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transfer Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (transfer.status) {
                        TransferStatus.COMPLETED -> Color(0xFF4CAF50)
                        TransferStatus.CANCELLED -> Color(0xFFF44336)
                        TransferStatus.DISPUTED -> Color(0xFFFF9800)
                        else -> MaterialTheme.colorScheme.primary
                    }
                ) {
                    Text(
                        text = transfer.status.displayName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Text(
                text = transfer.status.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // User role indicator
            userRole?.let { role ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        when (role) {
                            UserRole.SELLER -> Icons.Default.Sell
                            UserRole.BUYER -> Icons.Default.ShoppingCart
                            else -> Icons.Default.Person
                        },
                        contentDescription = role.displayName,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "You are the ${role.displayName.lowercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun FowlInformationCard(fowl: Fowl) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Fowl Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = fowl.imageUrls.firstOrNull(),
                    contentDescription = "Fowl Image",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(4.dp)
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = fowl.breed,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = fowl.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "ID: ${fowl.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            if (fowl.isTraceable) Icons.Default.Verified else Icons.Default.Warning,
                            contentDescription = "Traceability",
                            modifier = Modifier.size(16.dp),
                            tint = if (fowl.isTraceable) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        )
                        Text(
                            text = if (fowl.isTraceable) "Traceable" else "Non-traceable",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (fowl.isTraceable) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransferDetailsCard(transfer: OwnershipTransfer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Transfer Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            TransferDetailRow(
                label = "From:",
                value = transfer.fromUserName
            )
            
            TransferDetailRow(
                label = "To:",
                value = transfer.toUserName
            )
            
            TransferDetailRow(
                label = "Transfer Type:",
                value = transfer.transferType.displayName
            )
            
            if (transfer.transferPrice > 0) {
                TransferDetailRow(
                    label = "Transfer Price:",
                    value = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(transfer.transferPrice)
                )
            }
            
            TransferDetailRow(
                label = "Transfer Date:",
                value = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date().apply { time = transfer.transferDate })
            )
            
            if (transfer.notes.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    text = "Notes:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = transfer.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TransferDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun HealthSummaryCard(healthSummary: HealthSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Health Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Health Score:")
                Text(
                    text = "${healthSummary.healthScore}/100",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        healthSummary.healthScore >= 80 -> Color(0xFF4CAF50)
                        healthSummary.healthScore >= 60 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                )
            }
            
            TransferDetailRow(
                label = "Vaccinations:",
                value = "${healthSummary.vaccinationCount}"
            )
            
            TransferDetailRow(
                label = "Health Records:",
                value = "${healthSummary.totalRecords}"
            )
            
            TransferDetailRow(
                label = "Vet Certificates:",
                value = "${healthSummary.vetCertificates}"
            )
        }
    }
}

@Composable
private fun VerificationCard(
    transfer: OwnershipTransfer,
    verificationCode: String,
    onVerificationCodeChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = "Verification",
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Verification Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Text(
                text = "Please enter the verification code to confirm this transfer.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            OutlinedTextField(
                value = verificationCode,
                onValueChange = onVerificationCodeChange,
                label = { Text("Verification Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Key, contentDescription = "Code")
                }
            )
            
            Text(
                text = "Verification Method: ${transfer.verificationMethod.displayName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(
    transfer: OwnershipTransfer,
    canConfirm: Boolean,
    isProcessing: Boolean,
    userRole: UserRole?,
    certificateGenerated: Boolean,
    certificateId: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onViewCertificate: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Confirm Button
        if (canConfirm && transfer.status != TransferStatus.COMPLETED) {
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isProcessing,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Confirm",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (userRole) {
                            UserRole.SELLER -> "Confirm as Seller"
                            UserRole.BUYER -> "Confirm as Buyer"
                            else -> "Confirm Transfer"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // View Certificate Button
        if (certificateGenerated && certificateId.isNotEmpty()) {
            OutlinedButton(
                onClick = onViewCertificate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Assignment,
                    contentDescription = "Certificate",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "View Digital Certificate",
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Cancel Button
        if (transfer.status in listOf(TransferStatus.PENDING, TransferStatus.SELLER_CONFIRMED, TransferStatus.BUYER_CONFIRMED)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Default.Cancel,
                    contentDescription = "Cancel",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cancel Transfer",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TransferTimelineCard(transfer: OwnershipTransfer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Transfer Timeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            TimelineItem(
                title = "Transfer Initiated",
                timestamp = transfer.createdAt,
                isCompleted = true
            )
            
            if (transfer.sellerConfirmedAt != null) {
                TimelineItem(
                    title = "Seller Confirmed",
                    timestamp = transfer.sellerConfirmedAt,
                    isCompleted = true
                )
            }
            
            if (transfer.buyerConfirmedAt != null) {
                TimelineItem(
                    title = "Buyer Confirmed",
                    timestamp = transfer.buyerConfirmedAt,
                    isCompleted = true
                )
            }
            
            if (transfer.completedAt != null) {
                TimelineItem(
                    title = "Transfer Completed",
                    timestamp = transfer.completedAt,
                    isCompleted = true
                )
            }
        }
    }
}

@Composable
private fun TimelineItem(
    title: String,
    timestamp: Long,
    isCompleted: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = title,
            tint = if (isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCompleted) FontWeight.Medium else FontWeight.Normal,
                color = if (isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date().apply { time = timestamp }),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}