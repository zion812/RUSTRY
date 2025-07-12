package com.rio.rustry.presentation.screen.transfer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import coil.compose.AsyncImage
import com.rio.rustry.data.model.*
import com.rio.rustry.presentation.viewmodel.TransferInitiationViewModel
import com.rio.rustry.presentation.viewmodel.TransferResult
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferInitiationScreen(
    fowlId: String,
    paymentId: String? = null,
    onNavigateBack: () -> Unit,
    onTransferInitiated: (String) -> Unit,
    viewModel: TransferInitiationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(fowlId, paymentId) {
        viewModel.initializeTransfer(fowlId, paymentId)
    }
    
    // Handle transfer result
    LaunchedEffect(Unit) {
        viewModel.transferResult.collect { result ->
            when (result) {
                is TransferResult.Success -> {
                    onTransferInitiated(result.transferId)
                }
                is TransferResult.Error -> {
                    // Error is handled in UI state
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Initiate Transfer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.initiateTransfer() },
                        enabled = !uiState.isProcessing
                    ) {
                        if (uiState.isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Initiate")
                        }
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
                
                // Fowl Information Card
                uiState.fowl?.let { fowl ->
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
                                        text = "Current Price: ${NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(fowl.price)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
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
                
                // Payment Information (if from payment)
                uiState.payment?.let { payment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Payment,
                                    contentDescription = "Payment",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Payment Completed",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "Payment ID: ${payment.id}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Amount: ${NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(payment.amount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                // Buyer Information
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Buyer Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        OutlinedTextField(
                            value = uiState.buyerEmail,
                            onValueChange = viewModel::updateBuyerEmail,
                            label = { Text("Buyer Email") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = "Email")
                            }
                        )
                        
                        if (uiState.buyerSearchResult.isNotEmpty()) {
                            Text(
                                text = uiState.buyerSearchResult,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.buyer != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                        
                        uiState.buyer?.let { buyer ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Buyer Found",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Name: ${buyer.name}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Type: ${buyer.userType.name}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    if (buyer.location.isNotEmpty()) {
                                        Text(
                                            text = "Location: ${buyer.location}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Transfer Details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Transfer Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Transfer Type Selection
                        Text(
                            text = "Transfer Type",
                            style = MaterialTheme.typography.labelLarge
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(TransferType.values()) { type ->
                                FilterChip(
                                    selected = uiState.transferType == type,
                                    onClick = { viewModel.updateTransferType(type) },
                                    label = { Text(type.displayName) }
                                )
                            }
                        }
                        
                        // Transfer Price
                        OutlinedTextField(
                            value = if (uiState.transferPrice > 0) uiState.transferPrice.toString() else "",
                            onValueChange = { 
                                val price = it.toDoubleOrNull() ?: 0.0
                                viewModel.updateTransferPrice(price)
                            },
                            label = { Text("Transfer Price (₹)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.CurrencyRupee, contentDescription = "Price")
                            },
                            enabled = uiState.transferType == TransferType.SALE
                        )
                        
                        // Notes
                        OutlinedTextField(
                            value = uiState.notes,
                            onValueChange = viewModel::updateNotes,
                            label = { Text("Transfer Notes (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                }
                
                // Verification Settings
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Verification Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = uiState.requiresVerification,
                                onCheckedChange = viewModel::updateRequiresVerification
                            )
                            Text("Require verification from both parties")
                        }
                        
                        if (uiState.requiresVerification) {
                            // Verification Method Selection
                            Text(
                                text = "Verification Method",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                VerificationMethod.values().forEach { method ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        RadioButton(
                                            selected = uiState.verificationMethod == method,
                                            onClick = { viewModel.updateVerificationMethod(method) }
                                        )
                                        Text(
                                            text = method.displayName,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                            
                            // Witness Email (optional)
                            OutlinedTextField(
                                value = uiState.witnessEmail,
                                onValueChange = viewModel::updateWitnessEmail,
                                label = { Text("Witness Email (Optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = "Witness")
                                }
                            )
                        }
                    }
                }
                
                // Transfer Summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Transfer Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Transfer Type:")
                            Text(
                                text = uiState.transferType.displayName,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        if (uiState.transferPrice > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Transfer Price:")
                                Text(
                                    text = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(uiState.transferPrice),
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Verification Required:")
                            Text(
                                text = if (uiState.requiresVerification) "Yes" else "No",
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        if (uiState.buyer != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Buyer:")
                                Text(
                                    text = uiState.buyer?.name ?: "Unknown",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                // Important Notice
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Column {
                            Text(
                                text = "Important Notice",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Once initiated, both parties will need to confirm the transfer. A digital certificate will be generated upon completion.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar
        }
    }
}