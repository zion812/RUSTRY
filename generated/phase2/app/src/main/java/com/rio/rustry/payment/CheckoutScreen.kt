// generated/phase2/app/src/main/java/com/rio/rustry/payment/CheckoutScreen.kt

package com.rio.rustry.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CheckoutScreen(
    onPaymentSuccess: (String) -> Unit,
    onPaymentFailed: (String) -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.paymentResult) {
        uiState.paymentResult?.let { result ->
            result.fold(
                onSuccess = { orderId -> onPaymentSuccess(orderId) },
                onFailure = { error -> onPaymentFailed(error.message ?: "Payment failed") }
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCheckoutData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Checkout",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = viewModel::loadCheckoutData) {
                        Text("Retry")
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Order Summary
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Order Summary",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                uiState.cartItems.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${item.fowlName} x${item.quantity}")
                                        Text("$${item.price * item.quantity}")
                                    }
                                }
                                
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Total:",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$${uiState.total}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Payment Method
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Payment Method",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                PaymentMethod.values().forEach { method ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = uiState.selectedPaymentMethod == method,
                                            onClick = { viewModel.selectPaymentMethod(method) }
                                        )
                                        Text(
                                            text = method.displayName,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Delivery Address (if needed)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Delivery Address",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                OutlinedTextField(
                                    value = uiState.deliveryAddress,
                                    onValueChange = viewModel::updateDeliveryAddress,
                                    label = { Text("Address") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3
                                )
                            }
                        }
                    }
                }

                // Place Order Button
                Button(
                    onClick = viewModel::placeOrder,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isProcessingPayment && uiState.deliveryAddress.isNotBlank()
                ) {
                    if (uiState.isProcessingPayment) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Place Order")
                }
            }
        }
    }
}