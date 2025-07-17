package com.rio.rustry.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rio.rustry.domain.model.UserType

/**
 * Order placement screen for handling payments and COD
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderPlacementScreen(
    navController: NavHostController,
    productId: String,
    userType: UserType = UserType.GENERAL
) {
    var selectedPaymentMethod by remember { mutableStateOf("cod") }
    var deliveryAddress by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var specialInstructions by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(1) }
    var agreedToTerms by remember { mutableStateOf(false) }
    var showGDPRConsent by remember { mutableStateOf(true) }
    var gdprConsent by remember { mutableStateOf(false) }
    
    // Sample product data - in real app, fetch from repository
    val product = getSampleProduct(productId)
    val totalAmount = product.price * quantity
    val deliveryCharges = if (selectedPaymentMethod == "online") 0.0 else 50.0
    val finalAmount = totalAmount + deliveryCharges
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Place Order") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
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
            // GDPR Consent Dialog
            if (showGDPRConsent) {
                GDPRConsentCard(
                    onAccept = { 
                        gdprConsent = true
                        showGDPRConsent = false 
                    },
                    onDecline = { 
                        navController.navigateUp() 
                    }
                )
            }
            
            // Product Summary Card
            ProductSummaryCard(
                product = product,
                quantity = quantity,
                onQuantityChange = { quantity = it }
            )
            
            // Delivery Information
            DeliveryInformationCard(
                deliveryAddress = deliveryAddress,
                onAddressChange = { deliveryAddress = it },
                phoneNumber = phoneNumber,
                onPhoneChange = { phoneNumber = it },
                specialInstructions = specialInstructions,
                onInstructionsChange = { specialInstructions = it }
            )
            
            // Payment Method Selection
            PaymentMethodCard(
                selectedMethod = selectedPaymentMethod,
                onMethodChange = { selectedPaymentMethod = it }
            )
            
            // Order Summary
            OrderSummaryCard(
                subtotal = totalAmount,
                deliveryCharges = deliveryCharges,
                total = finalAmount,
                paymentMethod = selectedPaymentMethod
            )
            
            // Terms and Conditions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = agreedToTerms,
                    onCheckedChange = { agreedToTerms = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "I agree to the Terms & Conditions and Privacy Policy",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Place Order Button
            Button(
                onClick = {
                    if (validateOrder(deliveryAddress, phoneNumber, agreedToTerms, gdprConsent)) {
                        // Process order
                        processOrder(
                            productId = productId,
                            quantity = quantity,
                            paymentMethod = selectedPaymentMethod,
                            deliveryAddress = deliveryAddress,
                            phoneNumber = phoneNumber,
                            specialInstructions = specialInstructions,
                            totalAmount = finalAmount
                        )
                        navController.navigate("order_confirmation")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = agreedToTerms && gdprConsent && deliveryAddress.isNotBlank() && phoneNumber.isNotBlank()
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Place Order - ₹${finalAmount.toInt()}")
            }
            
            // Security Notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = "Security",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Your order and payment information is secure and encrypted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun GDPRConsentCard(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.PrivacyTip,
                    contentDescription = "Privacy",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Data Privacy Consent",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "We collect and process your personal data (name, address, phone) to fulfill your order. Your data is stored securely and used only for order processing, delivery, and customer support. You can request data deletion anytime.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Decline")
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Accept")
                }
            }
        }
    }
}

@Composable
fun ProductSummaryCard(
    product: SimpleProduct,
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${product.breed} • ${product.weight}kg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${product.price.toInt()} each",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Quantity Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) onQuantityChange(quantity - 1) }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    Text(
                        text = quantity.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    IconButton(
                        onClick = { onQuantityChange(quantity + 1) }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }
        }
    }
}

@Composable
fun DeliveryInformationCard(
    deliveryAddress: String,
    onAddressChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneChange: (String) -> Unit,
    specialInstructions: String,
    onInstructionsChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Delivery Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = deliveryAddress,
                onValueChange = onAddressChange,
                label = { Text("Delivery Address *") },
                placeholder = { Text("Enter your complete address") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = onPhoneChange,
                label = { Text("Phone Number *") },
                placeholder = { Text("Enter your phone number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = specialInstructions,
                onValueChange = onInstructionsChange,
                label = { Text("Special Instructions (Optional)") },
                placeholder = { Text("Any special delivery instructions") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )
        }
    }
}

@Composable
fun PaymentMethodCard(
    selectedMethod: String,
    onMethodChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .selectableGroup()
        ) {
            Text(
                text = "Payment Method",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Cash on Delivery
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedMethod == "cod",
                        onClick = { onMethodChange("cod") },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedMethod == "cod",
                    onClick = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Cash on Delivery",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Pay when you receive the product (+₹50 delivery charges)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Online Payment
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedMethod == "online",
                        onClick = { onMethodChange("online") },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedMethod == "online",
                    onClick = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Online Payment",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Pay now using UPI, Card, or Net Banking (Free delivery)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Advance Payment
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedMethod == "advance",
                        onClick = { onMethodChange("advance") },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedMethod == "advance",
                    onClick = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Advance Payment (50%)",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Pay 50% now, rest on delivery",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun OrderSummaryCard(
    subtotal: Double,
    deliveryCharges: Double,
    total: Double,
    paymentMethod: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Payment Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal")
                Text("₹${subtotal.toInt()}")
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Delivery Charges")
                Text(
                    text = if (deliveryCharges > 0) "₹${deliveryCharges.toInt()}" else "FREE",
                    color = if (deliveryCharges > 0) MaterialTheme.colorScheme.onSurface else Color(0xFF4CAF50)
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Amount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹${total.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            if (paymentMethod == "advance") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pay Now (50%)",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "₹${(total * 0.5).toInt()}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// Helper functions
private fun getSampleProduct(productId: String): SimpleProduct {
    return SimpleProduct(
        id = productId,
        title = "Healthy Desi Hen",
        breed = "Desi",
        age = 6,
        price = 800.0,
        weight = 1.5,
        location = "Hyderabad",
        farmerName = "Ravi Kumar",
        isVerified = true
    )
}

private fun validateOrder(
    address: String,
    phone: String,
    agreedToTerms: Boolean,
    gdprConsent: Boolean
): Boolean {
    return address.isNotBlank() && 
           phone.isNotBlank() && 
           agreedToTerms && 
           gdprConsent
}

private fun processOrder(
    productId: String,
    quantity: Int,
    paymentMethod: String,
    deliveryAddress: String,
    phoneNumber: String,
    specialInstructions: String,
    totalAmount: Double
) {
    // In real implementation, this would:
    // 1. Create order in database
    // 2. Process payment if online/advance
    // 3. Send notifications to seller
    // 4. Update inventory
    // 5. Generate order tracking ID
    println("Order processed: $productId, Qty: $quantity, Method: $paymentMethod, Amount: $totalAmount")
}