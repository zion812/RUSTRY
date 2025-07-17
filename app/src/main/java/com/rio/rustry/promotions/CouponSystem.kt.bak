// generated/phase3/app/src/main/java/com/rio/rustry/promotions/CouponSystem.kt

package com.rio.rustry.promotions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouponSystemScreen(
    viewModel: PromotionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        viewModel.loadAvailableCoupons()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Available Coupons",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is PromotionsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is PromotionsUiState.CouponsLoaded -> {
                if (uiState.coupons.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.LocalOffer,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No coupons available",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.coupons) { coupon ->
                            CouponCard(
                                coupon = coupon,
                                onCopyCode = {
                                    clipboardManager.setText(AnnotatedString(coupon.code))
                                },
                                onApplyCoupon = { viewModel.applyCoupon(coupon.code) }
                            )
                        }
                    }
                }
            }
            is PromotionsUiState.Error -> {
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
            else -> {
                // Handle other states
            }
        }
    }
}

@Composable
fun CouponCard(
    coupon: Coupon,
    onCopyCode: () -> Unit,
    onApplyCoupon: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (coupon.type) {
                CouponType.PERCENTAGE -> Color(0xFFE8F5E8)
                CouponType.FIXED_AMOUNT -> Color(0xFFE3F2FD)
                CouponType.FREE_SHIPPING -> Color(0xFFFFF3E0)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = coupon.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = coupon.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                CouponTypeChip(type = coupon.type, value = coupon.value)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Coupon Code
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = coupon.code,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onCopyCode) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expiry and Usage Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Expires: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(coupon.expiryDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Used: ${coupon.usedCount}/${coupon.maxUses}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Apply Button
            Button(
                onClick = onApplyCoupon,
                enabled = coupon.isValid && coupon.usedCount < coupon.maxUses,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (coupon.isValid && coupon.usedCount < coupon.maxUses) 
                        "Apply Coupon" else "Expired/Used"
                )
            }
        }
    }
}

@Composable
fun CouponTypeChip(type: CouponType, value: Double) {
    val (text, color) = when (type) {
        CouponType.PERCENTAGE -> "${value.toInt()}% OFF" to Color(0xFF4CAF50)
        CouponType.FIXED_AMOUNT -> "$${value.toInt()} OFF" to Color(0xFF2196F3)
        CouponType.FREE_SHIPPING -> "FREE SHIP" to Color(0xFFFF9800)
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

data class Coupon(
    val id: String,
    val code: String,
    val title: String,
    val description: String,
    val type: CouponType,
    val value: Double,
    val minOrderAmount: Double,
    val maxUses: Int,
    val usedCount: Int,
    val expiryDate: Long,
    val isValid: Boolean
)

enum class CouponType {
    PERCENTAGE,
    FIXED_AMOUNT,
    FREE_SHIPPING
}