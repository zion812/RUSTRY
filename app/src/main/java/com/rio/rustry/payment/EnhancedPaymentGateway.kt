package com.rio.rustry.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enhanced Payment Gateway for RUSTRY
 * Supports UPI, Cards, Net Banking, and Cash on Delivery
 * Ready for Razorpay, Stripe, and PayU integration
 */

data class PaymentRequest(
    val orderId: String,
    val amount: Double,
    val currency: String = "INR",
    val description: String,
    val customerEmail: String = "",
    val customerPhone: String = "",
    val customerName: String = "",
    val fowlId: String = "",
    val sellerId: String = "",
    val buyerId: String = ""
)

data class PaymentResponse(
    val success: Boolean,
    val transactionId: String = "",
    val paymentId: String = "",
    val orderId: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "",
    val status: PaymentStatus = PaymentStatus.PENDING,
    val errorMessage: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val gatewayResponse: String = ""
)

enum class PaymentMethod {
    UPI, CARD, NET_BANKING, CASH_ON_DELIVERY, WALLET
}

enum class PaymentStatus {
    PENDING, SUCCESS, FAILED, CANCELLED, REFUNDED
}

enum class PaymentGateway {
    RAZORPAY, STRIPE, PAYU, MOCK
}

class EnhancedPaymentViewModel : ViewModel() {
    
    private val _paymentState = MutableStateFlow(PaymentState())
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    fun updatePaymentMethod(method: PaymentMethod) {
        _paymentState.value = _paymentState.value.copy(selectedMethod = method)
    }
    
    fun updatePaymentGateway(gateway: PaymentGateway) {
        _paymentState.value = _paymentState.value.copy(selectedGateway = gateway)
    }
    
    fun processPayment(
        context: Context,
        request: PaymentRequest,
        onSuccess: (PaymentResponse) -> Unit,
        onFailure: (PaymentResponse) -> Unit
    ) {
        _isProcessing.value = true
        
        viewModelScope.launch {
            try {
                val response = when (_paymentState.value.selectedGateway) {
                    PaymentGateway.RAZORPAY -> processRazorpayPayment(context, request)
                    PaymentGateway.STRIPE -> processStripePayment(context, request)
                    PaymentGateway.PAYU -> processPayUPayment(context, request)
                    PaymentGateway.MOCK -> processMockPayment(request)
                }
                
                _isProcessing.value = false
                
                if (response.success) {
                    onSuccess(response)
                } else {
                    onFailure(response)
                }
                
            } catch (e: Exception) {
                _isProcessing.value = false
                onFailure(
                    PaymentResponse(
                        success = false,
                        orderId = request.orderId,
                        errorMessage = e.message ?: "Payment processing failed",
                        status = PaymentStatus.FAILED
                    )
                )
            }
        }
    }
    
    private suspend fun processRazorpayPayment(context: Context, request: PaymentRequest): PaymentResponse {
        // Razorpay integration
        return when (_paymentState.value.selectedMethod) {
            PaymentMethod.UPI -> processRazorpayUPI(context, request)
            PaymentMethod.CARD -> processRazorpayCard(context, request)
            PaymentMethod.NET_BANKING -> processRazorpayNetBanking(context, request)
            PaymentMethod.WALLET -> processRazorpayWallet(context, request)
            PaymentMethod.CASH_ON_DELIVERY -> processCashOnDelivery(request)
        }
    }
    
    private suspend fun processRazorpayUPI(context: Context, request: PaymentRequest): PaymentResponse {
        // Simulate Razorpay UPI payment
        delay(2000)
        
        return PaymentResponse(
            success = true,
            transactionId = "TXN_${System.currentTimeMillis()}",
            paymentId = "PAY_${System.currentTimeMillis()}",
            orderId = request.orderId,
            amount = request.amount,
            paymentMethod = "UPI",
            status = PaymentStatus.SUCCESS,
            gatewayResponse = "Razorpay UPI payment successful"
        )
    }
    
    private suspend fun processRazorpayCard(context: Context, request: PaymentRequest): PaymentResponse {
        delay(3000)
        
        return PaymentResponse(
            success = true,
            transactionId = "TXN_${System.currentTimeMillis()}",
            paymentId = "PAY_${System.currentTimeMillis()}",
            orderId = request.orderId,
            amount = request.amount,
            paymentMethod = "CARD",
            status = PaymentStatus.SUCCESS,
            gatewayResponse = "Razorpay Card payment successful"
        )
    }
    
    private suspend fun processRazorpayNetBanking(context: Context, request: PaymentRequest): PaymentResponse {
        delay(4000)
        
        return PaymentResponse(
            success = true,
            transactionId = "TXN_${System.currentTimeMillis()}",
            paymentId = "PAY_${System.currentTimeMillis()}",
            orderId = request.orderId,
            amount = request.amount,
            paymentMethod = "NET_BANKING",
            status = PaymentStatus.SUCCESS,
            gatewayResponse = "Razorpay Net Banking payment successful"
        )
    }
    
    private suspend fun processRazorpayWallet(context: Context, request: PaymentRequest): PaymentResponse {
        delay(2500)
        
        return PaymentResponse(
            success = true,
            transactionId = "TXN_${System.currentTimeMillis()}",
            paymentId = "PAY_${System.currentTimeMillis()}",
            orderId = request.orderId,
            amount = request.amount,
            paymentMethod = "WALLET",
            status = PaymentStatus.SUCCESS,
            gatewayResponse = "Razorpay Wallet payment successful"
        )
    }
    
