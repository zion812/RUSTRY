// generated/phase2/app/src/main/java/com/rio/rustry/orders/CartScreen.kt

package com.rio.rustry.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rio.rustry.data.model.CartItem
import com.rio.rustry.payment.PaymentMethod

@Composable
fun CartScreen(
    onCheckout: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadCart()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Text(
            text = "Shopping Cart",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.items.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Your cart is empty",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add some fowls to get started",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.items) { item ->
                        CartItemCard(
                            item = item,
                            onQuantityChange = { newQuantity ->
                                viewModel.updateQuantity(item.fowlId, newQuantity)
                            },
                            onRemove = { viewModel.removeFromCart(item.fowlId) }
                        )
                    }
                }

                // Payment Method Selection
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Payment Method",
                            style = MaterialTheme.typography.titleMedium,
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

                // Total and Checkout
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total:",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$${uiState.total}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = onCheckout,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState.items.isNotEmpty() && !uiState.isLoading
                        ) {
                            Text("Proceed to Checkout")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("IMG")
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.fowlName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${item.price} each",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Quantity controls
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onQuantityChange(maxOf(1, item.quantity - 1)) },
                        enabled = item.quantity > 1
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    
                    Text(
                        text = "${item.quantity}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    IconButton(
                        onClick = { onQuantityChange(item.quantity + 1) }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$${item.price * item.quantity}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}