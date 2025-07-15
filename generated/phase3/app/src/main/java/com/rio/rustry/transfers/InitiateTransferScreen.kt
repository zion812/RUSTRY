// generated/phase3/app/src/main/java/com/rio/rustry/transfers/InitiateTransferScreen.kt

package com.rio.rustry.transfers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rio.rustry.data.model.Fowl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitiateTransferScreen(
    onNavigateBack: () -> Unit,
    onTransferInitiated: (String) -> Unit,
    viewModel: TransferViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedFowl by remember { mutableStateOf<Fowl?>(null) }
    var recipientEmail by remember { mutableStateOf("") }
    var recipientPhone by remember { mutableStateOf("") }
    var contactMethod by remember { mutableStateOf(ContactMethod.EMAIL) }

    LaunchedEffect(Unit) {
        viewModel.loadUserFowls()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Initiate Transfer",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is TransferUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is TransferUiState.FowlsLoaded -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Fowl Selection
                    item {
                        Card {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Select Fowl to Transfer",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                uiState.fowls.forEach { fowl ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedFowl?.id == fowl.id,
                                            onClick = { selectedFowl = fowl }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = fowl.name,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(
                                                text = "Breed: ${fowl.breed}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Contact Method Selection
                    item {
                        Card {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Contact Method",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row {
                                    FilterChip(
                                        onClick = { contactMethod = ContactMethod.EMAIL },
                                        label = { Text("Email") },
                                        selected = contactMethod == ContactMethod.EMAIL,
                                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    FilterChip(
                                        onClick = { contactMethod = ContactMethod.PHONE },
                                        label = { Text("Phone") },
                                        selected = contactMethod == ContactMethod.PHONE,
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                                    )
                                }
                            }
                        }
                    }

                    // Recipient Input
                    item {
                        Card {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Recipient Information",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                when (contactMethod) {
                                    ContactMethod.EMAIL -> {
                                        OutlinedTextField(
                                            value = recipientEmail,
                                            onValueChange = { recipientEmail = it },
                                            label = { Text("Recipient Email") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    ContactMethod.PHONE -> {
                                        OutlinedTextField(
                                            value = recipientPhone,
                                            onValueChange = { recipientPhone = it },
                                            label = { Text("Recipient Phone") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Initiate Button
                    item {
                        Button(
                            onClick = {
                                selectedFowl?.let { fowl ->
                                    val recipient = when (contactMethod) {
                                        ContactMethod.EMAIL -> recipientEmail
                                        ContactMethod.PHONE -> recipientPhone
                                    }
                                    viewModel.initiateTransfer(fowl.id, recipient, contactMethod)
                                }
                            },
                            enabled = selectedFowl != null && 
                                     ((contactMethod == ContactMethod.EMAIL && recipientEmail.isNotBlank()) ||
                                      (contactMethod == ContactMethod.PHONE && recipientPhone.isNotBlank())),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Initiate Transfer")
                        }
                    }
                }
            }
            is TransferUiState.TransferInitiated -> {
                LaunchedEffect(uiState.transferId) {
                    onTransferInitiated(uiState.transferId)
                }
            }
            is TransferUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

enum class ContactMethod {
    EMAIL, PHONE
}