    private suspend fun processStripePayment(context: Context, request: PaymentRequest): PaymentResponse {
        delay(3000)
        
        return PaymentResponse(
            success = true,
            transactionId = "STRIPE_${System.currentTimeMillis()}",
            paymentId = "PI_${System.currentTimeMillis()}",
            orderId = request.orderId,
            amount = request.amount,
            paymentMethod = _paymentState.value.selectedMethod.name,
            status = PaymentStatus.SUCCESS,
            gatewayResponse = "Stripe payment successful"
        )
    }
    
    private suspend fun processPayUPayment(context: Context, request: PaymentRequest): PaymentResponse {
        delay(3500)
        
        return PaymentResponse(
            success = true,
            transactionId = "PAYU_${System.currentTimeMillis()}",
            paymentId = "PAYU_PAY_${System.currentTimeMillis()}",
            orderId = request.orderId,
            amount = request.amount,
            paymentMethod = _paymentState.value.selectedMethod.name,
            status = PaymentStatus.SUCCESS,
            gatewayResponse = "PayU payment successful"
        )
    }
    
    private suspend fun processCashOnDelivery(request: PaymentRequest): PaymentResponse {
        delay(1000)
        
        return PaymentResponse(
            success = true,
            transactionId = "COD_${System.currentTimeMillis()}",
            paymentId = "COD_${System.currentTimeMillis()}",
            orderId = request.orderId,
            amount = request.amount,
            paymentMethod = "CASH_ON_DELIVERY",
            status = PaymentStatus.PENDING,
            gatewayResponse = "Cash on Delivery order placed"
        )
    }
    
    private suspend fun processMockPayment(request: PaymentRequest): PaymentResponse {
        delay(2000)
        
        // Simulate random success/failure for testing
        val isSuccess = (1..10).random() > 2 // 80% success rate
        
        return if (isSuccess) {
            PaymentResponse(
                success = true,
                transactionId = "MOCK_${System.currentTimeMillis()}",
                paymentId = "MOCK_PAY_${System.currentTimeMillis()}",
                orderId = request.orderId,
                amount = request.amount,
                paymentMethod = _paymentState.value.selectedMethod.name,
                status = PaymentStatus.SUCCESS,
                gatewayResponse = "Mock payment successful"
            )
        } else {
            PaymentResponse(
                success = false,
                orderId = request.orderId,
                amount = request.amount,
                paymentMethod = _paymentState.value.selectedMethod.name,
                status = PaymentStatus.FAILED,
                errorMessage = "Mock payment failed - insufficient funds",
                gatewayResponse = "Mock payment failed"
            )
        }
    }
}

data class PaymentState(
    val selectedMethod: PaymentMethod = PaymentMethod.UPI,
    val selectedGateway: PaymentGateway = PaymentGateway.MOCK,
    val isProcessing: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedPaymentScreen(
    paymentRequest: PaymentRequest,
    viewModel: EnhancedPaymentViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onPaymentSuccess: (PaymentResponse) -> Unit,
    onPaymentFailure: (PaymentResponse) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val paymentState by viewModel.paymentState.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Order Summary
            OrderSummaryCard(paymentRequest)
            
            // Payment Method Selection
            PaymentMethodSelection(
                selectedMethod = paymentState.selectedMethod,
                onMethodSelected = viewModel::updatePaymentMethod
            )
            
            // Payment Gateway Selection (for testing)
            PaymentGatewaySelection(
                selectedGateway = paymentState.selectedGateway,
                onGatewaySelected = viewModel::updatePaymentGateway
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Pay Button
            Button(
                onClick = {
                    viewModel.processPayment(
                        context = context,
                        request = paymentRequest,
                        onSuccess = onPaymentSuccess,
                        onFailure = onPaymentFailure
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Processing...")
                } else {
                    Text("Pay ₹${paymentRequest.amount.toInt()}")
                }
            }
        }
    }
}

@Composable
fun OrderSummaryCard(paymentRequest: PaymentRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Order ID:")
                Text(paymentRequest.orderId)
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Description:")
                Text(paymentRequest.description)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total Amount:",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "₹${paymentRequest.amount.toInt()}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun PaymentMethodSelection(
    selectedMethod: PaymentMethod,
    onMethodSelected: (PaymentMethod) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Select Payment Method",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val paymentMethods = listOf(
                PaymentMethod.UPI to Pair(Icons.Default.AccountBalance, "UPI"),
                PaymentMethod.CARD to Pair(Icons.Default.CreditCard, "Card"),
                PaymentMethod.NET_BANKING to Pair(Icons.Default.AccountBalance, "Net Banking"),
                PaymentMethod.WALLET to Pair(Icons.Default.AccountBalanceWallet, "Wallet"),
                PaymentMethod.CASH_ON_DELIVERY to Pair(Icons.Default.LocalShipping, "Cash on Delivery")
            )
            
            paymentMethods.forEach { (method, iconText) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedMethod == method,
                            onClick = { onMethodSelected(method) }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedMethod == method,
                        onClick = { onMethodSelected(method) }
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Icon(
                        iconText.first,
                        contentDescription = iconText.second,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(iconText.second)
                }
            }
        }
    }
}

@Composable
fun PaymentGatewaySelection(
    selectedGateway: PaymentGateway,
    onGatewaySelected: (PaymentGateway) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Payment Gateway (Testing)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val gateways = listOf(
                PaymentGateway.MOCK to "Mock Gateway (Demo)",
                PaymentGateway.RAZORPAY to "Razorpay",
                PaymentGateway.STRIPE to "Stripe",
                PaymentGateway.PAYU to "PayU"
            )
            
            gateways.forEach { (gateway, name) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedGateway == gateway,
                            onClick = { onGatewaySelected(gateway) }
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedGateway == gateway,
                        onClick = { onGatewaySelected(gateway) }
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(name)
                }
            }
        }
    }
}